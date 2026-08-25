package intelbras.mobi.smart.ui.token

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.SmartHomeSession
import intelbras.mobi.smart.business.usecase.AuthenticationResult
import intelbras.mobi.smart.business.usecase.SessionStatus
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TokenEntryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val expiresAt = Instant.fromEpochMilliseconds(1_800_007_200_000)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `asks for a token when no session was stored`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(SessionStatus.None)

        testScheduler.advanceUntilIdle()

        assertEquals(TokenEntryUiState.AwaitingToken(), viewModel.uiState.value)
    }

    @Test
    fun `restores a session that is still valid`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(SessionStatus.Active(expiresAt))

        testScheduler.advanceUntilIdle()

        assertEquals(TokenEntryUiState.Authenticated(expiresAt), viewModel.uiState.value)
    }

    @Test
    fun `explains that the stored session expired`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(SessionStatus.Expired)

        testScheduler.advanceUntilIdle()

        val state = assertIs<TokenEntryUiState.AwaitingToken>(viewModel.uiState.value)
        assertEquals(TokenEntryFailure.ExpiredSession, state.failure)
    }

    @Test
    fun `submitting is only possible once a token was typed`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(SessionStatus.None)
        testScheduler.advanceUntilIdle()

        assertFalse(awaiting(viewModel).canSubmit)

        viewModel.onTokenChanged("Ot_token")

        assertTrue(awaiting(viewModel).canSubmit)
    }

    @Test
    fun `typing again clears the previous failure`() = runTest(testDispatcher) {
        val viewModel = viewModelWith(SessionStatus.Expired)
        testScheduler.advanceUntilIdle()

        viewModel.onTokenChanged("Ot_new")

        assertEquals(null, awaiting(viewModel).failure)
    }

    @Test
    fun `an accepted token opens the session`() = runTest(testDispatcher) {
        val session = sessionWith(SessionStatus.None)
        everySuspend { session.authenticate(any()) } returns AuthenticationResult.Success(expiresAt)
        val viewModel = TokenEntryViewModel(session)
        testScheduler.advanceUntilIdle()

        viewModel.onTokenChanged("Ot_token")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertEquals(TokenEntryUiState.Authenticated(expiresAt), viewModel.uiState.value)
        verifySuspend { session.authenticate("Ot_token") }
    }

    @Test
    fun `a refused token keeps the form with the invalid token message`() = runTest(testDispatcher) {
        val viewModel = submitting(AuthenticationResult.InvalidToken)

        val state = assertIs<TokenEntryUiState.AwaitingToken>(viewModel.uiState.value)
        assertEquals(TokenEntryFailure.InvalidToken, state.failure)
        assertFalse(state.isSubmitting)
        assertEquals("Ot_token", state.token)
    }

    @Test
    fun `a network failure keeps the form with the offline message`() = runTest(testDispatcher) {
        val viewModel = submitting(AuthenticationResult.NetworkUnavailable)

        val state = assertIs<TokenEntryUiState.AwaitingToken>(viewModel.uiState.value)
        assertEquals(TokenEntryFailure.NetworkUnavailable, state.failure)
    }

    @Test
    fun `an unexpected failure keeps the form with the generic message`() = runTest(testDispatcher) {
        val viewModel = submitting(AuthenticationResult.Error(IllegalStateException("boom")))

        val state = assertIs<TokenEntryUiState.AwaitingToken>(viewModel.uiState.value)
        assertEquals(TokenEntryFailure.Unexpected, state.failure)
    }

    @Test
    fun `signing out brings the token form back`() = runTest(testDispatcher) {
        val session = sessionWith(SessionStatus.Active(expiresAt))
        everySuspend { session.signOut() } returns Unit
        val viewModel = TokenEntryViewModel(session)
        testScheduler.advanceUntilIdle()

        viewModel.onSignOut()
        testScheduler.advanceUntilIdle()

        assertEquals(TokenEntryUiState.AwaitingToken(), viewModel.uiState.value)
        verifySuspend { session.signOut() }
    }

    private fun TestScope.submitting(result: AuthenticationResult): TokenEntryViewModel {
        val session = sessionWith(SessionStatus.None)
        everySuspend { session.authenticate(any()) } returns result
        val viewModel = TokenEntryViewModel(session)
        testScheduler.advanceUntilIdle()

        viewModel.onTokenChanged("Ot_token")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()
        return viewModel
    }

    private fun viewModelWith(status: SessionStatus) = TokenEntryViewModel(sessionWith(status))

    private fun sessionWith(status: SessionStatus) = mock<SmartHomeSession> {
        everySuspend { currentStatus() } returns status
    }

    private fun awaiting(viewModel: TokenEntryViewModel) =
        assertIs<TokenEntryUiState.AwaitingToken>(viewModel.uiState.value)
}
