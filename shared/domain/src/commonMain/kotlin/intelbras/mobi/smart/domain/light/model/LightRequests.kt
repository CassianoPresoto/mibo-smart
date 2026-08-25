package intelbras.mobi.smart.domain.light.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LightPowerRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("ligada") val on: Boolean,
)

@Serializable
data class LightTimerRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("tempo") val seconds: Int,
    @SerialName("ligar") val turnOn: Boolean,
) {
    companion object {
        const val MIN_SECONDS = 1
        const val MAX_SECONDS = 86_400
    }
}

@Serializable
data class LightBrightnessRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("brilho") val brightness: Int,
) {
    companion object {
        const val MIN_BRIGHTNESS = 1
        const val MAX_BRIGHTNESS = 100
    }
}

@Serializable
data class LightContrastRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("contraste") val contrast: Int,
) {
    companion object {
        const val MIN_CONTRAST = 0
        const val MAX_CONTRAST = 100
    }
}

@Serializable
data class LightColorRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("cor") val hue: Int,
) {
    companion object {
        const val MIN_HUE = 0
        const val MAX_HUE = 360
    }
}

@Serializable
data class LightModeRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("modo") val mode: LightMode,
)

@Serializable
data class LightTemperatureRequest(
    @SerialName("ns") val serialNumber: String,
    @SerialName("idProduto") val productId: String,
    @SerialName("temperatura") val temperature: Int = MIN_TEMPERATURE,
) {
    companion object {
        const val MIN_TEMPERATURE = 2_700
        const val MAX_TEMPERATURE = 6_500
    }
}
