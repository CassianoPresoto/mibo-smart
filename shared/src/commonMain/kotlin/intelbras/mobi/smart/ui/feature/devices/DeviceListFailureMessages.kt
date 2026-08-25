package intelbras.mobi.smart.ui.feature.devices

import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.device_list_failure_invalid_token
import mibosmart.shared.generated.resources.device_list_failure_network
import mibosmart.shared.generated.resources.device_list_failure_unexpected
import org.jetbrains.compose.resources.StringResource

internal fun DeviceListFailure.messageResource(): StringResource = when (this) {
    DeviceListFailure.InvalidToken -> Res.string.device_list_failure_invalid_token
    DeviceListFailure.NetworkUnavailable -> Res.string.device_list_failure_network
    DeviceListFailure.Unexpected -> Res.string.device_list_failure_unexpected
}
