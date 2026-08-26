package intelbras.mobi.smart.ui.feature.lock

import intelbras.mobi.smart.business.usecase.LockOpening
import intelbras.mobi.smart.business.usecase.LockOpeningWay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.datetime.LocalDateTime

class LockOpeningItemMapperTest {

    @Test
    fun `shows the moment of the opening in the local format`() {
        val opening = openingAt(LocalDateTime(2026, 8, 25, 17, 21, 7))

        assertEquals("25/08/2026 17:21", opening.toUiModels().single().happenedAt)
    }

    @Test
    fun `pads day month and hour so the list stays aligned`() {
        val opening = openingAt(LocalDateTime(2026, 1, 5, 9, 4, 0))

        assertEquals("05/01/2026 09:04", opening.toUiModels().single().happenedAt)
    }

    @Test
    fun `an opening without a readable moment shows a dash`() {
        val opening = openingAt(null)

        assertEquals("—", opening.toUiModels().single().happenedAt)
    }

    @Test
    fun `keeps the name of a way the app does not recognize`() {
        val openings = listOf(
            LockOpening(
                happenedAt = LocalDateTime(2026, 8, 25, 17, 21, 7),
                user = "Cassiano",
                way = LockOpeningWay.Unrecognized("senha"),
            )
        )

        assertEquals(
            LockOpeningWayUiModel.Unrecognized("senha"),
            openings.toUiModels().single().way,
        )
    }

    @Test
    fun `two openings at the same moment keep different identities`() {
        val moment = LocalDateTime(2026, 8, 25, 17, 21, 7)
        val openings = openingAt(moment) + openingAt(moment)

        val ids = openings.toUiModels().map { it.id }

        assertEquals(ids.distinct(), ids)
    }

    private fun openingAt(moment: LocalDateTime?) = listOf(
        LockOpening(happenedAt = moment, user = "APP", way = LockOpeningWay.RemoteApp)
    )
}
