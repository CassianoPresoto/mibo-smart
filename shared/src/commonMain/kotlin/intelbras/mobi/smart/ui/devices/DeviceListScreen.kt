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
                title = { Text(DeviceListTexts.TITLE) },
                actions = {
                    TextButton(onClick = onSignOut) { Text(DeviceListTexts.SIGN_OUT) }
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
        Text(text = DeviceListTexts.LOADING, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun NoDevices(onReload: () -> Unit, contentPadding: PaddingValues) {
    CenteredMessage(contentPadding) {
        Text(text = DeviceListTexts.EMPTY_TITLE, style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            text = DeviceListTexts.EMPTY_MESSAGE,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onReload) { Text(DeviceListTexts.RELOAD) }
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
            text = DeviceListTexts.failureMessage(failure),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onReload) { Text(DeviceListTexts.RETRY) }
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
                text = DeviceListTexts.statusLabel(device.isOnline),
                style = MaterialTheme.typography.labelLarge,
                color = statusColor(device.isOnline),
            )
        }
    }
}

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
