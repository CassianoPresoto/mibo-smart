package intelbras.mobi.smart.ui.feature.lock.history

import dev.mokkery.answering.returns
import dev.mokkery.answering.sequentially
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.LockController
import intelbras.mobi.smart.business.usecase.LockHistoryResult
import intelbras.mobi.smart.business.usecase.LockOpening
import intelbras.mobi.smart.business.usecase.LockOpeningWay
import intelbras.mobi.smart.domain.device.model.DeviceReference
import intelbras.mobi.smart.ui.feature.lock.LockFailure
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDateTime

private const val PAGE_SIZE = 20

@OptIn(ExperimentalCoroutinesApi::class)
class OpeningHistoryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private val lock = DeviceReference(
        serialNumber = "08B95FFFFE02116A_OGQ0010782013_sqNzDUSq",
        productId = "3Y2FSCDJ",
    )

    private val clock = FixedClock(Instant.parse("2026-08-26T12:00:00Z"))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `asks for the first page as soon as the screen opens`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockHistoryResult.Loaded(emptyList()))
        val viewModel = OpeningHistoryViewModel(controller, clock)

        viewModel.onScreenOpened(lock)
        assertTrue(viewModel.uiState.value.isLoading)
        testScheduler.advanceUntilIdle()

        verifySuspend { controller.historyOf(lock, PAGE_SIZE) }
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `groups the openings it received by day`() = runTest(testDispatcher) {
        val controller = controllerAnswering(
            LockHistoryResult.Loaded(
                listOf(
                    openingAt(LocalDateTime(2026, 8, 26, 8, 12)),
                    openingAt(LocalDateTime(2026, 8, 25, 19, 47)),
                )
            )
        )
        val viewModel = OpeningHistoryViewModel(controller, clock)

        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        assertEquals(
            listOf(OpeningDayLabel.Today, OpeningDayLabel.Yesterday),
            viewModel.uiState.value.days.map { it.label },
        )
    }

    @Test
    fun `a full page means there may be more to load`() = runTest(testDispatcher) {
        val viewModel = loaded(pageOf(PAGE_SIZE))

        assertTrue(viewModel.uiState.value.canLoadMore)
    }

    @Test
    fun `a page that came short means there is nothing more to load`() = runTest(testDispatcher) {
        val viewModel = loaded(pageOf(PAGE_SIZE - 1))

        assertFalse(viewModel.uiState.value.canLoadMore)
    }

    @Test
    fun `loading more asks for a bigger page`() = runTest(testDispatcher) {
        val controller = mock<LockController> {
            everySuspend { historyOf(any(), any()) } sequentially {
                returns(LockHistoryResult.Loaded(pageOf(PAGE_SIZE)))
                returns(LockHistoryResult.Loaded(pageOf(PAGE_SIZE + 1)))
            }
        }
        val viewModel = OpeningHistoryViewModel(controller, clock)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onLoadMore()
        testScheduler.advanceUntilIdle()

        verifySuspend { controller.historyOf(lock, PAGE_SIZE * 2) }
    }

    @Test
    fun `loading more without anything left asks nothing`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockHistoryResult.Loaded(pageOf(1)))
        val viewModel = OpeningHistoryViewModel(controller, clock)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onLoadMore()
        testScheduler.advanceUntilIdle()

        verifySuspend(VerifyMode.exactly(1)) { controller.historyOf(any(), any()) }
    }

    @Test
    fun `a history the platform cannot bring is reported as unavailable`() = runTest(testDispatcher) {
        val viewModel = loadedWith(LockHistoryResult.Unavailable)

        assertTrue(viewModel.uiState.value.isUnavailable)
        assertEquals(null, viewModel.uiState.value.failure)
    }

    @Test
    fun `a refused token asks for a new one`() = runTest(testDispatcher) {
        val viewModel = loadedWith(LockHistoryResult.InvalidToken)

        assertEquals(LockFailure.SessionExpired, viewModel.uiState.value.failure)
    }

    @Test
    fun `retrying after a failure shows that it is loading again`() = runTest(testDispatcher) {
        val controller = controllerAnswering(LockHistoryResult.NetworkUnavailable)
        val viewModel = OpeningHistoryViewModel(controller, clock)
        viewModel.onScreenOpened(lock)
        testScheduler.advanceUntilIdle()

        viewModel.onRetry()

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.failure)
    }

    private fun pageOf(size: Int) = List(size) { openingAt(LocalDateTime(2026, 8, 26, 8, 12)) }

    private fun openingAt(moment: LocalDateTime) = LockOpening(
        happenedAt = moment,
        user = "APP",
        way = LockOpeningWay.RemoteApp,
    )

    private fun loaded(openings: List<LockOpening>) =
        loadedWith(LockHistoryResult.Loaded(openings))

    private fun loadedWith(result: LockHistoryResult): OpeningHistoryViewModel {
        val viewModel = OpeningHistoryViewModel(controllerAnswering(result), clock)
        viewModel.onScreenOpened(lock)
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    private fun controllerAnswering(result: LockHistoryResult) = mock<LockController> {
        everySuspend { historyOf(any(), any()) } returns result
    }
}

private class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
