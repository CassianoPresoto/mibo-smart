package intelbras.mobi.smart.ui.feature.lock

import intelbras.mobi.smart.business.usecase.LockOpening
import intelbras.mobi.smart.business.usecase.LockOpeningWay
import kotlinx.datetime.LocalDateTime

private const val UNKNOWN_TIME = "—"
private const val TWO_DIGITS = 2
private const val PAD_GLYPH = '0'

internal fun List<LockOpening>.toUiModels(): List<LockOpeningUiModel> =
    mapIndexed { position, opening -> opening.toUiModel(position) }

private fun LockOpening.toUiModel(position: Int) = LockOpeningUiModel(
    id = "$position|${happenedAt?.toString().orEmpty()}|$user",
    happenedAt = happenedAt.formatted(),
    user = user,
    way = way.toUiModel(),
)

private fun LocalDateTime?.formatted(): String {
    if (this == null) return UNKNOWN_TIME
    val date = "${day.padded()}/${monthNumber.padded()}/$year"
    return "$date ${hour.padded()}:${minute.padded()}"
}

private fun Int.padded() = toString().padStart(TWO_DIGITS, PAD_GLYPH)

private fun LockOpeningWay.toUiModel(): LockOpeningWayUiModel = when (this) {
    LockOpeningWay.RemoteApp -> LockOpeningWayUiModel.RemoteApp
    is LockOpeningWay.Unrecognized -> LockOpeningWayUiModel.Unrecognized(name)
}
