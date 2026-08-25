package intelbras.mobi.smart.domain.auth.model

import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

data class AccessToken(
    val value: String,
    val expiresAt: Instant,
) {
    fun isValidAt(instant: Instant): Boolean = instant < expiresAt

    companion object {
        val LIFETIME: Duration = 2.hours

        fun issuedAt(value: String, issuedAt: Instant): AccessToken =
            AccessToken(value = value, expiresAt = issuedAt + LIFETIME)
    }
}
