package intelbras.mobi.smart.ui.feature.devices

import intelbras.mobi.smart.domain.device.model.DeviceKind

sealed interface DeviceListUiState {
    data object Loading : DeviceListUiState

    data class Loaded(val devices: List<DeviceListItem>) : DeviceListUiState

    data object Empty : DeviceListUiState

    data class Failed(val failure: DeviceListFailure) : DeviceListUiState
}

data class DeviceListItem(
    val serialNumber: String,
    val address: String,
    val productId: String,
    val name: String,
    val model: String,
    val isOnline: Boolean,
    val kind: DeviceKind,
) {
    val hasScreenOfItsOwn: Boolean
        get() = kind == DeviceKind.Camera || kind == DeviceKind.Lock
}

sealed interface DeviceListFailure {
    data object InvalidToken : DeviceListFailure

    data object NetworkUnavailable : DeviceListFailure

    data object Unexpected : DeviceListFailure
}
