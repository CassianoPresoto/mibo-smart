package intelbras.mobi.smart.ui.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.DeviceCatalog
import intelbras.mobi.smart.business.usecase.DeviceListResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceListViewModel(
    private val deviceCatalog: DeviceCatalog,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow<DeviceListUiState>(DeviceListUiState.Loading)
    val uiState: StateFlow<DeviceListUiState> = mutableUiState.asStateFlow()

    private var loading: Job? = null

    init {
        loadDevices()
    }

    fun onReload() = loadDevices()

    private fun loadDevices() {
        if (loading?.isActive == true) return

        mutableUiState.value = DeviceListUiState.Loading
        loading = viewModelScope.launch {
            mutableUiState.value = deviceCatalog.listDevices().toUiState()
        }
    }

    private fun DeviceListResult.toUiState(): DeviceListUiState = when (this) {
        is DeviceListResult.Success -> DeviceListUiState.Loaded(devices.map { it.toListItem() })
        DeviceListResult.Empty -> DeviceListUiState.Empty
        DeviceListResult.InvalidToken -> DeviceListUiState.Failed(DeviceListFailure.InvalidToken)
        DeviceListResult.NetworkUnavailable ->
            DeviceListUiState.Failed(DeviceListFailure.NetworkUnavailable)

        is DeviceListResult.Error -> DeviceListUiState.Failed(DeviceListFailure.Unexpected)
    }
}
