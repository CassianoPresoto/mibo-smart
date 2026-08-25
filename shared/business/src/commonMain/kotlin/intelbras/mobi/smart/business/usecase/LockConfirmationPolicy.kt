package intelbras.mobi.smart.business.usecase

import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class LockConfirmationPolicy(
    val attempts: Int = DEFAULT_ATTEMPTS,
    val firstWait: Duration = DEFAULT_FIRST_WAIT,
) {

    fun waitBefore(attempt: Int): Duration = firstWait * attempt

    companion object {
        const val DEFAULT_ATTEMPTS = 3
        val DEFAULT_FIRST_WAIT = 1.seconds
    }
}
