package intelbras.mobi.smart.ui.feature.activity

import intelbras.mobi.smart.ui.feature.lock.LockFailure
import intelbras.mobi.smart.ui.feature.lock.LockOpeningWayUiModel
import intelbras.mobi.smart.ui.feature.lock.history.OpeningDayLabel

data class ActivityUiState(
    val days: List<ActivityDayUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val hasNoLocks: Boolean = false,
    val isUnavailable: Boolean = false,
    val failure: LockFailure? = null,
) {
    val isEmpty: Boolean
        get() = days.isEmpty() && !isLoading && !hasNoLocks && !isUnavailable && failure == null
}

data class ActivityDayUiModel(
    val label: OpeningDayLabel,
    val entries: List<ActivityEntryUiModel>,
)

data class ActivityEntryUiModel(
    val id: String,
    val lockName: String,
    val time: String,
    val user: String,
    val way: LockOpeningWayUiModel,
)
