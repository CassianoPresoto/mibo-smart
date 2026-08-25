package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceKind

data class CatalogDevice(
    val device: Device,
    val kind: DeviceKind,
)
