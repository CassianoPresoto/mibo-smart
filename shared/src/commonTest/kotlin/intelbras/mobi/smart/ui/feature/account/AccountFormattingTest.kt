package intelbras.mobi.smart.ui.feature.account

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class AccountFormattingTest {

    @Test
    fun `hides everything but the end of the token`() {
        assertEquals("••••••••3F9A", maskedToken("3F9A"))
    }

    @Test
    fun `shows hours and minutes while the session still has an hour`() {
        assertEquals("1h 42min", formattedTimeLeft(1.hours + 42.minutes))
    }

    @Test
    fun `drops the hours once they are gone`() {
        assertEquals("42min", formattedTimeLeft(42.minutes))
    }

    @Test
    fun `keeps the zero minutes of a round hour`() {
        assertEquals("2h 0min", formattedTimeLeft(2.hours))
    }

    @Test
    fun `warns about the last minute instead of showing zero`() {
        assertEquals("<1min", formattedTimeLeft(30.seconds))
    }

    @Test
    fun `warns about the last minute for a session already over`() {
        assertEquals("<1min", formattedTimeLeft(ZERO))
    }
}
