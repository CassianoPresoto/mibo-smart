package intelbras.mobi.smart.rest.client

import intelbras.mobi.smart.domain.auth.AccessTokenProvider
import intelbras.mobi.smart.rest.SmartHomeForbiddenException
import intelbras.mobi.smart.rest.SmartHomeInvalidRequestException
import intelbras.mobi.smart.rest.SmartHomeMissingAccessTokenException
import intelbras.mobi.smart.rest.SmartHomeNetworkException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeQuotaExceededException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import intelbras.mobi.smart.rest.SmartHomeUnexpectedResponseException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json

internal typealias PlatformErrorHandler = (status: Int, message: String) -> Unit

internal val NoEndpointErrorHandling: PlatformErrorHandler = { _, _ -> }

internal class SmartHomeApiCaller(
    private val httpClient: HttpClient,
    private val accessTokenProvider: AccessTokenProvider,
    private val envelopeReader: EnvelopeReader,
    private val json: Json,
) {

    suspend fun <B, R> query(
        route: String,
        body: B,
        bodySerializer: SerializationStrategy<B>,
        responseDeserializer: DeserializationStrategy<R>,
        onEndpointError: PlatformErrorHandler = NoEndpointErrorHandling,
    ): R = envelopeReader.read(
        payload = exchange(route, body, bodySerializer, onEndpointError),
        deserializer = responseDeserializer,
    )

    suspend fun <B> command(
        route: String,
        body: B,
        bodySerializer: SerializationStrategy<B>,
        onEndpointError: PlatformErrorHandler = NoEndpointErrorHandling,
    ) = envelopeReader.readAcknowledgement(exchange(route, body, bodySerializer, onEndpointError))

    private suspend fun <B> exchange(
        route: String,
        body: B,
        bodySerializer: SerializationStrategy<B>,
        onEndpointError: PlatformErrorHandler,
    ): String {
        val accessToken = accessTokenProvider.currentAccessToken()
        if (accessToken.isNullOrBlank()) throw SmartHomeMissingAccessTokenException()

        val response = try {
            httpClient.post(route) {
                bearerAuth(accessToken)
                contentType(ContentType.Application.Json)
                setBody(json.encodeToString(bodySerializer, body))
            }
        } catch (cancellation: CancellationException) {
            throw cancellation
        } catch (networkFailure: IOException) {
            throw SmartHomeNetworkException(cause = networkFailure)
        }

        val payload = response.bodyAsText()
        ensureSuccess(response.status.value, payload, onEndpointError)
        return payload
    }

    private fun ensureSuccess(
        status: Int,
        payload: String,
        onEndpointError: PlatformErrorHandler,
    ) {
        if (status in SUCCESS_RANGE) return

        val message = envelopeReader.messageIn(payload)
        onEndpointError(status, message)

        throw when {
            status == UNAUTHORIZED -> SmartHomeUnauthorizedException(describe(status, message))
            status == FORBIDDEN -> SmartHomeForbiddenException(describe(status, message))
            status == NOT_FOUND -> SmartHomeNotFoundException(describe(status, message))
            status == PAYMENT_REQUIRED -> SmartHomeQuotaExceededException(describe(status, message))
            status == BAD_REQUEST -> SmartHomeInvalidRequestException(describe(status, message))
            status >= SERVER_ERROR && message.contains(UNKNOWN_ERROR, ignoreCase = true) ->
                SmartHomeUnknownPlatformErrorException(describe(status, message))

            status >= SERVER_ERROR -> SmartHomeServerException(describe(status, message))
            else -> SmartHomeUnexpectedResponseException(describe(status, message))
        }
    }

    private fun describe(status: Int, message: String): String =
        if (message.isBlank()) "HTTP $status" else "HTTP $status: $message"

    private companion object {
        val SUCCESS_RANGE = 200..299
        const val BAD_REQUEST = 400
        const val UNAUTHORIZED = 401
        const val PAYMENT_REQUIRED = 402
        const val FORBIDDEN = 403
        const val NOT_FOUND = 404
        const val SERVER_ERROR = 500
        const val UNKNOWN_ERROR = "Erro desconhecido"
    }
}
