package intelbras.mobi.smart.domain.camera.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoStream(
    val url: String,
    @SerialName("session_id") val sessionId: String = "",
    @SerialName("monitor_url") val monitorUrl: String = "",
    @SerialName("quota_gb") val quotaGb: Double = 0.0,
    val warning: String? = null,
)
