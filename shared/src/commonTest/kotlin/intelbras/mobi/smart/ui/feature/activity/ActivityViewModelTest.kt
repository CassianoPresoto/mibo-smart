package intelbras.mobi.smart.ui.feature.activity

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.ActivityFeed
import intelbras.mobi.smart.business.usecase.HomeActivityEntry
import intelbras.mobi.smart.business.usecase.HomeActivityResult
import intelbras.mobi.smart.business.usecase.LockOpening
import intelbras.mobi.smart.business.usecase.LockOpeningWay
import intelbras.mobi.smart.ui.feature.lock.LockFailure
import intelbras.mobi.smart.ui.feature.lock.history.OpeningDayLabel
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

@OptIn(ExperimentalCoroutinesApi::class)
class ActivityViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val clock = FixedClock(Instant.parse("2026-08-26T12:00:00Z"))

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `loads the activity when the screen appears`() = runTest(testDispatcher) {
        val feed = feedAnswering(
            HomeActivityResult.Loaded(
                listOf(entry("Porta de entrada", LocalDateTime(2026, 8, 26, 8, 12)))
            )
        )
        val viewModel = ActivityViewModel(feed, clock)

        viewModel.onScreenResumed()
        testScheduler.advanceUntilIdle()

        val day = viewModel.uiState.value.days.single()
        assertEquals(OpeningDayLabel.Today, day.label)
        assertEquals("Porta de entrada", day.entries.single().lockName)
        verifySuspend { feed.recentActivity(any()) }
    }

    @Test
    fun `groups the openings of several locks by day`() = runTest(testDispatcher) {
        val feed = feedAnswering(
            HomeActivityResult.Loaded(
                listOf(
                    entry("Porta", LocalDateTime(2026, 8, 26, 8, 12)),
                    entry("Garagem", LocalDateTime(2026, 8, 25, 19, 47)),
                )
            )
        )
        val viewModel = watching(feed)

        assertEquals(
            listOf(OpeningDayLabel.Today, OpeningDayLabel.Yesterday),
            viewModel.uiState.value.days.map { it.label },
        )
    }

    @Test
    fun `an account without locks says so instead of showing a failure`() = runTest(testDispatcher) {
        val viewModel = watching(feedAnswering(HomeActivityResult.NoLocks))

        val state = viewModel.uiState.value
        assertTrue(state.hasNoLocks)
        assertEquals(null, state.failure)
        assertFalse(state.isEmpty)
    }

    @Test
    fun `locks without openings show an empty activity`() = runTest(testDispatcher) {
        val viewModel = watching(feedAnswering(HomeActivityResult.Loaded(emptyList())))

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `an activity the platform cannot bring is reported as unavailable`() =
        runTest(testDispatcher) {
            val viewModel = watching(feedAnswering(HomeActivityResult.Unavailable))

            assertTrue(viewModel.uiState.value.isUnavailable)
            assertEquals(null, viewModel.uiState.value.failure)
        }

    @Test
    fun `a refused token asks for a new one`() = runTest(testDispatcher) {
        val viewModel = watching(feedAnswering(HomeActivityResult.InvalidToken))

        assertEquals(LockFailure.SessionExpired, viewModel.uiState.value.failure)
    }

    @Test
    fun `retrying after a failure shows that it is loading again`() = runTest(testDispatcher) {
        val viewModel = watching(feedAnswering(HomeActivityResult.NetworkUnavailable))

        viewModel.onRetry()

        assertTrue(viewModel.uiState.value.isLoading)
        assertEquals(null, viewModel.uiState.value.failure)
    }

    private fun entry(lockName: String, moment: LocalDateTime) = HomeActivityEntry(
        lockName = lockName,
        opening = LockOpening(happenedAt = moment, user = "APP", way = LockOpeningWay.RemoteApp),
    )

    private fun watching(feed: ActivityFeed): ActivityViewModel {
        val viewModel = ActivityViewModel(feed, clock)
        viewModel.onScreenResumed()
        testDispatcher.scheduler.advanceUntilIdle()
        return viewModel
    }

    private fun feedAnswering(result: HomeActivityResult) = mock<ActivityFeed> {
        everySuspend { recentActivity(any()) } returns result
    }
}

private class FixedClock(private val instant: Instant) : Clock {
    override fun now(): Instant = instant
}
