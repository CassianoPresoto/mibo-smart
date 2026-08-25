package intelbras.mobi.smart.ui.devices

sealed interface DeviceListUiState {
    data object Loading : DeviceListUiState

    data class Loaded(val devices: List<DeviceListItem>) : DeviceListUiState

    data object Empty : DeviceListUiState

    data class Failed(val failure: DeviceListFailure) : DeviceListUiState
}

data class DeviceListItem(
    val serialNumber: String,
    val productId: String,
    val name: String,
    val model: String,
    val isOnline: Boolean,
)

sealed interface DeviceListFailure {
    data object InvalidToken : DeviceListFailure

    data object NetworkUnavailable : DeviceListFailure

    data object Unexpected : DeviceListFailure
}
