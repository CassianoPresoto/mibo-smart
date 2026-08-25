package intelbras.mobi.smart.ui.feature.video

data class LiveVideoDetails(
    val isExpanded: Boolean = false,
    val isReadingUsage: Boolean = false,
    val sessionId: String = "",
    val quotaGb: Double = 0.0,
    val usage: LiveVideoUsage? = null,
) {
    val hasSession: Boolean get() = sessionId.isNotBlank()
}

data class LiveVideoUsage(
    val consumedBytes: Long,
    val remainingQuotaGb: Double,
    val isSessionActive: Boolean,
)
