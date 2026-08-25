package intelbras.mobi.smart.domain.device.model

data class DeviceListPage(
    val page: Int,
    val pageSize: Int,
    val origin: DeviceOriginFilter,
    val devices: List<Device>,
) {
    val isEmpty: Boolean get() = devices.isEmpty()

    val hasNextPage: Boolean get() = devices.size >= pageSize
}
