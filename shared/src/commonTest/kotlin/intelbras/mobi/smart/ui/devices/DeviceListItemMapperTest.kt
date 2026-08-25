package intelbras.mobi.smart.ui.devices

import intelbras.mobi.smart.domain.device.model.DeviceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceListItemMapperTest {

    @Test
    fun `keeps the name given by the user`() {
        val item = device(name = "Câmera da sala").toListItem()

        assertEquals("Câmera da sala", item.name)
    }

    @Test
    fun `falls back to the model when the device has no name`() {
        val item = device(name = "", model = "iM3-C").toListItem()

        assertEquals("iM3-C", item.name)
    }

    @Test
    fun `falls back to the serial number when there is no name nor model`() {
        val item = device(serialNumber = "SERIAL-9", name = "", model = "").toListItem()

        assertEquals("SERIAL-9", item.name)
    }

    @Test
    fun `carries what identifies the device for the next screen`() {
        val item = device(serialNumber = "SERIAL-9", productId = "PRODUTO-9").toListItem()

        assertEquals("SERIAL-9", item.serialNumber)
        assertEquals("PRODUTO-9", item.productId)
    }

    @Test
    fun `marks the device as online only when the platform says so`() {
        assertTrue(device(status = DeviceStatus.Online).toListItem().isOnline)
        assertFalse(device(status = DeviceStatus.Offline).toListItem().isOnline)
        assertFalse(device(status = DeviceStatus.Unknown).toListItem().isOnline)
    }
}
