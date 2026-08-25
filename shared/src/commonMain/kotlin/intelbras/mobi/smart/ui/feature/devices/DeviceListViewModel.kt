package intelbras.mobi.smart.ui.feature.devices

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import intelbras.mobi.smart.business.DeviceCatalog
import intelbras.mobi.smart.business.usecase.CatalogDevice
import intelbras.mobi.smart.business.usecase.DeviceListResult
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DeviceListViewModel(
    private val deviceCatalog: DeviceCatalog,
) : ViewModel() {

    private val mutableUiState = MutableStateFlow(DeviceListUiState())
    val uiState: StateFlow<DeviceListUiState> = mutableUiState.asStateFlow()

    private var currentPage = DeviceListQuery.FIRST_PAGE
    private var loadingJob: Job? = null

    init {
        loadPage(page = DeviceListQuery.FIRST_PAGE, replacingDevices = true)
    }

    fun onFilterSelected(filter: DeviceFilter) {
        if (filter == mutableUiState.value.filter) return
        mutableUiState.value = mutableUiState.value.copy(
            filter = filter,
            devices = emptyList(),
            hasMore = false,
        )
        loadPage(page = DeviceListQuery.FIRST_PAGE, replacingDevices = true)
    }

    fun onRetry() = loadPage(page = DeviceListQuery.FIRST_PAGE, replacingDevices = true)

    fun onLoadMore() {
        val state = mutableUiState.value
        if (!state.hasMore || state.isLoading || state.isLoadingMore) return
        loadPage(page = currentPage + 1, replacingDevices = false)
    }

    private fun loadPage(page: Int, replacingDevices: Boolean) {
        loadingJob?.cancel()
        val state = mutableUiState.value
        mutableUiState.value = if (replacingDevices) {
            state.copy(isLoading = true, isLoadingMore = false, failure = null)
        } else {
            state.copy(isLoadingMore = true)
        }
        val filter = mutableUiState.value.filter
        loadingJob = viewModelScope.launch {
            val result = deviceCatalog.listDevices(
                origin = filter.toOriginFilter(),
                page = page,
                pageSize = PAGE_SIZE,
            )
            currentPage = page
            mutableUiState.value = mutableUiState.value.applying(result, replacingDevices)
        }
    }

    private fun DeviceListUiState.applying(
        result: DeviceListResult,
        replacingDevices: Boolean,
    ): DeviceListUiState = when (result) {
        is DeviceListResult.Success -> {
            val mapped = result.devices.toUiModels()
            copy(
                devices = if (replacingDevices) mapped else devices + mapped,
                isLoading = false,
                isLoadingMore = false,
                hasMore = mapped.size >= PAGE_SIZE,
                failure = null,
            )
        }

        DeviceListResult.Empty -> copy(
            devices = if (replacingDevices) emptyList() else devices,
            isLoading = false,
            isLoadingMore = false,
            hasMore = false,
            failure = null,
        )

        DeviceListResult.InvalidToken -> failing(DeviceListFailure.ExpiredSession, replacingDevices)
        DeviceListResult.NetworkUnavailable -> failing(DeviceListFailure.Network, replacingDevices)
        is DeviceListResult.Error -> failing(DeviceListFailure.Unexpected, replacingDevices)
    }

    private fun DeviceListUiState.failing(failure: DeviceListFailure, replacingDevices: Boolean) = copy(
        isLoading = false,
        isLoadingMore = false,
        failure = if (replacingDevices) failure else this.failure,
    )

    private fun DeviceFilter.toOriginFilter(): DeviceOriginFilter = when (this) {
        DeviceFilter.All -> DeviceOriginFilter.All
        DeviceFilter.Linked -> DeviceOriginFilter.Linked
        DeviceFilter.Shared -> DeviceOriginFilter.Shared
    }

    private fun List<CatalogDevice>.toUiModels(): List<DeviceUiModel> = map { it.toUiModel() }

    private companion object {
        const val PAGE_SIZE = DeviceListQuery.DEFAULT_PAGE_SIZE
    }
}
