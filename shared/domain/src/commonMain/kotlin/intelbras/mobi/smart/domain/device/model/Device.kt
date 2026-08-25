package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Device(
    @SerialName("ns") val serialNumber: String,
    @SerialName("nome") val name: String = "",
    @SerialName("modelo") val model: String = "",
    val status: DeviceStatus = DeviceStatus.Unknown,
    @SerialName("origem") val origin: DeviceOrigin = DeviceOrigin.Unknown,
    @SerialName("versao") val firmwareVersion: String = "",
    @SerialName("idProduto") val productId: String = "",
    @SerialName("subdispositivo") val isSubdevice: Boolean = false,
    @SerialName("atualizacaoDisponivel") val updateAvailable: Boolean = false,
    @SerialName("ultimaVezOnline") val lastSeenOnline: String? = null,
) {
    val isOnline: Boolean get() = status == DeviceStatus.Online

    fun serial(): DeviceSerial = DeviceSerial(serialNumber)

    fun reference(): DeviceReference = DeviceReference(serialNumber = serialNumber, productId = productId)
}
