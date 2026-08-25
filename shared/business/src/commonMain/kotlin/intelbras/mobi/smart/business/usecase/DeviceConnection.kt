package intelbras.mobi.smart.business.usecase

sealed interface DeviceConnection {
    data class LiveVideo(val session: LiveVideoSession) : DeviceConnection
}

data class LiveVideoSession(
    val streamUrl: String,
    val sessionId: String,
    val quotaGb: Double,
)
