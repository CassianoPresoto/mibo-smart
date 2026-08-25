package intelbras.mobi.smart.domain.camera.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RecordingRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("tipoArmazenamento") val storage: RecordingStorage,
    @SerialName("dataInicio") val startDate: String,
    @SerialName("dataFim") val endDate: String,
    @SerialName("idProduto") val productId: String? = null,
    @SerialName("canalVideo") val videoChannel: Int = DEFAULT_VIDEO_CHANNEL,
    @SerialName("record_gb") val recordGb: Double = DEFAULT_RECORD_GB,
) {
    companion object {
        const val DEFAULT_VIDEO_CHANNEL = 0
        const val DEFAULT_RECORD_GB = 1.0
        const val MAX_INTERVAL_IN_MINUTES = 30
    }
}
