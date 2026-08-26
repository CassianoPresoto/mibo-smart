package intelbras.mobi.smart.ui.feature.lock

import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.not
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.LockController
import intelbras.mobi.smart.business.usecase.LockHistoryResult
import intelbras.mobi.smart.business.usecase.LockOpening
import intelbras.mobi.smart.business.usecase.LockOpeningWay
import intelbras.mobi.smart.business.usecase.LockOperationResult
import intelbras.mobi.smart.business.usecase.LockStatusResult
import intelbras.mobi.smart.business.usecase.LockVolumeChangeResult
import intelbras.mobi.smart.business.usecase.LockVolumeResult
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.domain.lock.model.LockVolumeLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.LocalDateTime
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
            everySuspend { volumeOf(any()) } returns LockVolumeResult.Known(LockVolumeLevel.Low)
            everySuspend { historyOf(any(), any()) } returns LockHistoryResult.Loaded(emptyList())
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

    @Test
    fun `reads the current volume as soon as the screen opens`() = runTest(testDispatcher) {
        val controller = controllerAnswering(
            result = LockStatusResult.Known(isOpen = false),
            volume = LockVolumeResult.Known(LockVolumeLevel.High),
        )
        val viewModel = LockViewModel(controller)

        viewModel.onScreenOpened(lock)
        assertTrue(viewModel.uiState.value.volume.isReading)
        testScheduler.advanceUntilIdle()

        val volume = viewModel.uiState.value.volume
        assertEquals(LockVolumeLevel.High, volume.level)
        assertFalse(volume.isReading)
        verifySuspend { controller.volumeOf(lock) }
    }

    @Test
    fun `a volume that cannot be read keeps the opening status on the screen`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(
                result = LockStatusResult.Known(isOpen = true),
                volume = LockVolumeResult.NetworkUnavailable,
            )
            val viewModel = LockViewModel(controller)

            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(LockStatus.Open, state.status)
            assertEquals(null, state.volume.level)
            assertEquals(LockFailure.NetworkUnavailable, state.volume.failure)
            assertEquals(null, state.failure)
        }

    @Test
    fun `a volume the platform does not answer shows the remembered level`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(
                result = LockStatusResult.Known(isOpen = false),
                volume = LockVolumeResult.Remembered(LockVolumeLevel.Medium),
            )
            val viewModel = LockViewModel(controller)

            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()

            val volume = viewModel.uiState.value.volume
            assertEquals(LockVolumeLevel.Medium, volume.level)
            assertTrue(volume.isRemembered)
            assertEquals(null, volume.failure)
            assertTrue(volume.canChange)
        }

    @Test
    fun `a remembered volume does not ask for a confirmation the platform will never give`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(
                result = LockStatusResult.Known(isOpen = false),
                volume = LockVolumeResult.Remembered(LockVolumeLevel.Medium),
            )
            everySuspend {
                controller.changeVolume(any(), any())
            } returns LockVolumeChangeResult.Done(level = LockVolumeLevel.High, confirmed = false)
            val viewModel = LockViewModel(controller)
            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()

            viewModel.onVolumeSelected(LockVolumeLevel.High)
            testScheduler.advanceUntilIdle()

            val volume = viewModel.uiState.value.volume
            assertEquals(LockVolumeLevel.High, volume.level)
            assertTrue(volume.isRemembered)
            assertFalse(volume.awaitingConfirmation)
        }

    @Test
    fun `choosing a volume shows the level the lock confirmed`() = runTest(testDispatcher) {
        val controller = controllerAnswering(
            result = LockStatusResult.Known(isOpen = false),
            volume = LockVolumeResult.Known(LockVolumeLevel.Mute),
        )
        everySuspend {
            controller.changeVolume(any(), any())
        } returns LockVolumeChangeResult.Done(level = LockVolumeLevel.High, confirmed = true)
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onVolumeSelected(LockVolumeLevel.High)
        assertTrue(viewModel.uiState.value.volume.isChanging)
        testScheduler.advanceUntilIdle()

        val volume = viewModel.uiState.value.volume
        assertEquals(LockVolumeLevel.High, volume.level)
        assertFalse(volume.isChanging)
        assertFalse(volume.awaitingConfirmation)
        verifySuspend { controller.changeVolume(lock, LockVolumeLevel.High) }
    }

    @Test
    fun `a volume that did not confirm says so without hiding what it reported`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(
                result = LockStatusResult.Known(isOpen = false),
                volume = LockVolumeResult.Known(LockVolumeLevel.Mute),
            )
            everySuspend {
                controller.changeVolume(any(), any())
            } returns LockVolumeChangeResult.Done(level = LockVolumeLevel.Mute, confirmed = false)
            val viewModel = LockViewModel(controller)
            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()

            viewModel.onVolumeSelected(LockVolumeLevel.High)
            testScheduler.advanceUntilIdle()

            val volume = viewModel.uiState.value.volume
            assertEquals(LockVolumeLevel.Mute, volume.level)
            assertTrue(volume.awaitingConfirmation)
            assertEquals(null, volume.failure)
        }

    @Test
    fun `a volume the platform refuses keeps the level and explains`() = runTest(testDispatcher) {
        val controller = controllerAnswering(
            result = LockStatusResult.Known(isOpen = false),
            volume = LockVolumeResult.Known(LockVolumeLevel.Low),
        )
        everySuspend { controller.changeVolume(any(), any()) } returns LockVolumeChangeResult.Refused
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onVolumeSelected(LockVolumeLevel.High)
        testScheduler.advanceUntilIdle()

        val volume = viewModel.uiState.value.volume
        assertEquals(LockVolumeLevel.Low, volume.level)
        assertEquals(LockFailure.Refused, volume.failure)
        assertFalse(volume.isChanging)
    }

    @Test
    fun `a second volume change waits for the one in flight`() = runTest(testDispatcher) {
        val controller = controllerAnswering(
            result = LockStatusResult.Known(isOpen = false),
            volume = LockVolumeResult.Known(LockVolumeLevel.Mute),
        )
        everySuspend {
            controller.changeVolume(any(), any())
        } returns LockVolumeChangeResult.Done(level = LockVolumeLevel.High, confirmed = true)
        val viewModel = LockViewModel(controller)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onVolumeSelected(LockVolumeLevel.High)
        viewModel.onVolumeSelected(LockVolumeLevel.Low)
        testScheduler.advanceUntilIdle()

        verifySuspend(not) { controller.changeVolume(lock, LockVolumeLevel.Low) }
    }

    @Test
    fun `changing the volume does not disturb the command in flight for the door`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(
                result = LockStatusResult.Known(isOpen = false),
                volume = LockVolumeResult.Known(LockVolumeLevel.Mute),
            )
            everySuspend {
                controller.switch(any(), any())
            } returns LockOperationResult.Done(isOpen = true, confirmed = true)
            everySuspend {
                controller.changeVolume(any(), any())
            } returns LockVolumeChangeResult.Done(level = LockVolumeLevel.High, confirmed = true)
            val viewModel = LockViewModel(controller)
            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()

            viewModel.onOpen()
            viewModel.onVolumeSelected(LockVolumeLevel.High)
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(LockStatus.Open, state.status)
            assertEquals(LockVolumeLevel.High, state.volume.level)
        }

    @Test
    fun `retrying the volume after a failure shows that it is reading again`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(
                result = LockStatusResult.Known(isOpen = false),
                volume = LockVolumeResult.DeviceOffline,
            )
            val viewModel = LockViewModel(controller)
            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()

            viewModel.onVolumeRetry()

            val volume = viewModel.uiState.value.volume
            assertTrue(volume.isReading)
            assertEquals(null, volume.failure)
        }

    @Test
    fun `loads the opening history as soon as the screen opens`() = runTest(testDispatcher) {
        val controller = controllerAnswering(
            result = LockStatusResult.Known(isOpen = false),
            history = LockHistoryResult.Loaded(
                listOf(
                    LockOpening(
                        happenedAt = LocalDateTime(2026, 8, 25, 17, 21, 7),
                        user = "APP",
                        way = LockOpeningWay.RemoteApp,
                    )
                )
            ),
        )
        val viewModel = LockViewModel(controller)

        viewModel.onScreenOpened(lock)
        assertTrue(viewModel.uiState.value.history.isLoading)
        testScheduler.advanceUntilIdle()

        val history = viewModel.uiState.value.history
        assertEquals("25/08/2026 17:21", history.openings.single().happenedAt)
        assertFalse(history.isLoading)
        verifySuspend { controller.historyOf(lock, any()) }
    }

    @Test
    fun `a lock without openings shows an empty history instead of a failure`() =
        runTest(testDispatcher) {
            val viewModel = watching(LockStatusResult.Known(isOpen = false))

            val history = viewModel.uiState.value.history
            assertTrue(history.isEmpty)
            assertEquals(null, history.failure)
        }

    @Test
    fun `a history the platform cannot bring keeps the rest of the screen working`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(
                result = LockStatusResult.Known(isOpen = true),
                history = LockHistoryResult.Unavailable,
            )
            val viewModel = LockViewModel(controller)

            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(LockStatus.Open, state.status)
            assertTrue(state.history.isUnavailable)
            assertEquals(null, state.history.failure)
        }

    @Test
    fun `retrying the history after a failure shows that it is loading again`() =
        runTest(testDispatcher) {
            val controller = controllerAnswering(
                result = LockStatusResult.Known(isOpen = false),
                history = LockHistoryResult.NetworkUnavailable,
            )
            val viewModel = LockViewModel(controller)
            viewModel.onScreenOpened(lock)
            testScheduler.advanceUntilIdle()
            assertEquals(LockFailure.NetworkUnavailable, viewModel.uiState.value.history.failure)

            viewModel.onHistoryRetry()

            val history = viewModel.uiState.value.history
            assertTrue(history.isLoading)
            assertEquals(null, history.failure)
        }

    private fun watching(result: LockStatusResult): LockViewModel {
        val viewModel = LockViewModel(controllerAnswering(result))
        viewModel.onScreenOpened(lock)
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    private fun controllerAnswering(
        result: LockStatusResult,
        volume: LockVolumeResult = LockVolumeResult.Known(LockVolumeLevel.Medium),
        history: LockHistoryResult = LockHistoryResult.Loaded(emptyList()),
    ) = mock<LockController> {
        everySuspend { statusOf(any()) } returns result
        everySuspend { volumeOf(any()) } returns volume
        everySuspend { historyOf(any(), any()) } returns history
    }
}
