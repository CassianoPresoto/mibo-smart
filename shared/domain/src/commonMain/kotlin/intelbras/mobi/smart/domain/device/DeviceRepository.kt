package intelbras.mobi.smart.domain.device

import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceAvailability
import intelbras.mobi.smart.domain.device.model.DeviceBatteryLevel
import intelbras.mobi.smart.domain.device.model.DeviceCapabilities
import intelbras.mobi.smart.domain.device.model.DeviceFirmware
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.device.model.DeviceSerial
import intelbras.mobi.smart.domain.device.model.RenameDeviceRequest

interface DeviceRepository {
    suspend fun listDevices(query: DeviceListQuery): DeviceListPage

    suspend fun findDevice(serial: DeviceSerial): Device

    suspend fun readCapabilities(serial: DeviceSerial): DeviceCapabilities

    suspend fun readAvailability(serial: DeviceSerial): DeviceAvailability

    suspend fun readFirmware(serial: DeviceSerial): DeviceFirmware

    suspend fun readBatteryLevel(reference: DeviceReference): DeviceBatteryLevel

    suspend fun rename(request: RenameDeviceRequest)

    suspend fun requestFirmwareUpdate(serial: DeviceSerial)
}
