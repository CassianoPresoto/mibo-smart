package intelbras.mobi.smart.ui.token

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.dp
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.app_name
import mibosmart.shared.generated.resources.token_entry_hide
import mibosmart.shared.generated.resources.token_entry_label
import mibosmart.shared.generated.resources.token_entry_show
import mibosmart.shared.generated.resources.token_entry_submit
import mibosmart.shared.generated.resources.token_entry_submitting
import mibosmart.shared.generated.resources.token_entry_subtitle
import mibosmart.shared.generated.resources.token_entry_where_to_find
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun TokenEntryScreen(
    uiState: TokenEntryUiState.AwaitingToken,
    onTokenChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .safeContentPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.token_entry_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(24.dp))

        var isTokenVisible by rememberSaveable { mutableStateOf(false) }

        OutlinedTextField(
            value = uiState.token,
            onValueChange = onTokenChanged,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(Res.string.token_entry_label)) },
            supportingText = { Text(stringResource(Res.string.token_entry_where_to_find)) },
            isError = uiState.failure != null,
            singleLine = true,
            enabled = !uiState.isSubmitting,
            visualTransformation = if (isTokenVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailingIcon = {
                TextButton(
                    onClick = { isTokenVisible = !isTokenVisible },
                    enabled = uiState.token.isNotEmpty(),
                ) {
                    Text(stringResource(visibilityLabel(isTokenVisible)))
                }
            },
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.None,
                autoCorrectEnabled = false,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Go,
            ),
            keyboardActions = KeyboardActions(onGo = { onSubmit() }),
        )

        uiState.failure?.let { failure ->
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(failure.messageResource()),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onSubmit,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.canSubmit,
        ) {
            Text(stringResource(submitLabel(uiState.isSubmitting)))
        }
    }
}

private fun visibilityLabel(isTokenVisible: Boolean) =
    if (isTokenVisible) Res.string.token_entry_hide else Res.string.token_entry_show

private fun submitLabel(isSubmitting: Boolean) =
    if (isSubmitting) Res.string.token_entry_submitting else Res.string.token_entry_submit
