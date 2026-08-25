package intelbras.mobi.smart.business.usecase

sealed interface StreamingUsageResult {
    data class Measured(val usage: StreamingUsage) : StreamingUsageResult

    data object Unavailable : StreamingUsageResult
}

data class StreamingUsage(
    val consumedBytes: Long,
    val remainingQuotaGb: Double,
    val isActive: Boolean,
    val quotaExceeded: Boolean,
)
