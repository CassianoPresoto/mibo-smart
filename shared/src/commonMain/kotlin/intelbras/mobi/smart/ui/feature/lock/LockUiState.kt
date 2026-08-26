package intelbras.mobi.smart.ui.feature.lock

import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel

data class LockUiState(
    val status: LockStatus = LockStatus.Checking,
    val isSwitching: Boolean = false,
    val awaitingConfirmation: Boolean = false,
    val failure: LockFailure? = null,
    val volume: LockVolumeUiState = LockVolumeUiState(),
    val history: LockHistoryUiState = LockHistoryUiState(),
    val details: LockDetailsUiState = LockDetailsUiState(),
) {
    val canSwitch: Boolean get() = !isSwitching && status != LockStatus.Checking
}

data class LockDetailsUiState(
    val batteryPercentage: Int? = null,
    val signalStrength: Int? = null,
    val remoteOpeningEnabled: Boolean? = null,
)

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

data class LockHistoryUiState(
    val openings: List<LockOpeningUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isUnavailable: Boolean = false,
    val failure: LockFailure? = null,
) {
    val isEmpty: Boolean
        get() = openings.isEmpty() && !isLoading && !isUnavailable && failure == null

    val latest: List<LockOpeningUiModel> get() = openings.take(CARD_OPENINGS)

    val hasOpenings: Boolean get() = openings.isNotEmpty()

    private companion object {
        const val CARD_OPENINGS = 5
    }
}

data class LockOpeningUiModel(
    val id: String,
    val happenedAt: String,
    val time: String,
    val user: String,
    val way: LockOpeningWayUiModel,
)

sealed interface LockOpeningWayUiModel {
    data object RemoteApp : LockOpeningWayUiModel

    data class Unrecognized(val name: String) : LockOpeningWayUiModel
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
