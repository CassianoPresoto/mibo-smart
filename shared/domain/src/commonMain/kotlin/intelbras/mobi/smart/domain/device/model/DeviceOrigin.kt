package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DeviceOrigin {
    @SerialName("vinculado")
    Linked,

    @SerialName("compartilhado")
    Shared,

    Unknown,
}
