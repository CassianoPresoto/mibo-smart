package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.playback.model.PlaybackFailure
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

data class PlaybackRetryPolicy(
    val maxAttempts: Int = DEFAULT_MAX_ATTEMPTS,
    val waitBetweenAttempts: Duration = DEFAULT_WAIT,
) {

    fun retriesAfter(failure: PlaybackFailure): Boolean = failure != PlaybackFailure.Playback

    fun hasAnotherAttemptAfter(attempt: Int): Boolean = attempt < maxAttempts

    fun waitBefore(attempt: Int): Duration = waitBetweenAttempts * attempt

    fun reopensConnectionOn(attempt: Int): Boolean = attempt > RELOAD_ATTEMPT

    companion object {
        const val DEFAULT_MAX_ATTEMPTS = 3
        const val RELOAD_ATTEMPT = 1
        val DEFAULT_WAIT = 2.seconds
    }
}
