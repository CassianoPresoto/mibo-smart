package intelbras.mobi.smart.domain.sensor.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SensorArmedStatus(
    @SerialName("armado") val isArmed: Boolean = false,
)

@Serializable
data class OpeningSensorStatus(
    @SerialName("aberto") val isOpen: Boolean = false,
)

@Serializable
data class ZigbeeSignalStrength(
    @SerialName("sinal") val strength: Int = 0,
)

@Serializable
data class HumidityAndTemperature(
    @SerialName("umidade") val humidity: Double = 0.0,
    @SerialName("temperatura") val temperature: Double = 0.0,
)
