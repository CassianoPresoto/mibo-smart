package intelbras.mobi.smart.business.usecase

import intelbras.mobi.smart.domain.device.model.DeviceListPage

sealed interface DeviceListResult {
    data class Success(val page: DeviceListPage) : DeviceListResult

    data object Empty : DeviceListResult

    data object InvalidToken : DeviceListResult

    data object NetworkUnavailable : DeviceListResult

    data class Error(val cause: Throwable) : DeviceListResult
}
