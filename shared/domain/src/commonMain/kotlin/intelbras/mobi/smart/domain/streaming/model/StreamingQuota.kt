package intelbras.mobi.smart.domain.streaming.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StreamingQuota(
    @SerialName("quota_gb") val quotaGb: Double = 0.0,
    @SerialName("used_gb") val usedGb: Double = 0.0,
    @SerialName("remaining_gb") val remainingGb: Double = 0.0,
)
