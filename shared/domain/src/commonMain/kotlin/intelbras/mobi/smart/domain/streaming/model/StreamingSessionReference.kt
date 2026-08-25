package intelbras.mobi.smart.domain.streaming.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamingSessionReference(
    @SerialName("session_id") val sessionId: String,
)
