package intelbras.mobi.smart.ui.feature.devices

import intelbras.mobi.smart.domain.device.model.DeviceKind as DomainDeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceStatus
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeviceListItemMapperTest {

    @Test
    fun `keeps the name given by the user`() {
        val item = device(name = "Câmera da sala").toUiModel()

        assertEquals("Câmera da sala", item.name)
    }

    @Test
    fun `falls back to the model when the device has no name`() {
        val item = device(name = "", model = "iM3-C").toUiModel()

        assertEquals("iM3-C", item.name)
    }

    @Test
    fun `falls back to the serial number when there is no name nor model`() {
        val item = device(serialNumber = "SERIAL-9", name = "", model = "").toUiModel()

        assertEquals("SERIAL-9", item.name)
    }

    @Test
    fun `carries what identifies the device for the next screen`() {
        val item = device(serialNumber = "SERIAL-9", productId = "PRODUTO-9").toUiModel()

        assertEquals("SERIAL-9", item.id)
        assertEquals("PRODUTO-9", item.productId)
    }

    @Test
    fun `maps camera and lock to their own presentation kind`() {
        assertEquals(DeviceKind.Camera, device(kind = DomainDeviceKind.Camera).toUiModel().kind)
        assertEquals(DeviceKind.Lock, device(kind = DomainDeviceKind.Lock).toUiModel().kind)
    }

    @Test
    fun `maps a hub and an unknown device to other`() {
        assertEquals(DeviceKind.Other, device(kind = DomainDeviceKind.Hub).toUiModel().kind)
        assertEquals(DeviceKind.Other, device(kind = DomainDeviceKind.Unknown).toUiModel().kind)
    }

    @Test
    fun `camera and lock have a screen of their own`() {
        assertTrue(device(kind = DomainDeviceKind.Camera).toUiModel().isOpenable)
        assertTrue(device(kind = DomainDeviceKind.Lock).toUiModel().isOpenable)
    }

    @Test
    fun `a hub and an unknown device have nowhere to go`() {
        assertFalse(device(kind = DomainDeviceKind.Hub).toUiModel().isOpenable)
        assertFalse(device(kind = DomainDeviceKind.Unknown).toUiModel().isOpenable)
    }

    @Test
    fun `a subdevice carries the address the platform expects`() {
        val item = device(
            serialNumber = "08B95FFFFE02116A",
            productId = "3Y2FSCDJ",
            kind = DomainDeviceKind.Lock,
            hubSerialNumber = "OGQ0010782013",
            hubProductId = "sqNzDUSq",
        ).toUiModel()

        assertEquals("08B95FFFFE02116A_OGQ0010782013_sqNzDUSq", item.id)
    }

    @Test
    fun `marks the device as online only when the platform says so`() {
        assertTrue(device(status = DeviceStatus.Online).toUiModel().isOnline)
        assertFalse(device(status = DeviceStatus.Offline).toUiModel().isOnline)
        assertFalse(device(status = DeviceStatus.Unknown).toUiModel().isOnline)
    }
}
