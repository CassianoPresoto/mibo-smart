package intelbras.mobi.smart.ui.devices

import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceListPage
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import intelbras.mobi.smart.domain.device.model.DeviceStatus

internal fun device(
    serialNumber: String = "SERIAL-1",
    name: String = "Câmera da sala",
    model: String = "iM3-C",
    status: DeviceStatus = DeviceStatus.Online,
) = Device(
    serialNumber = serialNumber,
    name = name,
    model = model,
    status = status,
)

internal fun pageOf(vararg devices: Device) = DeviceListPage(
    page = DeviceListQuery.FIRST_PAGE,
    pageSize = DeviceListQuery.DEFAULT_PAGE_SIZE,
    origin = DeviceOriginFilter.All,
    devices = devices.toList(),
)
