package intelbras.mobi.smart.business.lock.usecase

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.delay

private const val FIRST_ATTEMPT = 1

internal class LockConfirmation(
    private val policy: LockConfirmationPolicy,
) {

    suspend fun <T : Any> await(expected: T, read: suspend () -> T): Reading<T> {
        var lastReading: T? = null

        for (attempt in FIRST_ATTEMPT..policy.attempts) {
            delay(policy.waitBefore(attempt))

            when (val reading = readOrGiveUp(read)) {
                is Attempt.Read -> {
                    if (reading.value == expected) return Reading(reading.value, confirmed = true)
                    lastReading = reading.value
                }

                Attempt.Unreadable -> Unit
                Attempt.NotAnswered -> break
            }
        }

        return Reading(lastReading ?: expected, confirmed = false)
    }

    private suspend fun <T : Any> readOrGiveUp(read: suspend () -> T): Attempt<T> = try {
        Attempt.Read(read())
    } catch (cancellation: CancellationException) {
        throw cancellation
    } catch (unreadableLock: Throwable) {
        if (unreadableLock.asLockFailureKind() == LockFailureKind.PlatformFailure) {
            Attempt.NotAnswered
        } else {
            Attempt.Unreadable
        }
    }

    private sealed interface Attempt<out T : Any> {
        data class Read<T : Any>(val value: T) : Attempt<T>

        data object Unreadable : Attempt<Nothing>

        data object NotAnswered : Attempt<Nothing>
    }

    data class Reading<T : Any>(val value: T, val confirmed: Boolean)
}
