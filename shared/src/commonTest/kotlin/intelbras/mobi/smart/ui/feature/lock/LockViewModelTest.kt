package intelbras.mobi.smart.ui.feature.lock

import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.LockController
import intelbras.mobi.smart.business.usecase.LockOperationResult
import intelbras.mobi.smart.business.usecase.LockStatusResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LockViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val lock = DeviceReference(
        serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
        productId = "3Y2FSCDJ",
    )

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `checks the lock as soon as the screen opens`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockStatusResult.Known(isOpen = false))
        val viewModel = LockViewModel(controller)

        viewModel.onScreenOpened(lock)
        assertEquals(LockStatus.Checking, viewModel.uiState.value.status)
        testScheduler.advanceUntilIdle()

        assertEquals(LockStatus.Closed, viewModel.uiState.value.status)
        verifySuspend { controller.statusOf(lock) }
    }

    @Test
    fun `shows a lock that is open`() = runTest(testDispatcher) {
        val viewModel = watching(LockStatusResult.Known(isOpen = true))

        assertEquals(LockStatus.Open, viewModel.uiState.value.status)
    }

    @Test
    fun `a lock that cannot be reached loses its status`() = runTest(testDispatcher) {
        val viewModel = watching(LockStatusResult.DeviceOffline)

        val state = viewModel.uiState.value
        assertEquals(LockStatus.Unknown, state.status)
        assertEquals(LockFailure.DeviceOffline, state.failure)
    }

    @Test
    fun `a refused token asks for a new one`() = runTest(testDispatcher) {
        val viewModel = watching(LockStatusResult.InvalidToken)

        assertEquals(LockFailure.SessionExpired, viewModel.uiState.value.failure)
    }

    @Test
    fun `opening the lock shows the state it confirmed`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockStatusResult.Known(isOpen = false))
        everySuspend {
            controller.switch(any(), any())
        } returns LockOperationResult.Done(isOpen = true, confirmed = true)
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onOpen()
        assertTrue(viewModel.uiState.value.isSwitching)
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LockStatus.Open, state.status)
        assertFalse(state.isSwitching)
        assertFalse(state.awaitingConfirmation)
        verifySuspend { controller.switch(lock, true) }
    }

    @Test
    fun `a lock that did not confirm says so without hiding what it reported`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(LockStatusResult.Known(isOpen = false))
            everySuspend {
                controller.switch(any(), any())
            } returns LockOperationResult.Done(isOpen = false, confirmed = false)
            val viewModel = LockViewModel(controller)
            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()

            viewModel.onOpen()
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(LockStatus.Closed, state.status)
            assertTrue(state.awaitingConfirmation)
            assertEquals(null, state.failure)
        }

    @Test
    fun `closing the lock sends the command the platform expects`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockStatusResult.Known(isOpen = true))
        everySuspend {
            controller.switch(any(), any())
        } returns LockOperationResult.Done(isOpen = false, confirmed = true)
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onClose()
        testScheduler.advanceUntilIdle()

        verifySuspend { controller.switch(lock, false) }
    }

    @Test
    fun `a command the platform refuses keeps the status and explains`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockStatusResult.Known(isOpen = false))
        everySuspend { controller.switch(any(), any()) } returns LockOperationResult.Refused
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onOpen()
        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(LockStatus.Closed, state.status)
        assertEquals(LockFailure.Refused, state.failure)
        assertFalse(state.isSwitching)
    }

    @Test
    fun `a second command waits for the one in flight`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockStatusResult.Known(isOpen = false))
        everySuspend {
            controller.switch(any(), any())
        } returns LockOperationResult.Done(isOpen = true, confirmed = true)
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onOpen()
        viewModel.onClose()
        testScheduler.advanceUntilIdle()

        verifySuspend(not) { controller.switch(lock, false) }
    }

    @Test
    fun `coming back to the screen rereads without erasing what is on it`() = runTest(testDispatcher) {
        val controller = mock<LockController> {
            everySuspend { statusOf(any()) } sequentially {
                returns(LockStatusResult.Known(isOpen = false))
                returns(LockStatusResult.Known(isOpen = true))
            }
        }
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onScreenResumed()
        assertEquals(LockStatus.Closed, viewModel.uiState.value.status)
        testScheduler.advanceUntilIdle()

        assertEquals(LockStatus.Open, viewModel.uiState.value.status)
    }

    @Test
    fun `retrying after a failure shows that it is checking again`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockStatusResult.DeviceOffline)
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onRetry()

        assertEquals(LockStatus.Checking, viewModel.uiState.value.status)
        assertEquals(null, viewModel.uiState.value.failure)
    }

    private fun watching(result: LockStatusResult): LockViewModel {
        val viewModel = LockViewModel(controllerAnswering(result))
        viewModel.onScreenOpened(lock)
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    private fun controllerAnswering(result: LockStatusResult) = mock<LockController> {
        everySuspend { statusOf(any()) } returns result
    }
}
