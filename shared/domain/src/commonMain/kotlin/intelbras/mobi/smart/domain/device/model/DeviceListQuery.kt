package intelbras.mobi.smart.domain.device.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DeviceListQuery(
    @SerialName("pagina") val page: Int = FIRST_PAGE,
    @SerialName("tamanhoPagina") val pageSize: Int = DEFAULT_PAGE_SIZE,
    @SerialName("origem") val origin: DeviceOriginFilter = DeviceOriginFilter.All,
) {
    companion object {
        const val FIRST_PAGE = 1
        const val DEFAULT_PAGE_SIZE = 20
    }
}
