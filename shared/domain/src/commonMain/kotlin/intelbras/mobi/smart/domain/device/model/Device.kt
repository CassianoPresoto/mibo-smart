package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

private const val SUBDEVICE_ADDRESS_SEPARATOR = "_"

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
    @SerialName("dispositivoPai") val hubSerialNumber: String = "",
    @SerialName("idProdutoDispositivoPai") val hubProductId: String = "",
    @SerialName("atualizacaoDisponivel") val updateAvailable: Boolean = false,
    @SerialName("ultimaVezOnline") val lastSeenOnline: String? = null,
) {
    val isOnline: Boolean get() = status == DeviceStatus.Online

    val address: String
        get() = if (hangsOnAHub()) {
            listOf(serialNumber, hubSerialNumber, hubProductId).joinToString(SUBDEVICE_ADDRESS_SEPARATOR)
        } else {
            serialNumber
        }

    fun serial(): DeviceSerial = DeviceSerial(address)

    fun reference(): DeviceReference = DeviceReference(serialNumber = address, productId = productId)

    private fun hangsOnAHub(): Boolean = isSubdevice && hubSerialNumber.isNotBlank()
}
