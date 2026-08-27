package intelbras.mobi.smart.ui.feature.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.activity.ActivityFeed
import intelbras.mobi.smart.business.activity.usecase.HomeActivityResult
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

class ActivityViewModel(
    private val activityFeed: ActivityFeed,
    private val clock: Clock,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = mutableUiState.asStateFlow()

    private var work: Job? = null

    fun onScreenResumed() = load(quietly = true)

    fun onRetry() = load()

    private fun load(quietly: Boolean = false) {
        if (work?.isActive == true) return

        if (!quietly) {
            mutableUiState.update { state -> state.copy(isLoading = true, failure = null) }
        }
        work = viewModelScope.launch {
            val result = activityFeed.recentActivity(OPENINGS_PER_LOCK)
            mutableUiState.value = result.toUiState()
        }
    }

    private fun HomeActivityResult.toUiState(): ActivityUiState = when (this) {
        is HomeActivityResult.Loaded -> ActivityUiState(
            days = entries.toDays(today()),
            isLoading = false,
        )

        HomeActivityResult.NoLocks -> ActivityUiState(isLoading = false, hasNoLocks = true)
        HomeActivityResult.Unavailable -> ActivityUiState(isLoading = false, isUnavailable = true)
        else -> ActivityUiState(isLoading = false, failure = toFailure())
    }

    private fun today() = clock.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

    private fun HomeActivityResult.toFailure(): LockFailure = when (this) {
        HomeActivityResult.InvalidToken -> LockFailure.SessionExpired
        HomeActivityResult.NetworkUnavailable -> LockFailure.NetworkUnavailable
        HomeActivityResult.NoLocks,
        HomeActivityResult.Unavailable,
        is HomeActivityResult.Error,
        is HomeActivityResult.Loaded,
        -> LockFailure.Unexpected
    }

    private companion object {
        const val OPENINGS_PER_LOCK = 20
    }
}
