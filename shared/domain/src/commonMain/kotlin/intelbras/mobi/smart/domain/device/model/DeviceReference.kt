package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceReference(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
)
