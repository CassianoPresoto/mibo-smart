package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DeviceStatus {
    @SerialName("online")
    Online,

    @SerialName("offline")
    Offline,

    Unknown,
}
