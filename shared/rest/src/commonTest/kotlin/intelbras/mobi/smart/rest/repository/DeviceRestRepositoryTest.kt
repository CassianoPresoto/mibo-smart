package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOrigin
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.device.model.DeviceSerial
import intelbras.mobi.smart.domain.device.model.DeviceStatus
import intelbras.mobi.smart.domain.device.model.RenameDeviceRequest
import intelbras.mobi.smart.rest.PRODUCT_ID
import intelbras.mobi.smart.rest.SERIAL_NUMBER
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

class DeviceRestRepositoryTest {

    private val serial = DeviceSerial(serialNumber = SERIAL_NUMBER)
    private val reference = DeviceReference(serialNumber = SERIAL_NUMBER, productId = PRODUCT_ID)

    private val deviceJson = """
        {"atualizacaoDisponivel":false,"ns":"$SERIAL_NUMBER","modelo":"iM3-C","nome":"iM3-C-40D9",
         "status":"online","versao":"1.0.0","subdispositivo":false,"idProduto":"",
         "ultimaVezOnline":"20260821T200535Z","origem":"vinculado"}
    """.trimIndent()

    @Test
    fun `listDevices posts the paging parameters and returns the page`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(testApiCaller(captured) { respondEnvelope("[$deviceJson]") })

        val page = repository.listDevices(
            DeviceListQuery(page = 2, pageSize = 50, origin = DeviceOriginFilter.Shared)
        )

        val recorded = captured.single()
        assertEquals(HttpMethod.Post, recorded.method)
        assertEquals("/produtos/listar-dispositivos/v1", recorded.url.encodedPath)
        assertEquals(
            """{"pagina":2,"tamanhoPagina":50,"origem":"compartilhados"}""",
            recorded.bodyText(),
        )
        assertEquals(2, page.page)
        assertEquals(DeviceOriginFilter.Shared, page.origin)
        val device = page.devices.single()
        assertEquals(SERIAL_NUMBER, device.serialNumber)
        assertEquals("iM3-C-40D9", device.name)
        assertEquals(DeviceStatus.Online, device.status)
        assertEquals(DeviceOrigin.Linked, device.origin)
    }

    @Test
    fun `listDevices keeps the three required fields even when they hold default values`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(testApiCaller(captured) { respondEnvelope("[]") })

        repository.listDevices(DeviceListQuery())

        assertEquals(
            """{"pagina":1,"tamanhoPagina":20,"origem":"todos"}""",
            captured.single().bodyText(),
        )
    }

    @Test
    fun `listDevices reads a status the platform did not document as unknown`() = runTest {
        val repository = DeviceRestRepository(
            testApiCaller { respondEnvelope("""[{"ns":"X1","status":"em_manutencao"}]""") }
        )

        val page = repository.listDevices(DeviceListQuery())

        assertEquals(DeviceStatus.Unknown, page.devices.single().status)
    }

    @Test
    fun `listDevices reports a next page when it comes full`() = runTest {
        val repository = DeviceRestRepository(testApiCaller { respondEnvelope("[$deviceJson]") })

        val page = repository.listDevices(DeviceListQuery(pageSize = 1))

        assertTrue(page.hasNextPage)
    }

    @Test
    fun `findDevice posts the serial number and returns the device`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(testApiCaller(captured) { respondEnvelope(deviceJson) })

        val device = repository.findDevice(serial)

        val recorded = captured.single()
        assertEquals("/produtos/buscar-dispositivo/v1", recorded.url.encodedPath)
        assertEquals("""{"ns":"$SERIAL_NUMBER"}""", recorded.bodyText())
        assertEquals(SERIAL_NUMBER, device.serialNumber)
    }

    @Test
    fun `readCapabilities posts the serial number and splits the capability list`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"funcoes":"RTSV1,AudioTalk,LocalRecord"}""") }
        )

        val capabilities = repository.readCapabilities(serial)

        val recorded = captured.single()
        assertEquals("/produtos/funcoes/v1", recorded.url.encodedPath)
        assertEquals("""{"ns":"$SERIAL_NUMBER"}""", recorded.bodyText())
        assertEquals(listOf("RTSV1", "AudioTalk", "LocalRecord"), capabilities.values)
        assertTrue(capabilities.supports("localrecord"))
    }

    @Test
    fun `readAvailability posts the serial number and returns whether the device is online`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"online":true}""") }
        )

        val availability = repository.readAvailability(serial)

        val recorded = captured.single()
        assertEquals("/produtos/online/v1", recorded.url.encodedPath)
        assertEquals("""{"ns":"$SERIAL_NUMBER"}""", recorded.bodyText())
        assertTrue(availability.isOnline)
    }

    @Test
    fun `readFirmware posts the serial number and returns the installed version`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"versao":"1.4.2"}""") }
        )

        val firmware = repository.readFirmware(serial)

        val recorded = captured.single()
        assertEquals("/produtos/versao/v1", recorded.url.encodedPath)
        assertEquals("""{"ns":"$SERIAL_NUMBER"}""", recorded.bodyText())
        assertEquals("1.4.2", firmware.version)
    }

    @Test
    fun `readBatteryLevel posts serial number with product id and returns the percentage`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(
            testApiCaller(captured) { respondEnvelope("""{"bateria":80}""") }
        )

        val battery = repository.readBatteryLevel(reference)

        val recorded = captured.single()
        assertEquals("/produtos/bateria/v1", recorded.url.encodedPath)
        assertEquals("""{"ns":"$SERIAL_NUMBER","idProduto":"$PRODUCT_ID"}""", recorded.bodyText())
        assertEquals(80, battery.percentage)
    }

    @Test
    fun `rename posts the new name`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.rename(RenameDeviceRequest(serialNumber = SERIAL_NUMBER, newName = "Portaria"))

        val recorded = captured.single()
        assertEquals("/produtos/mudar-nome/v1", recorded.url.encodedPath)
        assertEquals("""{"ns":"$SERIAL_NUMBER","novoNome":"Portaria"}""", recorded.bodyText())
    }

    @Test
    fun `requestFirmwareUpdate posts the serial number`() = runTest {
        val captured = mutableListOf<HttpRequestData>()
        val repository = DeviceRestRepository(testApiCaller(captured) { respondAcknowledgement() })

        repository.requestFirmwareUpdate(serial)

        val recorded = captured.single()
        assertEquals("/produtos/atualizar-dispositivo/v1", recorded.url.encodedPath)
        assertEquals("""{"ns":"$SERIAL_NUMBER"}""", recorded.bodyText())
    }
}
