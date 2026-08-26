package intelbras.mobi.smart.ui.feature.lock.history

import intelbras.mobi.smart.business.usecase.LockOpening
import intelbras.mobi.smart.business.usecase.LockOpeningWay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class OpeningDayMapperTest {

    private val today = LocalDate(2026, 8, 26)

    @Test
    fun `openings of the current day are grouped under today`() {
        val days = listOf(openingAt(LocalDateTime(2026, 8, 26, 8, 12))).toDays(today)

        assertEquals(OpeningDayLabel.Today, days.single().label)
    }

    @Test
    fun `openings of the day before are grouped under yesterday`() {
        val days = listOf(openingAt(LocalDateTime(2026, 8, 25, 19, 47))).toDays(today)

        assertEquals(OpeningDayLabel.Yesterday, days.single().label)
    }

    @Test
    fun `older openings are grouped under their own date`() {
        val days = listOf(openingAt(LocalDateTime(2026, 8, 24, 14, 22))).toDays(today)

        assertEquals(OpeningDayLabel.Day("24/08/2026"), days.single().label)
    }

    @Test
    fun `openings of the same day stay in one group in the order they arrived`() {
        val days = listOf(
            openingAt(LocalDateTime(2026, 8, 26, 8, 12)),
            openingAt(LocalDateTime(2026, 8, 26, 7, 38)),
        ).toDays(today)

        val group = days.single()
        assertEquals(2, group.openings.size)
        assertEquals(listOf("08:12", "07:38"), group.openings.map { it.time })
    }

    @Test
    fun `each day becomes its own group`() {
        val days = listOf(
            openingAt(LocalDateTime(2026, 8, 26, 8, 12)),
            openingAt(LocalDateTime(2026, 8, 25, 19, 47)),
            openingAt(LocalDateTime(2026, 8, 24, 14, 22)),
        ).toDays(today)

        assertEquals(
            listOf(OpeningDayLabel.Today, OpeningDayLabel.Yesterday, OpeningDayLabel.Day("24/08/2026")),
            days.map { it.label },
        )
    }

    @Test
    fun `an opening without a readable moment gets its own group`() {
        val days = listOf(openingAt(null)).toDays(today)

        assertEquals(OpeningDayLabel.Undated, days.single().label)
    }

    @Test
    fun `openings in the same day at the same time keep different identities`() {
        val moment = LocalDateTime(2026, 8, 26, 8, 12)
        val days = listOf(openingAt(moment), openingAt(moment)).toDays(today)

        val ids = days.single().openings.map { it.id }
        assertEquals(ids.distinct(), ids)
    }

    private fun openingAt(moment: LocalDateTime?) = LockOpening(
        happenedAt = moment,
        user = "APP",
        way = LockOpeningWay.RemoteApp,
    )
}
