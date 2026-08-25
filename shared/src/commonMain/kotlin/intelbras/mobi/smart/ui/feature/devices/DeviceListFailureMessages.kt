package intelbras.mobi.smart.ui.feature.devices

import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.devices_error_expired_action
import mibosmart.shared.generated.resources.devices_error_expired_body
import mibosmart.shared.generated.resources.devices_error_expired_title
import mibosmart.shared.generated.resources.devices_error_generic_body
import mibosmart.shared.generated.resources.devices_error_generic_title
import mibosmart.shared.generated.resources.devices_error_network_body
import mibosmart.shared.generated.resources.devices_error_network_title
import mibosmart.shared.generated.resources.devices_error_retry
import org.jetbrains.compose.resources.StringResource

enum class DeviceListFailure {
    Network,
    ExpiredSession,
    Unexpected,
    ;

    fun titleResource(): StringResource = when (this) {
        Network -> Res.string.devices_error_network_title
        ExpiredSession -> Res.string.devices_error_expired_title
        Unexpected -> Res.string.devices_error_generic_title
    }

    fun bodyResource(): StringResource = when (this) {
        Network -> Res.string.devices_error_network_body
        ExpiredSession -> Res.string.devices_error_expired_body
        Unexpected -> Res.string.devices_error_generic_body
    }

    fun actionResource(): StringResource = when (this) {
        ExpiredSession -> Res.string.devices_error_expired_action
        else -> Res.string.devices_error_retry
    }
}
