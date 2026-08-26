package intelbras.mobi.smart.ui.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import intelbras.mobi.smart.domain.preferences.model.ThemeMode
import intelbras.mobi.smart.ui.component.MiboCard
import intelbras.mobi.smart.ui.component.MiboDangerButton
import intelbras.mobi.smart.ui.component.MiboDetailRow
import intelbras.mobi.smart.ui.component.MiboSwitch
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboSmartSpacing
import intelbras.mobi.smart.ui.theme.MiboTheme
import intelbras.mobi.smart.ui.theme.resolvesToDark
import kotlin.time.Duration.Companion.minutes
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.account_session_expires_in
import mibosmart.shared.generated.resources.account_session_title
import mibosmart.shared.generated.resources.account_session_token
import mibosmart.shared.generated.resources.account_sign_out
import mibosmart.shared.generated.resources.account_theme_subtitle
import mibosmart.shared.generated.resources.account_theme_title
import mibosmart.shared.generated.resources.account_title
import mibosmart.shared.generated.resources.account_value_unavailable
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AccountScreen(
    uiState: AccountUiState,
    onDarkThemeToggled: (Boolean) -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MiboTheme.colors.background)
            .statusBarsPadding(),
    ) {
        AccountHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MiboSmartSize.listPadding)
                .padding(bottom = MiboSmartSpacing.lg),
        ) {
            SessionCard(uiState)
            Spacer(Modifier.height(MiboSmartSpacing.md))
            ThemeCard(
                themeMode = uiState.themeMode,
                onDarkThemeToggled = onDarkThemeToggled,
            )
            Spacer(Modifier.height(MiboSmartSpacing.md))
            MiboDangerButton(
                text = stringResource(Res.string.account_sign_out),
                onClick = onSignOut,
                loading = uiState.isSigningOut,
            )
        }
    }
}

@Composable
private fun AccountHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MiboSmartSize.listPadding,
                end = MiboSmartSize.listPadding,
                top = MiboSmartSpacing.md,
                bottom = MiboSmartSpacing.md,
            ),
        horizontalArrangement = Arrangement.spacedBy(MiboSmartSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = stringResource(Res.string.account_title),
            style = MiboTheme.typography.display,
            color = MiboTheme.colors.text,
        )
    }
}

@Composable
private fun SessionCard(uiState: AccountUiState) {
    val unavailable = stringResource(Res.string.account_value_unavailable)

    MiboCard {
        Text(
            text = stringResource(Res.string.account_session_title),
            style = MiboTheme.typography.subtitle,
            color = MiboTheme.colors.text,
        )
        Spacer(Modifier.height(MiboSmartSpacing.sm))
        MiboDetailRow(
            label = stringResource(Res.string.account_session_token),
            value = if (uiState.hasSession) maskedToken(uiState.tokenSuffix) else unavailable,
            monospace = true,
        )
        MiboDetailRow(
            label = stringResource(Res.string.account_session_expires_in),
            value = uiState.expiresIn?.let(::formattedTimeLeft) ?: unavailable,
        )
    }
}

@Composable
private fun ThemeCard(themeMode: ThemeMode, onDarkThemeToggled: (Boolean) -> Unit) {
    val darkThemeLabel = stringResource(Res.string.account_theme_title)

    MiboCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = darkThemeLabel,
                    style = MiboTheme.typography.subtitle,
                    color = MiboTheme.colors.text,
                )
                Text(
                    text = stringResource(Res.string.account_theme_subtitle),
                    style = MiboTheme.typography.caption,
                    color = MiboTheme.colors.muted,
                )
            }
            Spacer(Modifier.width(MiboSmartSpacing.md))
            MiboSwitch(
                checked = themeMode.resolvesToDark(),
                onCheckedChange = onDarkThemeToggled,
                description = darkThemeLabel,
            )
        }
    }
}

private val previewState = AccountUiState(tokenSuffix = "3F9A", expiresIn = 102.minutes)

@Preview
@Composable
private fun AccountScreenPreview() {
    MiboTheme {
        AccountScreen(
            uiState = previewState,
            onDarkThemeToggled = {},
            onSignOut = {},
        )
    }
}

@Preview
@Composable
private fun AccountScreenDarkPreview() {
    MiboTheme(darkTheme = true) {
        AccountScreen(
            uiState = previewState.copy(themeMode = ThemeMode.Dark),
            onDarkThemeToggled = {},
            onSignOut = {},
        )
    }
}
