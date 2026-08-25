package intelbras.mobi.smart.ui.feature.devices

import intelbras.mobi.smart.domain.device.model.DeviceKind
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
    fun `carries the kind the platform announced`() {
        assertEquals(DeviceKind.Camera, device(kind = DeviceKind.Camera).toListItem().kind)
        assertEquals(DeviceKind.Unknown, device(kind = DeviceKind.Unknown).toListItem().kind)
    }

    @Test
    fun `camera and lock have a screen of their own`() {
        assertTrue(device(kind = DeviceKind.Camera).toListItem().hasScreenOfItsOwn)
        assertTrue(device(kind = DeviceKind.Lock).toListItem().hasScreenOfItsOwn)
    }

    @Test
    fun `a hub and an unknown device have nowhere to go`() {
        assertFalse(device(kind = DeviceKind.Hub).toListItem().hasScreenOfItsOwn)
        assertFalse(device(kind = DeviceKind.Unknown).toListItem().hasScreenOfItsOwn)
    }

    @Test
    fun `a subdevice carries the address the platform expects`() {
        val item = device(
            serialNumber = "08B95FFFFE02116A",
            productId = "3Y2FSCDJ",
            kind = DeviceKind.Lock,
            hubSerialNumber = "OGQ0010782013",
            hubProductId = "sqNzDUSq",
        ).toListItem()

        assertEquals("08B95FFFFE02116A", item.serialNumber)
        assertEquals("08B95FFFFE02116A_OGQ0010782013_sqNzDUSq", item.address)
    }

    @Test
    fun `marks the device as online only when the platform says so`() {
        assertTrue(device(status = DeviceStatus.Online).toListItem().isOnline)
        assertFalse(device(status = DeviceStatus.Offline).toListItem().isOnline)
        assertFalse(device(status = DeviceStatus.Unknown).toListItem().isOnline)
    }
}
