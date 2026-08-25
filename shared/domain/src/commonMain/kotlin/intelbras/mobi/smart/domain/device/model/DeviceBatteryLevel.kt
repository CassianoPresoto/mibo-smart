package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceBatteryLevel(
    @SerialName("bateria") val percentage: Int = 0,
)
