package intelbras.mobi.smart.ui.feature.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.LockController
import intelbras.mobi.smart.business.usecase.LockDetails
import intelbras.mobi.smart.business.usecase.LockHistoryResult
import intelbras.mobi.smart.business.usecase.LockOperationResult
import intelbras.mobi.smart.business.usecase.LockStatusResult
import intelbras.mobi.smart.business.usecase.LockVolumeChangeResult
import intelbras.mobi.smart.business.usecase.LockVolumeResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LockViewModel(
    private val lockController: LockController,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(LockUiState())
    val uiState: StateFlow<LockUiState> = mutableUiState.asStateFlow()

    private var lock: DeviceReference? = null
    private var work: Job? = null
    private var volumeWork: Job? = null
    private var historyWork: Job? = null
    private var detailsWork: Job? = null

    fun onScreenOpened(lock: DeviceReference) {
        this.lock = lock
        readStatus()
        readVolume()
        readHistory()
        readDetails()
    }

    fun onScreenResumed() {
        readStatus(quietly = true)
        readVolume(quietly = true)
        readHistory(quietly = true)
        readDetails()
    }

    fun onRetry() = readStatus()

    fun onToggle() {
        when (mutableUiState.value.status) {
            LockStatus.Open -> switch(open = false)
            LockStatus.Closed -> switch(open = true)
            LockStatus.Checking, LockStatus.Unknown -> Unit
        }
    }

    fun onVolumeRetry() = readVolume()

    fun onHistoryRetry() = readHistory()

    fun onVolumeSelected(level: LockVolumeLevel) {
        val lock = lock ?: return
        if (volumeWork?.isActive == true) return

        mutableUiState.update { state ->
            state.withVolume { volume ->
                volume.copy(isChanging = true, awaitingConfirmation = false, failure = null)
            }
        }
        volumeWork = viewModelScope.launch {
            val result = lockController.changeVolume(lock, level)
            mutableUiState.update { state -> state.withVolume { volume -> volume.after(result) } }
        }
    }

    private fun readStatus(quietly: Boolean = false) {
        val lock = lock ?: return
        if (work?.isActive == true) return

        if (!quietly) {
            mutableUiState.update { state ->
                state.copy(status = LockStatus.Checking, failure = null)
            }
        }
        work = viewModelScope.launch {
            val result = lockController.statusOf(lock)
            mutableUiState.update { state -> state.after(result) }
        }
    }

    private fun readVolume(quietly: Boolean = false) {
        val lock = lock ?: return
        if (volumeWork?.isActive == true) return

        if (!quietly) {
            mutableUiState.update { state ->
                state.withVolume { volume -> volume.copy(isReading = true, failure = null) }
            }
        }
        volumeWork = viewModelScope.launch {
            val result = lockController.volumeOf(lock)
            mutableUiState.update { state -> state.withVolume { volume -> volume.after(result) } }
        }
    }

    private fun readHistory(quietly: Boolean = false) {
        val lock = lock ?: return
        if (historyWork?.isActive == true) return

        if (!quietly) {
            mutableUiState.update { state ->
                state.copy(history = state.history.copy(isLoading = true, failure = null))
            }
        }
        historyWork = viewModelScope.launch {
            val result = lockController.historyOf(lock, HISTORY_SIZE)
            mutableUiState.update { state -> state.copy(history = state.history.after(result)) }
        }
    }

    private fun readDetails() {
        val lock = lock ?: return
        if (detailsWork?.isActive == true) return

        detailsWork = viewModelScope.launch {
            val details = lockController.detailsOf(lock)
            mutableUiState.update { state -> state.copy(details = details.toUiState()) }
        }
    }

    private fun LockDetails.toUiState() = LockDetailsUiState(
        batteryPercentage = batteryPercentage,
        signalStrength = signalStrength,
        remoteOpeningEnabled = remoteOpeningEnabled,
    )

    private fun switch(open: Boolean) {
        val lock = lock ?: return
        if (work?.isActive == true) return

        mutableUiState.update { state ->
            state.copy(isSwitching = true, awaitingConfirmation = false, failure = null)
        }
        work = viewModelScope.launch {
            val result = lockController.switch(lock, open)
            mutableUiState.update { state -> state.after(result) }
            if (result is LockOperationResult.Done) readHistory(quietly = true)
        }
    }

    private fun LockUiState.withVolume(change: (LockVolumeUiState) -> LockVolumeUiState) =
        copy(volume = change(volume))

    private fun LockUiState.after(result: LockOperationResult): LockUiState = when (result) {
        is LockOperationResult.Done -> copy(
            status = openOrClosed(result.isOpen),
            isSwitching = false,
            awaitingConfirmation = !result.confirmed,
            failure = null,
        )

        else -> copy(isSwitching = false, failure = result.toFailure())
    }

    private fun LockUiState.after(result: LockStatusResult): LockUiState = when (result) {
        is LockStatusResult.Known -> copy(
            status = openOrClosed(result.isOpen),
            isSwitching = false,
            awaitingConfirmation = false,
            failure = null,
        )

        else -> copy(status = LockStatus.Unknown, failure = result.toFailure())
    }

    private fun LockVolumeUiState.after(result: LockVolumeResult): LockVolumeUiState =
        when (result) {
            is LockVolumeResult.Known -> LockVolumeUiState(
                level = result.level,
                source = LockVolumeSource.Platform,
                isReading = false,
            )

            is LockVolumeResult.Remembered -> LockVolumeUiState(
                level = result.level,
                source = LockVolumeSource.Remembered,
                isReading = false,
            )

            else -> copy(isReading = false, failure = result.toFailure())
        }

    private fun LockVolumeUiState.after(result: LockVolumeChangeResult): LockVolumeUiState =
        when (result) {
            is LockVolumeChangeResult.Done -> LockVolumeUiState(
                level = result.level,
                source = if (result.confirmed) LockVolumeSource.Platform else source,
                isReading = false,
                awaitingConfirmation = !result.confirmed && !isRemembered,
            )

            else -> copy(isChanging = false, failure = result.toFailure())
        }

    private fun LockHistoryUiState.after(result: LockHistoryResult): LockHistoryUiState =
        when (result) {
            is LockHistoryResult.Loaded -> LockHistoryUiState(
                openings = result.openings.toUiModels(),
                isLoading = false,
            )

            LockHistoryResult.Unavailable -> LockHistoryUiState(
                isLoading = false,
                isUnavailable = true,
            )

            else -> copy(isLoading = false, failure = result.toFailure())
        }

    private fun LockHistoryResult.toFailure(): LockFailure = when (this) {
        LockHistoryResult.DeviceOffline -> LockFailure.DeviceOffline
        LockHistoryResult.InvalidToken -> LockFailure.SessionExpired
        LockHistoryResult.NetworkUnavailable -> LockFailure.NetworkUnavailable
        LockHistoryResult.Unavailable,
        is LockHistoryResult.Error,
        is LockHistoryResult.Loaded,
        -> LockFailure.Unexpected
    }

    private fun LockOperationResult.toFailure(): LockFailure = when (this) {
        LockOperationResult.Refused -> LockFailure.Refused
        LockOperationResult.DeviceOffline -> LockFailure.DeviceOffline
        LockOperationResult.InvalidToken -> LockFailure.SessionExpired
        LockOperationResult.NetworkUnavailable -> LockFailure.NetworkUnavailable
        is LockOperationResult.Error, is LockOperationResult.Done -> LockFailure.Unexpected
    }

    private fun LockStatusResult.toFailure(): LockFailure = when (this) {
        LockStatusResult.DeviceOffline -> LockFailure.DeviceOffline
        LockStatusResult.InvalidToken -> LockFailure.SessionExpired
        LockStatusResult.NetworkUnavailable -> LockFailure.NetworkUnavailable
        is LockStatusResult.Error, is LockStatusResult.Known -> LockFailure.Unexpected
    }

    private fun LockVolumeResult.toFailure(): LockFailure = when (this) {
        LockVolumeResult.DeviceOffline -> LockFailure.DeviceOffline
        LockVolumeResult.InvalidToken -> LockFailure.SessionExpired
        LockVolumeResult.NetworkUnavailable -> LockFailure.NetworkUnavailable
        is LockVolumeResult.Error,
        is LockVolumeResult.Known,
        is LockVolumeResult.Remembered,
        -> LockFailure.Unexpected
    }

    private fun LockVolumeChangeResult.toFailure(): LockFailure = when (this) {
        LockVolumeChangeResult.Refused -> LockFailure.Refused
        LockVolumeChangeResult.DeviceOffline -> LockFailure.DeviceOffline
        LockVolumeChangeResult.InvalidToken -> LockFailure.SessionExpired
        LockVolumeChangeResult.NetworkUnavailable -> LockFailure.NetworkUnavailable
        is LockVolumeChangeResult.Error, is LockVolumeChangeResult.Done -> LockFailure.Unexpected
    }

    private fun openOrClosed(isOpen: Boolean) = if (isOpen) LockStatus.Open else LockStatus.Closed

    private companion object {
        const val HISTORY_SIZE = 20
    }
}
