package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceSerial(
    @SerialName("ns") val serialNumber: String,
)
