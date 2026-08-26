package intelbras.mobi.smart.ui.feature.lock

import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel

data class LockUiState(
    val status: LockStatus = LockStatus.Checking,
    val isSwitching: Boolean = false,
    val awaitingConfirmation: Boolean = false,
    val failure: LockFailure? = null,
    val volume: LockVolumeUiState = LockVolumeUiState(),
) {
    val canSwitch: Boolean get() = !isSwitching && status != LockStatus.Checking
}

data class LockVolumeUiState(
    val level: LockVolumeLevel? = null,
    val source: LockVolumeSource = LockVolumeSource.Platform,
    val isReading: Boolean = true,
    val isChanging: Boolean = false,
    val awaitingConfirmation: Boolean = false,
    val failure: LockFailure? = null,
) {
    val canChange: Boolean get() = !isReading && !isChanging

    val isRemembered: Boolean get() = source == LockVolumeSource.Remembered
}

enum class LockVolumeSource {
    Platform,
    Remembered,
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
