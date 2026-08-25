package intelbras.mobi.smart.rest.repository

import intelbras.mobi.smart.domain.device.DeviceRepository
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
import intelbras.mobi.smart.rest.client.ApiRoutes
import intelbras.mobi.smart.rest.client.SmartHomeApiCaller
import kotlinx.serialization.builtins.ListSerializer

internal class DeviceRestRepository(
    private val caller: SmartHomeApiCaller,
) : DeviceRepository {

    override suspend fun listDevices(query: DeviceListQuery): DeviceListPage {
        val devices = caller.query(
            route = ApiRoutes.LIST_DEVICES,
            body = query,
            bodySerializer = DeviceListQuery.serializer(),
            responseDeserializer = ListSerializer(Device.serializer()),
        )
        return DeviceListPage(
            page = query.page,
            pageSize = query.pageSize,
            origin = query.origin,
            devices = devices,
        )
    }

    override suspend fun findDevice(serial: DeviceSerial): Device =
        caller.query(
            route = ApiRoutes.FIND_DEVICE,
            body = serial,
            bodySerializer = DeviceSerial.serializer(),
            responseDeserializer = Device.serializer(),
        )

    override suspend fun readCapabilities(serial: DeviceSerial): DeviceCapabilities =
        caller.query(
            route = ApiRoutes.DEVICE_CAPABILITIES,
            body = serial,
            bodySerializer = DeviceSerial.serializer(),
            responseDeserializer = DeviceCapabilities.serializer(),
        )

    override suspend fun readAvailability(serial: DeviceSerial): DeviceAvailability =
        caller.query(
            route = ApiRoutes.DEVICE_AVAILABILITY,
            body = serial,
            bodySerializer = DeviceSerial.serializer(),
            responseDeserializer = DeviceAvailability.serializer(),
        )

    override suspend fun readFirmware(serial: DeviceSerial): DeviceFirmware =
        caller.query(
            route = ApiRoutes.DEVICE_FIRMWARE,
            body = serial,
            bodySerializer = DeviceSerial.serializer(),
            responseDeserializer = DeviceFirmware.serializer(),
        )

    override suspend fun readBatteryLevel(reference: DeviceReference): DeviceBatteryLevel =
        caller.query(
            route = ApiRoutes.DEVICE_BATTERY,
            body = reference,
            bodySerializer = DeviceReference.serializer(),
            responseDeserializer = DeviceBatteryLevel.serializer(),
        )

    override suspend fun rename(request: RenameDeviceRequest) =
        caller.command(
            route = ApiRoutes.RENAME_DEVICE,
            body = request,
            bodySerializer = RenameDeviceRequest.serializer(),
        )

    override suspend fun requestFirmwareUpdate(serial: DeviceSerial) =
        caller.command(
            route = ApiRoutes.UPDATE_DEVICE,
            body = serial,
            bodySerializer = DeviceSerial.serializer(),
        )
}
