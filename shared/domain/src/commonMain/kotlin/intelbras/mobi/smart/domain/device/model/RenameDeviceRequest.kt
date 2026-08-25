package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RenameDeviceRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("novoNome") val newName: String,
)
