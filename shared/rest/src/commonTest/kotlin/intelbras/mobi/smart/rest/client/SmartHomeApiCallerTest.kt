package intelbras.mobi.smart.rest.client

import intelbras.mobi.smart.rest.SmartHomeForbiddenException
import intelbras.mobi.smart.rest.SmartHomeInvalidRequestException
import intelbras.mobi.smart.rest.SmartHomeMissingAccessTokenException
import intelbras.mobi.smart.rest.SmartHomeNotFoundException
import intelbras.mobi.smart.rest.SmartHomeQuotaExceededException
import intelbras.mobi.smart.rest.SmartHomeServerException
import intelbras.mobi.smart.rest.SmartHomeUnauthorizedException
import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import intelbras.mobi.smart.rest.TEST_ACCESS_TOKEN
import intelbras.mobi.smart.rest.TEST_ROUTE
import intelbras.mobi.smart.rest.TestRequest
import intelbras.mobi.smart.rest.respondAcknowledgement
import intelbras.mobi.smart.rest.respondJson
import intelbras.mobi.smart.rest.testApiCaller
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class SmartHomeApiCallerTest {

    private val request = TestRequest()

    @Test
    fun `sends the stored access token as a bearer credential`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val caller = testApiCaller(captured) { respondAcknowledgement() }

        caller.command(TEST_ROUTE, request, TestRequest.serializer())

        assertEquals(
            "Bearer $TEST_ACCESS_TOKEN",
            captured.single().headers[HttpHeaders.Authorization]
        )
    }

    @Test
    fun `does not reach the network when there is no stored token`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val caller = testApiCaller(captured, accessToken = null) { respondAcknowledgement() }

        assertFailsWith<SmartHomeMissingAccessTokenException> {
            caller.command(TEST_ROUTE, request, TestRequest.serializer())
        }
        assertTrue(captured.isEmpty())
    }

    @Test
    fun `maps 401 to unauthorized`() = runTest {
        assertFailsWith<SmartHomeUnauthorizedException> {
            call(HttpStatusCode.Unauthorized, """{"msg":"Token não está presente na requisição"}""")
        }
    }

    @Test
    fun `maps 403 to forbidden`() = runTest {
        assertFailsWith<SmartHomeForbiddenException> {
            call(
                HttpStatusCode.Forbidden,
                """{"msg":"Token expirado, por favor gere um novo token"}"""
            )
        }
    }

    @Test
    fun `maps 404 to not found`() = runTest {
        assertFailsWith<SmartHomeNotFoundException> {
            call(
                HttpStatusCode.NotFound,
                """{"msg":"Dispositivo não encontrado ou sem permissão"}"""
            )
        }
    }

    @Test
    fun `maps 402 to quota exceeded`() = runTest {
        assertFailsWith<SmartHomeQuotaExceededException> {
            call(HttpStatusCode.PaymentRequired, """{"msg":"Quota de streaming insuficiente"}""")
        }
    }

    @Test
    fun `maps 400 to invalid request`() = runTest {
        assertFailsWith<SmartHomeInvalidRequestException> {
            call(HttpStatusCode.BadRequest, """{"msg":"Parâmetros inválidos"}""")
        }
    }

    @Test
    fun `maps 500 with unknown error to the platform unknown error`() = runTest {
        val failure = assertFailsWith<SmartHomeUnknownPlatformErrorException> {
            call(
                HttpStatusCode.InternalServerError,
                """{"msg":"Erro desconhecido, por favor tente novamente mais tarde"}""",
            )
        }

        assertEquals(true, failure.message?.contains("HTTP 500"))
    }

    @Test
    fun `an unauthorized envelope arriving with http 200 is still unauthorized`() = runTest {
        val failure = assertFailsWith<SmartHomeUnauthorizedException> {
            call(HttpStatusCode.OK, """{"statusCode":401,"body":"Não autorizado"}""")
        }

        assertEquals(true, failure.message?.contains("Não autorizado"))
    }

    @Test
    fun `an invalid request reported inside a http 200 envelope is still invalid`() = runTest {
        assertFailsWith<SmartHomeInvalidRequestException> {
            call(
                HttpStatusCode.OK,
                """{"statusCode":400,"body":{"status":"erro","msg":"Parâmetro inválido"}}""",
            )
        }
    }

    @Test
    fun `a successful envelope reporting status 200 is not treated as a failure`() = runTest {
        call(HttpStatusCode.OK, """{"statusCode":200,"body":{"status":"sucesso"}}""")
    }

    @Test
    fun `maps other server failures to a server exception`() = runTest {
        assertFailsWith<SmartHomeServerException> {
            call(HttpStatusCode.ServiceUnavailable, """{"msg":"Serviço indisponível"}""")
        }
    }

    private suspend fun call(status: HttpStatusCode, payload: String) {
        val caller = testApiCaller { respondJson(payload, status) }
        caller.command(TEST_ROUTE, request, TestRequest.serializer())
    }
}
