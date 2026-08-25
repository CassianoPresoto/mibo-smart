package intelbras.mobi.smart.business.usecase

sealed interface DeviceListResult {
    data class Success(val devices: List<CatalogDevice>) : DeviceListResult

    data object Empty : DeviceListResult

    data object InvalidToken : DeviceListResult

    data object NetworkUnavailable : DeviceListResult

    data class Error(val cause: Throwable) : DeviceListResult
}
