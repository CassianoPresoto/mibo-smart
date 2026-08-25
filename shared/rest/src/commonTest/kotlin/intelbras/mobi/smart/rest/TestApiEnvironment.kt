package intelbras.mobi.smart.rest

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import intelbras.mobi.smart.domain.auth.AccessTokenProvider
import intelbras.mobi.smart.rest.client.EnvelopeReader
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller
import intelbras.mobi.smart.rest.client.createSmartHomeHttpClient
import intelbras.mobi.smart.rest.client.restJson
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.TextContent
import io.ktor.http.headersOf

internal const val TEST_ACCESS_TOKEN = "Ot_test_token"
internal const val SERIAL_NUMBER = "KAYK0109140D9"
internal const val SUBDEVICE_SERIAL_NUMBER = "NS_DEVICE_NS_HUB_ID_HUB"
internal const val PRODUCT_ID = "42"
internal const val SESSION_ID = "6ecd7198-1c2b-4f3a-9d5e-7a1b2c3d4e5f"

internal fun testApiCaller(
    captured: MutableList<HttpRequestData> = mutableListOf(),
    accessToken: String? = TEST_ACCESS_TOKEN,
    handler: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData,
): SmartHomeApiCaller {
    val engine = MockEngine { request ->
        captured += request
        handler(request)
    }
    val json = restJson()
    return SmartHomeApiCaller(
        httpClient = createSmartHomeHttpClient(RestConfiguration(), engine),
        accessTokenProvider = mock<AccessTokenProvider> {
            everySuspend { currentAccessToken() } returns accessToken
        },
        envelopeReader = EnvelopeReader(json),
        json = json,
    )
}

internal fun MockRequestHandleScope.respondEnvelope(data: String): HttpResponseData =
    respondJson("""{"statusCode":200,"body":{"status":"sucesso","data":$data}}""")

internal fun MockRequestHandleScope.respondAcknowledgement(): HttpResponseData =
    respondJson("""{"statusCode":200,"body":{"status":"sucesso"}}""")

internal fun MockRequestHandleScope.respondJson(
    payload: String,
    status: HttpStatusCode = HttpStatusCode.OK,
): HttpResponseData = respond(
    content = payload,
    status = status,
    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString()),
)

internal fun HttpRequestData.bodyText(): String = (body as TextContent).text
