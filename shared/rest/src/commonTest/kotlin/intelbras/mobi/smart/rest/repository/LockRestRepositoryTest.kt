package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.model.DynamicPasswordRequest
import intelbras.mobi.smart.domain.lock.model.LockControlRequest
import intelbras.mobi.smart.domain.lock.model.LockHistoryRequest
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import intelbras.mobi.smart.domain.lock.model.LockVolumeRequest
import intelbras.mobi.smart.domain.lock.model.PasswordDeletionRequest
import intelbras.mobi.smart.domain.lock.model.PeriodicPasswordRequest
import intelbras.mobi.smart.domain.lock.model.RemoteOpeningRequest
import intelbras.mobi.smart.domain.lock.model.SinglePasswordRequest
import intelbras.mobi.smart.rest.PRODUCT_ID
import intelbras.mobi.smart.rest.SUBDEVICE_SERIAL_NUMBER
import intelbras.mobi.smart.rest.bodyText
import intelbras.mobi.smart.rest.respondAcknowledgement
import intelbras.mobi.smart.rest.respondEnvelope
import intelbras.mobi.smart.rest.testApiCaller
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpMethod
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class LockRestRepositoryTest {

    private val reference = DeviceReference(
        serialNumber = SUBDEVICE_SERIAL_NUMBER,
        productId = PRODUCT_ID,
    )
    private val referenceBody = """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID"}"""

    @Test
    fun `readOpeningStatus posts the lock reference and returns whether it is open`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"aberto":true}""") }
        )

        val status = repository.readOpeningStatus(reference)

        val recorded = captured.single()
        assertEquals(HttpMethod.Post, recorded.method)
        assertEquals("/fechaduras/status-abertura/v1", recorded.url.encodedPath)
        assertEquals(referenceBody, recorded.bodyText())
        assertTrue(status.isOpen)
    }

    @Test
    fun `control posts the requested open state`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.control(
            LockControlRequest(
                serialNumber = SUBDEVICE_SERIAL_NUMBER,
                productId = PRODUCT_ID,
                open = true,
            )
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/controle-fechadura/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID","aberto":true}""",
            recorded.bodyText(),
        )
    }

    @Test
    fun `readVolume posts the lock reference and converts the integer into a domain level`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"volume":2}""") }
        )

        val volume = repository.readVolume(reference)

        val recorded = captured.single()
        assertEquals("/fechaduras/volume/v1", recorded.url.encodedPath)
        assertEquals(referenceBody, recorded.bodyText())
        assertEquals(LockVolumeLevel.Medium, volume.volume)
    }

    @Test
    fun `changeVolume posts the level as the integer the platform expects`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.changeVolume(
            LockVolumeRequest(
                serialNumber = SUBDEVICE_SERIAL_NUMBER,
                productId = PRODUCT_ID,
                volume = LockVolumeLevel.High,
            )
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/mudar-volume/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID","volume":3}""",
            recorded.bodyText(),
        )
    }

    @Test
    fun `readOpeningHistory posts the requested amount and returns the records`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(
            testApiCaller(captured) {
                respondEnvelope(
                    """[{"data":"20260824T101500Z","tipo":"senha","usuario":"Cassiano","idUsuario":7}]"""
                )
            }
        )

        val records = repository.readOpeningHistory(
            LockHistoryRequest(serialNumber = SUBDEVICE_SERIAL_NUMBER, limit = 10)
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/historico-abertura/v1", recorded.url.encodedPath)
        assertEquals("""{"ns":"$SUBDEVICE_SERIAL_NUMBER","quantidade":10}""", recorded.bodyText())
        val record = records.single()
        assertEquals("senha", record.type)
        assertEquals("Cassiano", record.user)
        assertEquals(7, record.userId)
    }

    @Test
    fun `readRemoteOpeningStatus posts the lock reference and returns whether it is enabled`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"habilitado":true}""") }
        )

        val status = repository.readRemoteOpeningStatus(reference)

        val recorded = captured.single()
        assertEquals("/fechaduras/status-abrir-remoto/v1", recorded.url.encodedPath)
        assertEquals(referenceBody, recorded.bodyText())
        assertTrue(status.isEnabled)
    }

    @Test
    fun `enableRemoteOpening posts the requested state`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.enableRemoteOpening(
            RemoteOpeningRequest(
                serialNumber = SUBDEVICE_SERIAL_NUMBER,
                productId = PRODUCT_ID,
                enabled = true,
            )
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/habilitar-abrir-remoto/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID","habilitar":true}""",
            recorded.bodyText(),
        )
    }

    @Test
    fun `createSinglePassword posts the password`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.createSinglePassword(
            SinglePasswordRequest(
                serialNumber = SUBDEVICE_SERIAL_NUMBER,
                productId = PRODUCT_ID,
                password = "123456",
            )
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/criar-senha-unica/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID","senha":"123456"}""",
            recorded.bodyText(),
        )
    }

    @Test
    fun `createPeriodicPassword posts the password with its validity window`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.createPeriodicPassword(
            PeriodicPasswordRequest(
                serialNumber = SUBDEVICE_SERIAL_NUMBER,
                productId = PRODUCT_ID,
                password = "654321",
                startsAt = "1756000000",
                endsAt = "1756086400",
            )
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/criar-senha-periodica/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID","senha":"654321","comeco":"1756000000","limite":"1756086400"}""",
            recorded.bodyText(),
        )
    }

    @Test
    fun `createDynamicPassword posts the password`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.createDynamicPassword(
            DynamicPasswordRequest(
                serialNumber = SUBDEVICE_SERIAL_NUMBER,
                productId = PRODUCT_ID,
                password = "12345678",
            )
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/criar-senha-dinamica/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID","senha":"12345678"}""",
            recorded.bodyText(),
        )
    }

    @Test
    fun `deleteSinglePassword posts the password id`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.deleteSinglePassword(
            PasswordDeletionRequest(
                serialNumber = SUBDEVICE_SERIAL_NUMBER,
                productId = PRODUCT_ID,
                passwordId = 7,
            )
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/deletar-senha-unica/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID","idSenha":7}""",
            recorded.bodyText(),
        )
    }

    @Test
    fun `deletePeriodicPassword posts the password id`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = LockRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.deletePeriodicPassword(
            PasswordDeletionRequest(
                serialNumber = SUBDEVICE_SERIAL_NUMBER,
                productId = PRODUCT_ID,
                passwordId = 8,
            )
        )

        val recorded = captured.single()
        assertEquals("/fechaduras/deletar-senha-periodica/v1", recorded.url.encodedPath)
        assertEquals(
            """{"ns":"$SUBDEVICE_SERIAL_NUMBER","idProduto":"$PRODUCT_ID","idSenha":8}""",
            recorded.bodyText(),
        )
    }
}
