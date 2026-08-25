package intelbras.mobi.smart.ui.feature.devices

import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.DeviceCatalog
import intelbras.mobi.smart.business.usecase.DeviceListResult
import intelbras.mobi.smart.domain.device.model.DeviceKind as DomainDeviceKind
import intelbras.mobi.smart.domain.device.model.DeviceListQuery
import intelbras.mobi.smart.domain.device.model.DeviceOriginFilter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DeviceListViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads the devices as soon as the screen opens`() = runTest(testDispatcher) {
        val catalog = catalogReturning(DeviceListResult.Success(listOf(device())))
        val viewModel = DeviceListViewModel(catalog)

        assertTrue(viewModel.uiState.value.isLoading)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertEquals(listOf("Câmera da sala"), state.devices.map { it.name })
        verifySuspend {
            catalog.listDevices(DeviceOriginFilter.All, DeviceListQuery.FIRST_PAGE, DeviceListQuery.DEFAULT_PAGE_SIZE)
        }
    }

    @Test
    fun `shows every device returned by the platform`() = runTest(testDispatcher) {
        val devices = listOf(
            device(serialNumber = "SERIAL-1", name = "Câmera da sala"),
            device(serialNumber = "SERIAL-2", name = "Fechadura da porta", kind = DomainDeviceKind.Unknown),
        )
        val viewModel = DeviceListViewModel(catalogReturning(DeviceListResult.Success(devices)))

        testScheduler.advanceUntilIdle()

        assertEquals(listOf("SERIAL-1", "SERIAL-2"), viewModel.uiState.value.devices.map { it.id })
    }

    @Test
    fun `explains that the account has no devices`() = runTest(testDispatcher) {
        val viewModel = DeviceListViewModel(catalogReturning(DeviceListResult.Empty))

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertTrue(state.isEmpty)
        assertTrue(state.devices.isEmpty())
    }

    @Test
    fun `asks for a new token when the platform refuses the current one`() = runTest(testDispatcher) {
        val viewModel = DeviceListViewModel(catalogReturning(DeviceListResult.InvalidToken))

        testScheduler.advanceUntilIdle()

        assertEquals(DeviceListFailure.ExpiredSession, viewModel.uiState.value.failure)
    }

    @Test
    fun `reports that the platform is unreachable`() = runTest(testDispatcher) {
        val viewModel = DeviceListViewModel(catalogReturning(DeviceListResult.NetworkUnavailable))

        testScheduler.advanceUntilIdle()

        assertEquals(DeviceListFailure.Network, viewModel.uiState.value.failure)
    }

    @Test
    fun `falls back to the generic message on an unexpected failure`() = runTest(testDispatcher) {
        val failure = DeviceListResult.Error(IllegalStateException("boom"))
        val viewModel = DeviceListViewModel(catalogReturning(failure))

        testScheduler.advanceUntilIdle()

        assertEquals(DeviceListFailure.Unexpected, viewModel.uiState.value.failure)
    }

    @Test
    fun `retrying asks the platform again`() = runTest(testDispatcher) {
        val catalog = catalogReturning(DeviceListResult.NetworkUnavailable)
        val viewModel = DeviceListViewModel(catalog)
        testScheduler.advanceUntilIdle()

        everySuspend {
            catalog.listDevices(any(), any(), any())
        } returns DeviceListResult.Success(listOf(device()))
        viewModel.onRetry()

        assertTrue(viewModel.uiState.value.isLoading)
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.devices.size)
        assertNull(viewModel.uiState.value.failure)
    }

    @Test
    fun `selecting a filter reloads from the first page with that origin`() = runTest(testDispatcher) {
        val catalog = catalogReturning(DeviceListResult.Success(listOf(device())))
        val viewModel = DeviceListViewModel(catalog)
        testScheduler.advanceUntilIdle()

        viewModel.onFilterSelected(DeviceFilter.Shared)
        testScheduler.advanceUntilIdle()

        assertEquals(DeviceFilter.Shared, viewModel.uiState.value.filter)
        verifySuspend {
            catalog.listDevices(DeviceOriginFilter.Shared, DeviceListQuery.FIRST_PAGE, DeviceListQuery.DEFAULT_PAGE_SIZE)
        }
    }

    @Test
    fun `loading more appends the next page to the current list`() = runTest(testDispatcher) {
        val firstPage = List(DeviceListQuery.DEFAULT_PAGE_SIZE) { index -> device(serialNumber = "SERIAL-$index") }
        val secondPage = listOf(device(serialNumber = "SERIAL-NEXT"))
        val catalog = mock<DeviceCatalog> {
            everySuspend { listDevices(any(), any(), any()) } sequentially {
                returns(DeviceListResult.Success(firstPage))
                returns(DeviceListResult.Success(secondPage))
            }
        }
        val viewModel = DeviceListViewModel(catalog)
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasMore)

        viewModel.onLoadMore()
        testScheduler.advanceUntilIdle()

        assertEquals(DeviceListQuery.DEFAULT_PAGE_SIZE + 1, viewModel.uiState.value.devices.size)
        assertFalse(viewModel.uiState.value.hasMore)
    }

    @Test
    fun `does not ask for more while there is nothing else to fetch`() = runTest(testDispatcher) {
        val catalog = catalogReturning(DeviceListResult.Success(listOf(device())))
        val viewModel = DeviceListViewModel(catalog)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasMore)
        viewModel.onLoadMore()
        testScheduler.advanceUntilIdle()

        assertEquals(1, viewModel.uiState.value.devices.size)
    }

    private fun catalogReturning(result: DeviceListResult) = mock<DeviceCatalog> {
        everySuspend { listDevices(any(), any(), any()) } returns result
    }
}
