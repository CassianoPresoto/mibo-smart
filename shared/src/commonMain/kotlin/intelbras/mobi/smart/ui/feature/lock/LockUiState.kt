package intelbras.mobi.smart.ui.feature.lock

data class LockUiState(
    val status: LockStatus = LockStatus.Checking,
    val isSwitching: Boolean = false,
    val awaitingConfirmation: Boolean = false,
    val failure: LockFailure? = null,
) {
    val canSwitch: Boolean get() = !isSwitching && status != LockStatus.Checking
}

enum class LockStatus {
    Checking,
    Open,
    Closed,
    Unknown,
}

sealed interface LockFailure {
    data object DeviceOffline : LockFailure

    data object Refused : LockFailure

    data object SessionExpired : LockFailure

    data object NetworkUnavailable : LockFailure

    data object Unexpected : LockFailure
}
