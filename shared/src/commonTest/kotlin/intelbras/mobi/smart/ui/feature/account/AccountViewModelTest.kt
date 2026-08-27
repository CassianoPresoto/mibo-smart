package intelbras.mobi.smart.ui.feature.account

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode.Companion.exhaustive
import dev.mokkery.verifySuspend
import intelbras.mobi.smart.business.session.SmartHomeSession
import intelbras.mobi.smart.business.theme.ThemeSettings
import intelbras.mobi.smart.business.account.UserAccount
import intelbras.mobi.smart.business.account.usecase.AccountSummary
import intelbras.mobi.smart.business.account.usecase.AccountSummaryResult
import intelbras.mobi.smart.domain.preferences.model.ThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private val summary = AccountSummary(tokenSuffix = "3F9A", expiresIn = 102.minutes)

    @BeforeTest
    fun setUp() = Dispatchers.setMain(testDispatcher)

    @AfterTest
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun `shows the session that was stored on this device`() = runTest(testDispatcher) {
        val viewModel = viewModel()

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("3F9A", state.tokenSuffix)
        assertEquals(102.minutes, state.expiresIn)
    }

    @Test
    fun `leaves the screen when there is no session to show`() = runTest(testDispatcher) {
        val viewModel = viewModel(result = AccountSummaryResult.SessionMissing)

        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.signedOut)
    }

    @Test
    fun `reports the theme the user had chosen`() = runTest(testDispatcher) {
        val viewModel = viewModel(themeMode = ThemeMode.Dark)

        testScheduler.advanceUntilIdle()

        assertEquals(ThemeMode.Dark, viewModel.uiState.value.themeMode)
    }

    @Test
    fun `turning the switch on asks for the dark theme`() = runTest(testDispatcher) {
        val themeSettings = themeSettings()
        val viewModel = viewModel(themeSettings = themeSettings)

        viewModel.onDarkThemeToggled(true)
        testScheduler.advanceUntilIdle()

        verifySuspend { themeSettings.choose(ThemeMode.Dark) }
    }

    @Test
    fun `turning the switch off asks for the light theme instead of following the system`() =
        runTest(testDispatcher) {
            val themeSettings = themeSettings()
            val viewModel = viewModel(themeSettings = themeSettings)

            viewModel.onDarkThemeToggled(false)
            testScheduler.advanceUntilIdle()

            verifySuspend { themeSettings.choose(ThemeMode.Light) }
        }

    @Test
    fun `signing out leaves no session behind`() = runTest(testDispatcher) {
        val session = session()
        val viewModel = viewModel(session = session)

        viewModel.onSignOut()
        testScheduler.advanceUntilIdle()

        assertTrue(viewModel.uiState.value.signedOut)
        assertFalse(viewModel.uiState.value.isSigningOut)
        verifySuspend { session.signOut() }
    }

    @Test
    fun `signs out only once even when the button is tapped twice`() = runTest(testDispatcher) {
        val session = session()
        val viewModel = viewModel(session = session)

        viewModel.onSignOut()
        viewModel.onSignOut()
        testScheduler.advanceUntilIdle()

        verifySuspend(exhaustive) { session.signOut() }
    }

    private fun viewModel(
        result: AccountSummaryResult = AccountSummaryResult.Success(summary),
        themeMode: ThemeMode = ThemeMode.System,
        themeSettings: ThemeSettings = themeSettings(themeMode),
        session: SmartHomeSession = session(),
    ) = AccountViewModel(
        userAccount = mock<UserAccount> { everySuspend { summary() } returns result },
        themeSettings = themeSettings,
        smartHomeSession = session,
    )

    private fun themeSettings(themeMode: ThemeMode = ThemeMode.System) = mock<ThemeSettings> {
        every { mode } returns MutableStateFlow(themeMode)
        everySuspend { choose(any()) } returns Unit
    }

    private fun session() = mock<SmartHomeSession> {
        everySuspend { signOut() } returns Unit
    }
}
