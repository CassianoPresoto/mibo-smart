package intelbras.mobi.smart.ui.feature.devices

import intelbras.mobi.smart.business.device.usecase.CatalogDevice
import intelbras.mobi.smart.domain.device.model.Device
import intelbras.mobi.smart.domain.device.model.DeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceStatus

internal fun device(
    serialNumber: String = "SERIAL-1",
    productId: String = "PRODUTO-1",
    name: String = "Câmera da sala",
    model: String = "iM3-C",
    status: DeviceStatus = DeviceStatus.Online,
    kind: DeviceKind = DeviceKind.Camera,
    hubSerialNumber: String = "",
    hubProductId: String = "",
) = CatalogDevice(
    device = Device(
        serialNumber = serialNumber,
        name = name,
        model = model,
        status = status,
        productId = productId,
        isSubdevice = hubSerialNumber.isNotBlank(),
        hubSerialNumber = hubSerialNumber,
        hubProductId = hubProductId,
    ),
    kind = kind,
)
