package intelbras.mobi.smart.ui.devices

import intelbras.mobi.smart.business.usecase.CatalogDevice

internal fun CatalogDevice.toListItem(): DeviceListItem = DeviceListItem(
    serialNumber = device.serialNumber,
    productId = device.productId,
    name = device.name.ifBlank { device.model }.ifBlank { device.serialNumber },
    model = device.model,
    isOnline = device.isOnline,
    kind = kind,
)
