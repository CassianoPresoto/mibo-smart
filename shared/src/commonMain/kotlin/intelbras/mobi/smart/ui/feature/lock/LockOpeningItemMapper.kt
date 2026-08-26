package intelbras.mobi.smart.ui.feature.lock

import intelbras.mobi.smart.business.usecase.LockOpening
import intelbras.mobi.smart.business.usecase.LockOpeningWay

internal fun List<LockOpening>.toUiModels(): List<LockOpeningUiModel> =
    mapIndexed { position, opening -> opening.toUiModel(position) }

internal fun LockOpening.toUiModel(position: Int) = LockOpeningUiModel(
    id = "$position|${happenedAt?.toString().orEmpty()}|$user",
    happenedAt = happenedAt.formattedMoment(),
    time = happenedAt?.formattedTime() ?: UNKNOWN_TIME,
    user = user,
    way = way.toUiModel(),
)

internal fun LockOpeningWay.toUiModel(): LockOpeningWayUiModel = when (this) {
    LockOpeningWay.RemoteApp -> LockOpeningWayUiModel.RemoteApp
    is LockOpeningWay.Unrecognized -> LockOpeningWayUiModel.Unrecognized(name)
}
