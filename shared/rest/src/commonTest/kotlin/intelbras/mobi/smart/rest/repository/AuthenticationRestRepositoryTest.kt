package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.auth.model.TokenRenewalRequest
import intelbras.mobi.smart.rest.bodyText
import intelbras.mobi.smart.rest.respondEnvelope
import intelbras.mobi.smart.rest.testApiCaller
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class AuthenticationRestRepositoryTest {

    @Test
    fun `renewToken posts the current token and returns the new one`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = AuthenticationRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"token":"Ot_new_token"}""") }
        )

        val renewed = repository.renewToken(TokenRenewalRequest(token = "Ot_previous_token"))

        val recorded = captured.single()
        assertEquals(HttpMethod.Post, recorded.method)
        assertEquals("/autenticacao/renovarToken", recorded.url.encodedPath)
        assertEquals("""{"token":"Ot_previous_token"}""", recorded.bodyText())
        assertEquals("Ot_new_token", renewed.token)
    }
}
