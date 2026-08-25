package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class DeviceOriginFilter {
    @SerialName("todos")
    All,

    @SerialName("vinculados")
    Linked,

    @SerialName("compartilhados")
    Shared,
}
