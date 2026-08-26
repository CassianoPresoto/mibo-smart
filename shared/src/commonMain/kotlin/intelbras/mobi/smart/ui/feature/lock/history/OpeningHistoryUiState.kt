package intelbras.mobi.smart.ui.feature.lock.history

import intelbras.mobi.smart.ui.feature.lock.LockFailure
import intelbras.mobi.smart.ui.feature.lock.LockOpeningUiModel

data class OpeningHistoryUiState(
    val days: List<OpeningDayUiModel> = emptyList(),
    val isLoading: Boolean = true,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = false,
    val isUnavailable: Boolean = false,
    val failure: LockFailure? = null,
) {
    val isEmpty: Boolean
        get() = days.isEmpty() && !isLoading && !isUnavailable && failure == null
}

data class OpeningDayUiModel(
    val label: OpeningDayLabel,
    val openings: List<LockOpeningUiModel>,
)

sealed interface OpeningDayLabel {
    data object Today : OpeningDayLabel

    data object Yesterday : OpeningDayLabel

    data class Day(val date: String) : OpeningDayLabel

    data object Undated : OpeningDayLabel
}
