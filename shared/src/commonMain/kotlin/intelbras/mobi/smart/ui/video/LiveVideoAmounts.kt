package intelbras.mobi.smart.ui.video

import kotlin.math.round

private const val BYTES_IN_A_MEGABYTE = 1024.0 * 1024.0
private const val ONE_DECIMAL = 10.0

internal fun megabytesOf(bytes: Long): String = withOneDecimal(bytes / BYTES_IN_A_MEGABYTE)

internal fun withOneDecimal(amount: Double): String {
    val rounded = round(amount * ONE_DECIMAL) / ONE_DECIMAL
    val whole = rounded.toLong()
    val decimal = round((rounded - whole) * ONE_DECIMAL).toLong()
    return "$whole,$decimal"
}
