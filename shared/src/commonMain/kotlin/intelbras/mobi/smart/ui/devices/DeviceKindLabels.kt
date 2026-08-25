    package intelbras.mobi.smart.ui.devices

import intelbras.mobi.smart.domain.device.model.DeviceKind
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.device_kind_camera
import mibosmart.shared.generated.resources.device_kind_hub
import mibosmart.shared.generated.resources.device_kind_lock
import mibosmart.shared.generated.resources.device_kind_unknown
import org.jetbrains.compose.resources.StringResource

internal fun DeviceKind.labelResource(): StringResource = when (this) {
    DeviceKind.Camera -> Res.string.device_kind_camera
    DeviceKind.Lock -> Res.string.device_kind_lock
    DeviceKind.Hub -> Res.string.device_kind_hub
    DeviceKind.Unknown -> Res.string.device_kind_unknown
}
