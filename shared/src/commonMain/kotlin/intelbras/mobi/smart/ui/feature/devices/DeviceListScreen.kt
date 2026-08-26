package intelbras.mobi.smart.ui.feature.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.component.MiboAccountButton
import intelbras.mobi.smart.ui.component.MiboErrorMark
import intelbras.mobi.smart.ui.component.MiboFeedbackState
import intelbras.mobi.smart.ui.component.MiboFilterChip
import intelbras.mobi.smart.ui.component.MiboSkeletonList
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.devices_account
import mibosmart.shared.generated.resources.devices_empty_action
import mibosmart.shared.generated.resources.devices_empty_body
import mibosmart.shared.generated.resources.devices_empty_title
import mibosmart.shared.generated.resources.devices_eyebrow
import mibosmart.shared.generated.resources.devices_filter_all
import mibosmart.shared.generated.resources.devices_filter_linked
import mibosmart.shared.generated.resources.devices_filter_shared
import mibosmart.shared.generated.resources.devices_loading
import mibosmart.shared.generated.resources.devices_loading_more
import mibosmart.shared.generated.resources.devices_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeviceListScreen(
    uiState: DeviceListUiState,
    onFilterSelected: (DeviceFilter) -> Unit,
    onDeviceClick: (DeviceUiModel) -> Unit,
    onRetry: () -> Unit,
    onLoadMore: () -> Unit,
    onAccountClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MiboTheme.colors.background)
            .statusBarsPadding(),
    ) {
        DeviceListHeader(onAccountClick = onAccountClick)

        DeviceFilterRow(
            selected = uiState.filter,
            onFilterSelected = onFilterSelected,
        )

        Box(Modifier.weight(1f)) {
            when {
                uiState.isLoading -> MiboSkeletonList(
                    label = stringResource(Res.string.devices_loading),
                    modifier = Modifier.padding(horizontal = MiboSmartSize.listPadding),
                    itemCount = 2,
                )

                uiState.failure != null -> MiboFeedbackState(
                    title = stringResource(uiState.failure.titleResource()),
                    body = stringResource(uiState.failure.bodyResource()),
                    actionLabel = stringResource(uiState.failure.actionResource()),
                    onAction = onRetry,
                    mark = { MiboErrorMark() },
                )

                uiState.isEmpty -> MiboFeedbackState(
                    title = stringResource(Res.string.devices_empty_title),
                    body = stringResource(Res.string.devices_empty_body),
                    actionLabel = stringResource(Res.string.devices_empty_action),
                    onAction = onRetry,
                )

                else -> DeviceList(
                    devices = uiState.devices,
                    isLoadingMore = uiState.isLoadingMore,
                    onDeviceClick = onDeviceClick,
                    onLoadMore = onLoadMore,
                )
            }
        }
    }
}

@Composable
private fun DeviceListHeader(onAccountClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = MiboSmartSize.listPadding,
                end = MiboSmartSize.listPadding,
                top = 18.dp,
                bottom = 12.dp,
            ),
        verticalAlignment = Alignment.Bottom,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.devices_eyebrow),
                style = MiboTheme.typography.caption.copy(
                    fontSize = 12.sp,
                    letterSpacing = 0.2.sp,
                ),
                color = MiboTheme.colors.muted,
            )
            Text(
                text = stringResource(Res.string.devices_title),
                style = MiboTheme.typography.display,
                color = MiboTheme.colors.text,
            )
        }
        MiboAccountButton(
            description = stringResource(Res.string.devices_account),
            onClick = onAccountClick,
        )
    }
}

@Composable
private fun DeviceFilterRow(
    selected: DeviceFilter,
    onFilterSelected: (DeviceFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = MiboSmartSize.listPadding)
            .padding(top = 4.dp, bottom = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DeviceFilter.entries.forEach { filter ->
            MiboFilterChip(
                label = stringResource(filter.labelResource()),
                selected = filter == selected,
                onClick = { onFilterSelected(filter) },
            )
        }
    }
}

private const val LOAD_MORE_THRESHOLD = 4

@Composable
private fun DeviceList(
    devices: List<DeviceUiModel>,
    isLoadingMore: Boolean,
    onDeviceClick: (DeviceUiModel) -> Unit,
    onLoadMore: () -> Unit,
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }.collect { layoutInfo ->
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: return@collect
            if (lastVisible >= layoutInfo.totalItemsCount - LOAD_MORE_THRESHOLD) {
                onLoadMore()
            }
        }
    }

    LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = MiboSmartSize.listPadding,
            end = MiboSmartSize.listPadding,
            bottom = 12.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(items = devices, key = { it.id }) { device ->
            DeviceCard(device = device, onClick = onDeviceClick)
        }

        if (isLoadingMore) {
            item(key = "loading-more") {
                DeviceListLoadingMore()
            }
        }
    }
}

@Composable
private fun DeviceListLoadingMore() {
    val description = stringResource(Res.string.devices_loading_more)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
            .semantics { contentDescription = description },
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            strokeWidth = 2.dp,
            color = MiboTheme.colors.primary,
        )
    }
}

private fun DeviceFilter.labelResource(): StringResource = when (this) {
    DeviceFilter.All -> Res.string.devices_filter_all
    DeviceFilter.Linked -> Res.string.devices_filter_linked
    DeviceFilter.Shared -> Res.string.devices_filter_shared
}

private val previewDevices = listOf(
    DeviceUiModel(
        id = "1",
        name = "Câmera da sala",
        serialNumber = "ABC123456",
        kind = DeviceKind.Camera,
        origin = DeviceOrigin.Linked,
        isOnline = true,
        productId = "PRODUTO-1",
        model = "iM5 S",
    ),
    DeviceUiModel(
        id = "2",
        name = "Fechadura da porta",
        serialNumber = "XYZ987654",
        kind = DeviceKind.Lock,
        origin = DeviceOrigin.Shared,
        isOnline = false,
        productId = "PRODUTO-2",
        model = "Smart Lock",
    ),
)

@Preview
@Composable
private fun DeviceListScreenLoadingPreview() {
    MiboTheme {
        DeviceListScreen(
            uiState = DeviceListUiState(isLoading = true),
            onFilterSelected = {},
            onDeviceClick = {},
            onRetry = {},
            onLoadMore = {},
            onAccountClick = {},
        )
    }
}

@Preview
@Composable
private fun DeviceListScreenLoadedPreview() {
    MiboTheme {
        DeviceListScreen(
            uiState = DeviceListUiState(devices = previewDevices),
            onFilterSelected = {},
            onDeviceClick = {},
            onRetry = {},
            onLoadMore = {},
            onAccountClick = {},
        )
    }
}

@Preview
@Composable
private fun DeviceListScreenEmptyPreview() {
    MiboTheme {
        DeviceListScreen(
            uiState = DeviceListUiState(),
            onFilterSelected = {},
            onDeviceClick = {},
            onRetry = {},
            onLoadMore = {},
            onAccountClick = {},
        )
    }
}

@Preview
@Composable
private fun DeviceListScreenFailurePreview() {
    MiboTheme {
        DeviceListScreen(
            uiState = DeviceListUiState(failure = DeviceListFailure.Network),
            onFilterSelected = {},
            onDeviceClick = {},
            onRetry = {},
            onLoadMore = {},
            onAccountClick = {},
        )
    }
}
