package intelbras.mobi.smart.ui.feature.token

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import intelbras.mobi.smart.ui.component.MiboPrimaryButton
import intelbras.mobi.smart.ui.component.MiboTextField
import intelbras.mobi.smart.ui.component.MiboWordmark
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.app_name
import mibosmart.shared.generated.resources.token_entry_hide
import mibosmart.shared.generated.resources.token_entry_label
import mibosmart.shared.generated.resources.token_entry_session_hint
import mibosmart.shared.generated.resources.token_entry_show
import mibosmart.shared.generated.resources.token_entry_submit
import mibosmart.shared.generated.resources.token_entry_submitting
import mibosmart.shared.generated.resources.token_entry_subtitle
import mibosmart.shared.generated.resources.token_entry_where_to_find
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TokenEntryScreen(
    uiState: TokenEntryUiState,
    sessionExpired: Boolean,
    onTokenChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    val typography = MiboTheme.typography

    var isTokenVisible by rememberSaveable { mutableStateOf(false) }
    val failure = uiState.failureToShow(sessionExpired)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
            .safeContentPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MiboSmartSize.screenPadding, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start,
    ) {
        MiboWordmark(name = stringResource(Res.string.app_name))

        Spacer(Modifier.height(34.dp))
        Text(
            text = stringResource(Res.string.token_entry_subtitle),
            style = typography.body,
            color = colors.muted,
            modifier = Modifier.widthIn(max = 300.dp),
        )

        Spacer(Modifier.height(26.dp))
        Text(
            text = stringResource(Res.string.token_entry_label).uppercase(),
            style = typography.label,
            color = colors.muted,
        )

        Spacer(Modifier.height(8.dp))
        MiboTextField(
            value = uiState.token,
            onValueChange = onTokenChanged,
            enabled = !uiState.isSubmitting,
            isError = failure != null,
            trailingLabel = stringResource(visibilityLabel(isTokenVisible)),
            onTrailingClick = { isTokenVisible = !isTokenVisible },
            trailingEnabled = uiState.token.isNotEmpty(),
            visualTransformation = if (isTokenVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { onSubmit() }),
        )

        failure?.let { shown ->
            Spacer(Modifier.height(10.dp))
            Text(
                text = stringResource(shown.messageResource()),
                style = typography.caption,
                color = colors.danger,
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(Res.string.token_entry_where_to_find),
            style = typography.caption,
            color = colors.muted,
        )

        Spacer(Modifier.height(26.dp))
        MiboPrimaryButton(
            text = stringResource(submitLabel(uiState.isSubmitting)),
            onClick = onSubmit,
            enabled = uiState.canSubmit,
            loading = uiState.isSubmitting,
        )

        Spacer(Modifier.height(18.dp))
        Text(
            text = stringResource(Res.string.token_entry_session_hint),
            style = typography.caption,
            color = colors.muted,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
        )
    }
}

private fun TokenEntryUiState.failureToShow(sessionExpired: Boolean): TokenEntryFailure? =
    failure ?: TokenEntryFailure.ExpiredSession.takeIf { sessionExpired && token.isBlank() }

private fun visibilityLabel(isTokenVisible: Boolean) =
    if (isTokenVisible) Res.string.token_entry_hide else Res.string.token_entry_show

private fun submitLabel(isSubmitting: Boolean) =
    if (isSubmitting) Res.string.token_entry_submitting else Res.string.token_entry_submit

@Preview
@Composable
private fun TokenEntryScreenPreview() {
    PreviewScreen(uiState = TokenEntryUiState(), sessionExpired = false)
}

@Preview
@Composable
private fun TokenEntryScreenFilledPreview() {
    PreviewScreen(
        uiState = TokenEntryUiState(token = "Ot_0001or1c98d0"),
        sessionExpired = false,
    )
}

@Preview
@Composable
private fun TokenEntryScreenExpiredSessionPreview() {
    PreviewScreen(uiState = TokenEntryUiState(), sessionExpired = true)
}

@Preview
@Composable
private fun TokenEntryScreenSubmittingPreview() {
    PreviewScreen(
        uiState = TokenEntryUiState(token = "Ot_0001or1c98d0", isSubmitting = true),
        sessionExpired = false,
    )
}

@Preview
@Composable
private fun TokenEntryScreenRefusedPreview() {
    PreviewScreen(
        uiState = TokenEntryUiState(
            token = "Ot_0001or1c98d0",
            failure = TokenEntryFailure.InvalidToken,
        ),
        sessionExpired = false,
    )
}

@Composable
private fun PreviewScreen(uiState: TokenEntryUiState, sessionExpired: Boolean) {
    MiboTheme {
        TokenEntryScreen(
            uiState = uiState,
            sessionExpired = sessionExpired,
            onTokenChanged = {},
            onSubmit = {},
        )
    }
}