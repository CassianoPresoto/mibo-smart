package intelbras.mobi.smart.ui.feature.devices

import intelbras.mobi.smart.business.usecase.CatalogDevice
import intelbras.mobi.smart.domain.device.model.DeviceKind as DomainDeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceOrigin as DomainDeviceOrigin

internal fun CatalogDevice.toUiModel(): DeviceUiModel = DeviceUiModel(
    id = device.address,
    name = device.name.ifBlank { device.model }.ifBlank { device.serialNumber },
    serialNumber = device.serialNumber.ifBlank { null },
    kind = kind.toUiKind(),
    origin = device.origin.toUiOrigin(),
    isOnline = device.isOnline,
    productId = device.productId,
    model = device.model,
)

private fun DomainDeviceKind.toUiKind(): DeviceKind = when (this) {
    DomainDeviceKind.Camera -> DeviceKind.Camera
    DomainDeviceKind.Lock -> DeviceKind.Lock
    DomainDeviceKind.Hub, DomainDeviceKind.Unknown -> DeviceKind.Other
}

private fun DomainDeviceOrigin.toUiOrigin(): DeviceOrigin = when (this) {
    DomainDeviceOrigin.Shared -> DeviceOrigin.Shared
    DomainDeviceOrigin.Linked, DomainDeviceOrigin.Unknown -> DeviceOrigin.Linked
}
