package intelbras.mobi.smart.domain.camera.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoStreamRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("stream_gb") val streamGb: Double = DEFAULT_STREAM_GB,
    @SerialName("canalVideo") val videoChannel: Int = MAIN_VIDEO_CHANNEL,
    @SerialName("streamId") val streamProfile: StreamProfile = StreamProfile.Main,
) {
    companion object {
        const val DEFAULT_STREAM_GB = 1.0
        const val MAIN_VIDEO_CHANNEL = 0
    }
}
