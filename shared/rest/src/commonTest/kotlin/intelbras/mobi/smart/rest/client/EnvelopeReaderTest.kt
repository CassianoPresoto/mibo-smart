package intelbras.mobi.smart.rest.client

import intelbras.mobi.smart.rest.TestResource
import intelbras.mobi.smart.rest.SmartHomeInvalidRequestException
import intelbras.mobi.smart.rest.SmartHomeOperationRejectedException
import intelbras.mobi.smart.rest.SmartHomeUnexpectedResponseException
import kotlinx.serialization.builtins.ListSerializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class EnvelopeReaderTest {

    private val reader = EnvelopeReader(restJson())
    private val resourceList = ListSerializer(TestResource.serializer())

    @Test
    fun `reads the envelope nested under statusCode and body`() {
        val payload = """
            {"statusCode":200,"body":{"status":"sucesso","data":[{"id":"abc","label":"first"}]}}
        """.trimIndent()

        val resources = reader.read(payload, resourceList)

        assertEquals("abc", resources.single().id)
    }

    @Test
    fun `reads the flat envelope without statusCode`() {
        val payload = """{"status":"sucesso","data":[{"id":"abc"}]}"""

        assertEquals(1, reader.read(payload, resourceList).size)
    }

    @Test
    fun `error status throws a rejected operation carrying the platform message`() {
        val payload = """{"statusCode":200,"body":{"status":"erro","msg":"Operation failed"}}"""

        val failure = assertFailsWith<SmartHomeOperationRejectedException> {
            reader.read(payload, resourceList)
        }

        assertEquals("Operation failed", failure.message)
    }

    @Test
    fun `invalid parameter throws an invalid request`() {
        val payload =
            """{"statusCode":400,"body":{"status":"erro","msg":"Parâmetro inválido para 'origem'"}}"""

        assertFailsWith<SmartHomeInvalidRequestException> { reader.read(payload, resourceList) }
    }

    @Test
    fun `response without a data field throws an unexpected response`() {
        val payload = """{"statusCode":200,"body":{"status":"sucesso"}}"""

        assertFailsWith<SmartHomeUnexpectedResponseException> { reader.read(payload, resourceList) }
    }

    @Test
    fun `payload that is not json throws an unexpected response`() {
        assertFailsWith<SmartHomeUnexpectedResponseException> {
            reader.read("<html>gateway</html>", resourceList)
        }
    }

    @Test
    fun `unreadable data keeps the original failure as cause`() {
        val payload =
            """{"statusCode":200,"body":{"status":"sucesso","data":{"nao":"e uma lista"}}}"""

        val failure = assertFailsWith<SmartHomeUnexpectedResponseException> {
            reader.read(payload, resourceList)
        }

        assertEquals(true, failure.cause != null)
    }

    @Test
    fun `command without a body counts as completed`() {
        reader.readAcknowledgement("")
    }

    @Test
    fun `command with error status does not count as completed`() {
        assertFailsWith<SmartHomeOperationRejectedException> {
            reader.readAcknowledgement("""{"status":"erro","msg":"Operation failed"}""")
        }
    }
}
