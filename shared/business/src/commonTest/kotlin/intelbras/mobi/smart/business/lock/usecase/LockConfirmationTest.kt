package intelbras.mobi.smart.business.lock.usecase

import intelbras.mobi.smart.rest.SmartHomeUnknownPlatformErrorException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.test.runTest

@OptIn(ExperimentalCoroutinesApi::class)
class LockConfirmationTest {

    private val confirmation =
        LockConfirmation(LockConfirmationPolicy(attempts = 3, firstWait = 1.seconds))

    @Test
    fun `stops reading as soon as the expected value shows up`() = runTest {
        var readings = 0

        val reading = confirmation.await(expected = "alto") {
            readings++
            "alto"
        }

        assertEquals(LockConfirmation.Reading("alto", confirmed = true), reading)
        assertEquals(1, readings)
    }

    @Test
    fun `gives up after the attempts the policy allows`() = runTest {
        var readings = 0

        val reading = confirmation.await(expected = "alto") {
            readings++
            "baixo"
        }

        assertEquals(LockConfirmation.Reading("baixo", confirmed = false), reading)
        assertEquals(3, readings)
    }

    @Test
    fun `keeps the expected value when nothing could be read`() = runTest {
        val reading = confirmation.await<String>(expected = "alto") {
            error("fechadura ilegível")
        }

        assertEquals(LockConfirmation.Reading("alto", confirmed = false), reading)
    }

    @Test
    fun `gives up at the first reading the platform refuses to answer`() = runTest {
        var readings = 0

        val reading = confirmation.await<String>(expected = "alto") {
            readings++
            throw SmartHomeUnknownPlatformErrorException("HTTP 500: Erro desconhecido")
        }

        assertEquals(LockConfirmation.Reading("alto", confirmed = false), reading)
        assertEquals(1, readings)
    }

    @Test
    fun `waits between readings as the policy tells`() = runTest {
        confirmation.await(expected = "alto") { "baixo" }

        assertEquals(6.seconds.inWholeMilliseconds, testScheduler.currentTime)
    }
}
