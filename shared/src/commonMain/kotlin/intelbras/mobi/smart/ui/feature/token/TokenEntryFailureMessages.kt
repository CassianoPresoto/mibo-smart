package intelbras.mobi.smart.ui.feature.token

import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.token_failure_empty
import mibosmart.shared.generated.resources.token_failure_expired_session
import mibosmart.shared.generated.resources.token_failure_invalid
import mibosmart.shared.generated.resources.token_failure_network
import mibosmart.shared.generated.resources.token_failure_unexpected
import org.jetbrains.compose.resources.StringResource

internal fun TokenEntryFailure.messageResource(): StringResource = when (this) {
    TokenEntryFailure.EmptyToken -> Res.string.token_failure_empty
    TokenEntryFailure.InvalidToken -> Res.string.token_failure_invalid
    TokenEntryFailure.ExpiredSession -> Res.string.token_failure_expired_session
    TokenEntryFailure.NetworkUnavailable -> Res.string.token_failure_network
    TokenEntryFailure.Unexpected -> Res.string.token_failure_unexpected
}
