package intelbras.mobi.smart.ui.feature.token

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.session.SmartHomeSession
import intelbras.mobi.smart.business.token.usecase.AuthenticationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
class TokenEntryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val expiresAt = Instant.fromEpochMilliseconds(1_800_007_200_000)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `starts with an empty form`() = runTest(testDispatcher) {
        val viewModel = TokenEntryViewModel(mock<SmartHomeSession>())

        assertEquals(TokenEntryUiState(), viewModel.uiState.value)
    }

    @Test
    fun `submitting is only possible once a token was typed`() = runTest(testDispatcher) {
        val viewModel = TokenEntryViewModel(mock<SmartHomeSession>())

        assertFalse(viewModel.uiState.value.canSubmit)

        viewModel.onTokenChanged("Ot_token")

        assertTrue(viewModel.uiState.value.canSubmit)
    }

    @Test
    fun `typing again clears the previous failure`() = runTest(testDispatcher) {
        val viewModel = submitting(AuthenticationResult.InvalidToken)

        viewModel.onTokenChanged("Ot_new")

        assertEquals(null, viewModel.uiState.value.failure)
    }

    @Test
    fun `an accepted token opens the session`() = runTest(testDispatcher) {
        val session = sessionAnswering(AuthenticationResult.Success(expiresAt))
        val viewModel = TokenEntryViewModel(session)

        viewModel.onTokenChanged("Ot_token")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.isAuthenticated)
        assertFalse(viewModel.uiState.value.isSubmitting)
        verifySuspend { session.authenticate("Ot_token") }
    }

    @Test
    fun `a refused token keeps the form with the invalid token message`() = runTest(testDispatcher) {
        val viewModel = submitting(AuthenticationResult.InvalidToken)

        val state = viewModel.uiState.value
        assertEquals(TokenEntryFailure.InvalidToken, state.failure)
        assertFalse(state.isSubmitting)
        assertFalse(state.isAuthenticated)
        assertEquals("Ot_token", state.token)
    }

    @Test
    fun `an empty token is refused by the session`() = runTest(testDispatcher) {
        val viewModel = submitting(AuthenticationResult.MissingToken)

        assertEquals(TokenEntryFailure.EmptyToken, viewModel.uiState.value.failure)
    }

    @Test
    fun `a network failure keeps the form with the offline message`() = runTest(testDispatcher) {
        val viewModel = submitting(AuthenticationResult.NetworkUnavailable)

        assertEquals(TokenEntryFailure.NetworkUnavailable, viewModel.uiState.value.failure)
    }

    @Test
    fun `an unexpected failure keeps the form with the generic message`() = runTest(testDispatcher) {
        val viewModel = submitting(AuthenticationResult.Error(IllegalStateException("boom")))

        assertEquals(TokenEntryFailure.Unexpected, viewModel.uiState.value.failure)
    }

    @Test
    fun `submitting twice asks the session only once`() = runTest(testDispatcher) {
        val session = sessionAnswering(AuthenticationResult.Success(expiresAt))
        val viewModel = TokenEntryViewModel(session)
        viewModel.onTokenChanged("Ot_token")

        viewModel.onSubmit()
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()

        verifySuspend { session.authenticate("Ot_token") }
    }

    private fun TestScope.submitting(result: AuthenticationResult): TokenEntryViewModel {
        val viewModel = TokenEntryViewModel(sessionAnswering(result))

        viewModel.onTokenChanged("Ot_token")
        viewModel.onSubmit()
        testScheduler.advanceUntilIdle()
        return viewModel
    }

    private fun sessionAnswering(result: AuthenticationResult) = mock<SmartHomeSession> {
        everySuspend { authenticate(any()) } returns result
    }
}
