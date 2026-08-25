package intelbras.mobi.smart.ui.devices

import intelbras.mobi.smart.domain.device.model.Device

internal fun Device.toListItem(): DeviceListItem = DeviceListItem(
    serialNumber = serialNumber,
    name = name.ifBlank { model }.ifBlank { serialNumber },
    model = model,
    isOnline = isOnline,
)
