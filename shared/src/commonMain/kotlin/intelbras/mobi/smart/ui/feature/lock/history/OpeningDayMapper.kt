package intelbras.mobi.smart.ui.feature.lock.history

import intelbras.mobi.smart.business.usecase.LockOpening
import intelbras.mobi.smart.ui.feature.lock.formatted
import intelbras.mobi.smart.ui.feature.lock.toUiModel
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus

private const val ONE_DAY = 1

internal fun List<LockOpening>.toDays(today: LocalDate): List<OpeningDayUiModel> =
    mapIndexed { position, opening -> opening to opening.toUiModel(position) }
        .groupBy { (opening, _) -> opening.happenedAt?.date }
        .map { (date, entries) ->
            OpeningDayUiModel(
                label = date.toLabel(today),
                openings = entries.map { (_, uiModel) -> uiModel },
            )
        }

private fun LocalDate?.toLabel(today: LocalDate): OpeningDayLabel = when (this) {
    null -> OpeningDayLabel.Undated
    today -> OpeningDayLabel.Today
    today.minus(ONE_DAY, DateTimeUnit.DAY) -> OpeningDayLabel.Yesterday
    else -> OpeningDayLabel.Day(formatted())
}
