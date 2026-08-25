package intelbras.mobi.smart.domain.device

import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.device.model.DeviceSerial
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json

class DeviceSerializationTest {

    private val json = Json { encodeDefaults = true }

    @Test
    fun `origin filter travels with the terms the platform accepts`() {
        val query = DeviceListQuery(page = 1, pageSize = 20, origin = DeviceOriginFilter.Shared)

        assertEquals(
            """{"pagina":1,"tamanhoPagina":20,"origem":"compartilhados"}""",
            json.encodeToString(DeviceListQuery.serializer(), query),
        )
    }

    @Test
    fun `a device of its own is addressed by its serial number`() {
        val camera = Device(serialNumber = "KAYK0109140D9")

        assertEquals("KAYK0109140D9", camera.address)
        assertEquals(DeviceSerial("KAYK0109140D9"), camera.serial())
    }

    @Test
    fun `a subdevice is addressed together with the hub it hangs on`() {
        val lock = Device(
            serialNumber = "08B95FFFFE02116A",
            productId = "3Y2FSCDJ",
            isSubdevice = true,
            hubSerialNumber = "OGQ0010782013",
            hubProductId = "sqNzDUSq",
        )

        assertEquals("08B95FFFFE02116A_OGQ0010782013_sqNzDUSq", lock.address)
        assertEquals(
            DeviceReference(
                serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
                productId = "3Y2FSCDJ",
            ),
            lock.reference(),
        )
    }

    @Test
    fun `a subdevice without a known hub keeps its own serial number`() {
        val orphan = Device(serialNumber = "08B95FFFFE02116A", isSubdevice = true)

        assertEquals("08B95FFFFE02116A", orphan.address)
    }

    @Test
    fun `capabilities arrive as a comma separated list`() {
        val capabilities = DeviceCapabilities("RTSV1, AudioTalk ,LocalRecord")

        assertEquals(listOf("RTSV1", "AudioTalk", "LocalRecord"), capabilities.values)
        assertTrue(capabilities.supports("audiotalk"))
        assertFalse(capabilities.supports("CloudRecord"))
    }
}
