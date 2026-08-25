package intelbras.mobi.smart.ui.devices

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.DeviceCatalog
import intelbras.mobi.smart.business.usecase.DeviceListResult
import intelbras.mobi.smart.domain.device.model.DeviceKind
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
import kotlin.test.assertIs

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

        assertEquals(DeviceListUiState.Loading, viewModel.uiState.value)
        testScheduler.advanceUntilIdle()

        val state = assertIs<DeviceListUiState.Loaded>(viewModel.uiState.value)
        assertEquals(listOf("Câmera da sala"), state.devices.map { it.name })
        verifySuspend { catalog.listDevices(any(), any(), any()) }
    }

    @Test
    fun `shows every device returned by the platform`() = runTest(testDispatcher) {
        val devices = listOf(
            device(serialNumber = "SERIAL-1", name = "Câmera da sala"),
            device(serialNumber = "SERIAL-2", name = "Fechadura da porta", kind = DeviceKind.Unknown),
        )
        val viewModel = DeviceListViewModel(catalogReturning(DeviceListResult.Success(devices)))

        testScheduler.advanceUntilIdle()

        val state = assertIs<DeviceListUiState.Loaded>(viewModel.uiState.value)
        assertEquals(listOf("SERIAL-1", "SERIAL-2"), state.devices.map { it.serialNumber })
    }

    @Test
    fun `explains that the account has no devices`() = runTest(testDispatcher) {
        val viewModel = DeviceListViewModel(catalogReturning(DeviceListResult.Empty))

        testScheduler.advanceUntilIdle()

        assertEquals(DeviceListUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun `asks for a new token when the platform refuses the current one`() = runTest(testDispatcher) {
        val viewModel = DeviceListViewModel(catalogReturning(DeviceListResult.InvalidToken))

        testScheduler.advanceUntilIdle()

        assertEquals(failing(DeviceListFailure.InvalidToken), viewModel.uiState.value)
    }

    @Test
    fun `reports that the platform is unreachable`() = runTest(testDispatcher) {
        val viewModel = DeviceListViewModel(catalogReturning(DeviceListResult.NetworkUnavailable))

        testScheduler.advanceUntilIdle()

        assertEquals(failing(DeviceListFailure.NetworkUnavailable), viewModel.uiState.value)
    }

    @Test
    fun `falls back to the generic message on an unexpected failure`() = runTest(testDispatcher) {
        val failure = DeviceListResult.Error(IllegalStateException("boom"))
        val viewModel = DeviceListViewModel(catalogReturning(failure))

        testScheduler.advanceUntilIdle()

        assertEquals(failing(DeviceListFailure.Unexpected), viewModel.uiState.value)
    }

    @Test
    fun `reloading asks the platform again`() = runTest(testDispatcher) {
        val catalog = catalogReturning(DeviceListResult.NetworkUnavailable)
        val viewModel = DeviceListViewModel(catalog)
        testScheduler.advanceUntilIdle()

        everySuspend {
            catalog.listDevices(any(), any(), any())
        } returns DeviceListResult.Success(listOf(device()))
        viewModel.onReload()

        assertEquals(DeviceListUiState.Loading, viewModel.uiState.value)
        testScheduler.advanceUntilIdle()
        assertIs<DeviceListUiState.Loaded>(viewModel.uiState.value)
    }

    private fun catalogReturning(result: DeviceListResult) = mock<DeviceCatalog> {
        everySuspend { listDevices(any(), any(), any()) } returns result
    }

    private fun failing(failure: DeviceListFailure) = DeviceListUiState.Failed(failure)
}
