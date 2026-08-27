package intelbras.mobi.smart.ui.feature.lock.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.lock.LockController
import intelbras.mobi.smart.business.lock.usecase.LockHistoryResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.ui.feature.lock.LockFailure
import kotlin.time.Clock
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class OpeningHistoryViewModel(
    private val lockController: LockController,
    private val clock: Clock,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(OpeningHistoryUiState())
    val uiState: StateFlow<OpeningHistoryUiState> = mutableUiState.asStateFlow()

    private var lock: DeviceReference? = null
    private var limit = PAGE_SIZE
    private var work: Job? = null

    fun onScreenOpened(lock: DeviceReference) {
        this.lock = lock
        load()
    }

    fun onRetry() = load()

    fun onLoadMore() {
        if (!mutableUiState.value.canLoadMore) return

        limit += PAGE_SIZE
        load(more = true)
    }

    private fun load(more: Boolean = false) {
        val lock = lock ?: return
        if (work?.isActive == true) return

        mutableUiState.update { state ->
            state.copy(
                isLoading = !more && state.days.isEmpty(),
                isLoadingMore = more,
                failure = null,
            )
        }
        work = viewModelScope.launch {
            val result = lockController.historyOf(lock, limit)
            mutableUiState.update { state -> state.after(result) }
        }
    }

    private fun OpeningHistoryUiState.after(result: LockHistoryResult): OpeningHistoryUiState =
        when (result) {
            is LockHistoryResult.Loaded -> OpeningHistoryUiState(
                days = result.openings.toDays(today()),
                isLoading = false,
                canLoadMore = result.openings.size >= limit,
            )

            LockHistoryResult.Unavailable -> copy(
                isLoading = false,
                isLoadingMore = false,
                isUnavailable = true,
            )

            else -> copy(
                isLoading = false,
                isLoadingMore = false,
                failure = result.toFailure(),
            )
        }

    private fun today() = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private fun LockHistoryResult.toFailure(): LockFailure = when (this) {
        LockHistoryResult.DeviceOffline -> LockFailure.DeviceOffline
        LockHistoryResult.InvalidToken -> LockFailure.SessionExpired
        LockHistoryResult.NetworkUnavailable -> LockFailure.NetworkUnavailable
        LockHistoryResult.Unavailable,
        is LockHistoryResult.Error,
        is LockHistoryResult.Loaded,
        -> LockFailure.Unexpected
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
