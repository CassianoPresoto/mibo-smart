package intelbras.mobi.smart.business.usecase

sealed interface LockStatusResult {
    data class Known(val isOpen: Boolean) : LockStatusResult

    data object DeviceOffline : LockStatusResult

    data object InvalidToken : LockStatusResult

    data object NetworkUnavailable : LockStatusResult

    data class Error(val cause: Throwable) : LockStatusResult
}

sealed interface LockOperationResult {
    data class Done(val isOpen: Boolean, val confirmed: Boolean) : LockOperationResult

    data object Refused : LockOperationResult

    data object DeviceOffline : LockOperationResult

    data object InvalidToken : LockOperationResult

    data object NetworkUnavailable : LockOperationResult

    data class Error(val cause: Throwable) : LockOperationResult
}
