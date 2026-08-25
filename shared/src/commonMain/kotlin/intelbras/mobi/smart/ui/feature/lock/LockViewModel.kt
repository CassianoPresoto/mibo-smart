package intelbras.mobi.smart.ui.feature.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.LockController
import intelbras.mobi.smart.business.usecase.LockOperationResult
import intelbras.mobi.smart.business.usecase.LockStatusResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
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

    fun onScreenOpened(lock: DeviceReference) {
        this.lock = lock
        readStatus()
    }

    fun onScreenResumed() = readStatus(quietly = true)

    fun onRetry() = readStatus()

    fun onOpen() = switch(open = true)

    fun onClose() = switch(open = false)

    private fun readStatus(quietly: Boolean = false) {
        val lock = lock ?: return
        if (work?.isActive == true) return

        if (!quietly) {
            mutableUiState.update { state ->
                state.copy(status = LockStatus.Checking, failure = null)
            }
        }
        work = viewModelScope.launch {
            mutableUiState.value = lockController.statusOf(lock).toUiState()
        }
    }

    private fun switch(open: Boolean) {
        val lock = lock ?: return
        if (work?.isActive == true) return

        mutableUiState.update { state ->
            state.copy(isSwitching = true, awaitingConfirmation = false, failure = null)
        }
        work = viewModelScope.launch {
            val result = lockController.switch(lock, open)
            mutableUiState.update { state -> state.after(result) }
        }
    }

    private fun LockUiState.after(result: LockOperationResult): LockUiState = when (result) {
        is LockOperationResult.Done -> copy(
            status = openOrClosed(result.isOpen),
            isSwitching = false,
            awaitingConfirmation = !result.confirmed,
            failure = null,
        )

        else -> copy(isSwitching = false, failure = result.toFailure())
    }

    private fun LockOperationResult.toFailure(): LockFailure = when (this) {
        LockOperationResult.Refused -> LockFailure.Refused
        LockOperationResult.DeviceOffline -> LockFailure.DeviceOffline
        LockOperationResult.InvalidToken -> LockFailure.SessionExpired
        LockOperationResult.NetworkUnavailable -> LockFailure.NetworkUnavailable
        is LockOperationResult.Error, is LockOperationResult.Done -> LockFailure.Unexpected
    }

    private fun LockStatusResult.toUiState(): LockUiState = when (this) {
        is LockStatusResult.Known -> LockUiState(status = openOrClosed(isOpen))
        LockStatusResult.DeviceOffline -> failedWith(LockFailure.DeviceOffline)
        LockStatusResult.InvalidToken -> failedWith(LockFailure.SessionExpired)
        LockStatusResult.NetworkUnavailable -> failedWith(LockFailure.NetworkUnavailable)
        is LockStatusResult.Error -> failedWith(LockFailure.Unexpected)
    }

    private fun openOrClosed(isOpen: Boolean) = if (isOpen) LockStatus.Open else LockStatus.Closed

    private fun failedWith(failure: LockFailure) =
        LockUiState(status = LockStatus.Unknown, failure = failure)

}
