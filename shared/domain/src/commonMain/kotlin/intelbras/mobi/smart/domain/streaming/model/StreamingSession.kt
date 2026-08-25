package intelbras.mobi.smart.domain.streaming.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamingSession(
    @SerialName("session_id") val sessionId: String = "",
    @SerialName("bytes_consumed") val bytesConsumed: Long = 0L,
    @SerialName("quota_remaining_gb") val quotaRemainingGb: Double = 0.0,
    @SerialName("is_active") val isActive: Boolean = false,
    @SerialName("quota_exceeded") val quotaExceeded: Boolean = false,
)
