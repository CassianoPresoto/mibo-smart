package intelbras.mobi.smart.ui.feature.devices

import androidx.compose.runtime.Immutable

enum class DeviceOrigin { Linked, Shared }

enum class DeviceFilter { All, Linked, Shared }

enum class DeviceKind {
    Camera,
    Lock,
    Light,
    Sensor,
    Other,
    ;

    val isOpenable: Boolean get() = this == Camera || this == Lock
}

@Immutable
data class DeviceUiModel(
    val id: String,
    val name: String,
    val serialNumber: String?,
    val kind: DeviceKind,
    val origin: DeviceOrigin,
    val isOnline: Boolean,
    val productId: String,
    val model: String,
) {
    val isOpenable: Boolean get() = kind.isOpenable && isOnline
}

@Immutable
data class DeviceListUiState(
    val devices: List<DeviceUiModel> = emptyList(),
    val filter: DeviceFilter = DeviceFilter.All,
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = false,
    val failure: DeviceListFailure? = null,
) {
    val isEmpty: Boolean get() = !isLoading && failure == null && devices.isEmpty()

    val showList: Boolean get() = !isLoading && failure == null && devices.isNotEmpty()
}
