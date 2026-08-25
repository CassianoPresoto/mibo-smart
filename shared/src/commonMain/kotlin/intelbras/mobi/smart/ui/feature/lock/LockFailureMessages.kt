package intelbras.mobi.smart.ui.feature.lock

import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.lock_failure_network
import mibosmart.shared.generated.resources.lock_failure_offline
import mibosmart.shared.generated.resources.lock_failure_refused
import mibosmart.shared.generated.resources.lock_failure_session_expired
import mibosmart.shared.generated.resources.lock_failure_unexpected
import org.jetbrains.compose.resources.StringResource

internal fun LockFailure.messageResource(): StringResource = when (this) {
    LockFailure.DeviceOffline -> Res.string.lock_failure_offline
    LockFailure.Refused -> Res.string.lock_failure_refused
    LockFailure.SessionExpired -> Res.string.lock_failure_session_expired
    LockFailure.NetworkUnavailable -> Res.string.lock_failure_network
    LockFailure.Unexpected -> Res.string.lock_failure_unexpected
}
