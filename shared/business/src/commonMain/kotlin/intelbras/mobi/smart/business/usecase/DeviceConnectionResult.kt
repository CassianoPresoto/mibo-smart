package intelbras.mobi.smart.business.usecase

sealed interface DeviceConnectionResult {
    data class Connected(val connection: DeviceConnection) : DeviceConnectionResult

    data object NotSupported : DeviceConnectionResult

    data object DeviceOffline : DeviceConnectionResult

    data object QuotaExceeded : DeviceConnectionResult

    data object InvalidToken : DeviceConnectionResult

    data object NetworkUnavailable : DeviceConnectionResult

    data class Error(val cause: Throwable) : DeviceConnectionResult
}
