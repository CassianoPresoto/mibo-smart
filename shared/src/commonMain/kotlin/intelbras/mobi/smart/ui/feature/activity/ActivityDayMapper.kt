package intelbras.mobi.smart.ui.feature.activity

import intelbras.mobi.smart.business.usecase.HomeActivityEntry
import intelbras.mobi.smart.ui.feature.lock.UNKNOWN_TIME
import intelbras.mobi.smart.ui.feature.lock.formattedTime
import intelbras.mobi.smart.ui.feature.lock.history.dayLabelOf
import intelbras.mobi.smart.ui.feature.lock.toUiModel
import kotlinx.datetime.LocalDate

internal fun List<HomeActivityEntry>.toDays(today: LocalDate): List<ActivityDayUiModel> =
    mapIndexed { position, entry -> entry to position }
        .groupBy { (entry, _) -> entry.opening.happenedAt?.date }
        .map { (date, entries) ->
            ActivityDayUiModel(
                label = dayLabelOf(date, today),
                entries = entries.map { (entry, position) -> entry.toUiModel(position) },
            )
        }

private fun HomeActivityEntry.toUiModel(position: Int) = ActivityEntryUiModel(
    id = "$position|${opening.happenedAt?.toString().orEmpty()}|$lockName",
    lockName = lockName,
    time = opening.happenedAt?.formattedTime() ?: UNKNOWN_TIME,
    user = opening.user,
    way = opening.way.toUiModel(),
)
