package intelbras.mobi.smart.domain.camera.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class RecordingStorage {
    @SerialName("nuvem")
    Cloud,

    @SerialName("local")
    Local,
}
