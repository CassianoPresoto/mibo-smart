package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel

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

sealed interface LockVolumeResult {
    data class Known(val level: LockVolumeLevel) : LockVolumeResult

    data class Remembered(val level: LockVolumeLevel) : LockVolumeResult

    data object DeviceOffline : LockVolumeResult

    data object InvalidToken : LockVolumeResult

    data object NetworkUnavailable : LockVolumeResult

    data class Error(val cause: Throwable) : LockVolumeResult
}

sealed interface LockVolumeChangeResult {
    data class Done(val level: LockVolumeLevel, val confirmed: Boolean) : LockVolumeChangeResult

    data object Refused : LockVolumeChangeResult

    data object DeviceOffline : LockVolumeChangeResult

    data object InvalidToken : LockVolumeChangeResult

    data object NetworkUnavailable : LockVolumeChangeResult

    data class Error(val cause: Throwable) : LockVolumeChangeResult
}
