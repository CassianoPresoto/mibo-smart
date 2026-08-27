package intelbras.mobi.smart.ui.feature.session

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustive
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.session.SmartHomeSession
import intelbras.mobi.smart.business.session.usecase.SessionStatus
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
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class SessionViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val expiresAt = Instant.fromEpochMilliseconds(1_800_007_200_000)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `says nothing until the stored session is checked`() = runTest(testDispatcher) {
        val viewModel = SessionViewModel(sessionWith(SessionStatus.None))

        assertEquals(null, viewModel.status.value)
    }

    @Test
    fun `reports the session that was stored`() = runTest(testDispatcher) {
        val viewModel = SessionViewModel(sessionWith(SessionStatus.Active(expiresAt)))

        viewModel.onSessionChecked()
        testScheduler.advanceUntilIdle()

        assertEquals(SessionStatus.Active(expiresAt), viewModel.status.value)
    }

    @Test
    fun `reports a session that expired`() = runTest(testDispatcher) {
        val viewModel = SessionViewModel(sessionWith(SessionStatus.Expired))

        viewModel.onSessionChecked()
        testScheduler.advanceUntilIdle()

        assertEquals(SessionStatus.Expired, viewModel.status.value)
    }

    @Test
    fun `checks the stored session only once`() = runTest(testDispatcher) {
        val session = sessionWith(SessionStatus.None)
        val viewModel = SessionViewModel(session)

        viewModel.onSessionChecked()
        testScheduler.advanceUntilIdle()
        viewModel.onSessionChecked()
        testScheduler.advanceUntilIdle()

        verifySuspend(exhaustive) { session.currentStatus() }
    }

    @Test
    fun `signing out leaves no session behind`() = runTest(testDispatcher) {
        val session = sessionWith(SessionStatus.Active(expiresAt))
        everySuspend { session.signOut() } returns Unit
        val viewModel = SessionViewModel(session)

        viewModel.onSignOut()
        testScheduler.advanceUntilIdle()

        assertEquals(SessionStatus.None, viewModel.status.value)
        verifySuspend { session.signOut() }
    }

    private fun sessionWith(status: SessionStatus) = mock<SmartHomeSession> {
        everySuspend { currentStatus() } returns status
    }
}
