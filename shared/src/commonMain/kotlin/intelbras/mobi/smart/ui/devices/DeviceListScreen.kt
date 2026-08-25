package intelbras.mobi.smart.ui.devices

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.device_list_empty_message
import mibosmart.shared.generated.resources.device_list_empty_title
import mibosmart.shared.generated.resources.device_list_loading
import mibosmart.shared.generated.resources.device_list_reload
import mibosmart.shared.generated.resources.device_list_retry
import mibosmart.shared.generated.resources.device_list_title
import mibosmart.shared.generated.resources.device_status_offline
import mibosmart.shared.generated.resources.device_status_online
import mibosmart.shared.generated.resources.session_sign_out
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeviceListScreen(
    uiState: DeviceListUiState,
    onReload: () -> Unit,
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(Res.string.device_list_title)) },
                actions = {
                    TextButton(onClick = onSignOut) { Text(stringResource(Res.string.session_sign_out)) }
                },
            )
        },
    ) { contentPadding ->
        when (uiState) {
            DeviceListUiState.Loading -> LoadingDevices(contentPadding)
            is DeviceListUiState.Loaded -> DeviceRows(uiState.devices, contentPadding)
            DeviceListUiState.Empty -> NoDevices(onReload, contentPadding)
            is DeviceListUiState.Failed -> LoadFailure(uiState.failure, onReload, contentPadding)
        }
    }
}

@Composable
private fun LoadingDevices(contentPadding: PaddingValues) {
    CenteredMessage(contentPadding) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text(text = stringResource(Res.string.device_list_loading), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NoDevices(onReload: () -> Unit, contentPadding: PaddingValues) {
    CenteredMessage(contentPadding) {
        Text(text = stringResource(Res.string.device_list_empty_title), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            text = stringResource(Res.string.device_list_empty_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onReload) { Text(stringResource(Res.string.device_list_reload)) }
    }
}

@Composable
private fun LoadFailure(
    failure: DeviceListFailure,
    onReload: () -> Unit,
    contentPadding: PaddingValues,
) {
    CenteredMessage(contentPadding) {
        Text(
            text = stringResource(failure.messageResource()),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onReload) { Text(stringResource(Res.string.device_list_retry)) }
    }
}

@Composable
private fun DeviceRows(devices: List<DeviceListItem>, contentPadding: PaddingValues) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = devices, key = { it.serialNumber }) { device ->
            DeviceRow(device)
        }
    }
}

@Composable
private fun DeviceRow(device: DeviceListItem) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = device.name, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = device.model,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = device.serialNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = stringResource(statusLabel(device.isOnline)),
                style = MaterialTheme.typography.labelLarge,
                color = statusColor(device.isOnline),
            )
        }
    }
}

private fun statusLabel(isOnline: Boolean) =
    if (isOnline) Res.string.device_status_online else Res.string.device_status_offline

@Composable
private fun statusColor(isOnline: Boolean): Color =
    if (isOnline) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline

@Composable
private fun CenteredMessage(
    contentPadding: PaddingValues,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
    }
}
