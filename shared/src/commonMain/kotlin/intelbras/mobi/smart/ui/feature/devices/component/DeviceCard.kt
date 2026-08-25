package intelbras.mobi.smart.ui.feature.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceUnknown
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import intelbras.mobi.smart.ui.component.MiboCodeChip
import intelbras.mobi.smart.ui.component.MiboOutlinedBadge
import intelbras.mobi.smart.ui.component.MiboStatusLabel
import intelbras.mobi.smart.ui.theme.MiboSmartElevation
import intelbras.mobi.smart.ui.theme.MiboSmartShapes
import intelbras.mobi.smart.ui.theme.MiboSmartSize
import intelbras.mobi.smart.ui.theme.MiboTheme
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.devices_badge_shared
import mibosmart.shared.generated.resources.devices_card_description
import mibosmart.shared.generated.resources.devices_description_with_model
import mibosmart.shared.generated.resources.devices_kind_camera
import mibosmart.shared.generated.resources.devices_kind_light
import mibosmart.shared.generated.resources.devices_kind_lock
import mibosmart.shared.generated.resources.devices_kind_other
import mibosmart.shared.generated.resources.devices_kind_sensor
import mibosmart.shared.generated.resources.devices_no_action
import mibosmart.shared.generated.resources.devices_open
import mibosmart.shared.generated.resources.devices_serial
import mibosmart.shared.generated.resources.devices_status_offline
import mibosmart.shared.generated.resources.devices_status_online
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DeviceCard(
    device: DeviceUiModel,
    onClick: (DeviceUiModel) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MiboTheme.colors
    val statusLabel = stringResource(
        if (device.isOnline) Res.string.devices_status_online else Res.string.devices_status_offline,
    )
    val caption = device.caption()
    val description = stringResource(
        Res.string.devices_card_description,
        device.name,
        caption,
        statusLabel,
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .alpha(if (device.isOnline) 1f else 0.7f)
            .clip(MiboSmartShapes.card)
            .background(colors.surface)
            .border(MiboSmartSize.hairline, colors.outline, MiboSmartShapes.card)
            .clickable(enabled = device.isOpenable) { onClick(device) }
            .clearAndSetSemantics { contentDescription = description }
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        DeviceIconTile(device)

        Column(Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(7.dp),
            ) {
                Text(
                    text = device.name,
                    style = MiboTheme.typography.subtitle,
                    color = colors.text,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (device.origin == DeviceOrigin.Shared) {
                    MiboOutlinedBadge(stringResource(Res.string.devices_badge_shared))
                } else {
                    MiboStatusLabel(label = statusLabel, online = device.isOnline)
                }
            }

            Spacer(Modifier.height(3.dp))
            Text(
                text = caption,
                style = MiboTheme.typography.caption,
                color = colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(Modifier.height(5.dp))
            when {
                !device.kind.isOpenable -> Text(
                    text = stringResource(Res.string.devices_no_action),
                    style = MiboTheme.typography.caption,
                    color = colors.muted,
                )

                device.serialNumber != null -> MiboCodeChip(
                    text = stringResource(Res.string.devices_serial, device.serialNumber),
                )
            }
        }

        if (device.isOpenable) {
            Text(
                text = stringResource(Res.string.devices_open),
                style = MiboTheme.typography.button.copy(fontSize = 13.sp),
                color = colors.primary,
                maxLines = 1,
                modifier = Modifier.align(Alignment.CenterVertically),
            )
        }
    }
}

@Composable
private fun DeviceIconTile(device: DeviceUiModel) {
    val colors = MiboTheme.colors
    val tile = if (device.kind.isOpenable && device.isOnline) {
        colors.primaryTint
    } else {
        colors.codeSurface
    }
    val tint = when {
        !device.isOnline -> colors.muted
        device.kind == DeviceKind.Light -> colors.warning
        device.kind.isOpenable -> colors.primary
        else -> colors.muted
    }

    Box(
        modifier = Modifier
            .size(MiboSmartSize.deviceIcon)
            .clip(MiboSmartShapes.icon)
            .background(tile),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = device.kind.icon(),
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}

private fun DeviceKind.icon(): ImageVector = when (this) {
    DeviceKind.Camera -> Icons.Filled.Videocam
    DeviceKind.Lock -> Icons.Filled.Lock
    DeviceKind.Light -> Icons.Filled.Lightbulb
    DeviceKind.Sensor -> Icons.Filled.Sensors
    DeviceKind.Other -> Icons.Filled.DeviceUnknown
}

@Composable
private fun DeviceUiModel.caption(): String {
    val kindLabel = stringResource(kind.labelResource())
    return if (model.isBlank()) {
        kindLabel
    } else {
        stringResource(Res.string.devices_description_with_model, kindLabel, model)
    }
}

private fun DeviceKind.labelResource() = when (this) {
    DeviceKind.Camera -> Res.string.devices_kind_camera
    DeviceKind.Lock -> Res.string.devices_kind_lock
    DeviceKind.Light -> Res.string.devices_kind_light
    DeviceKind.Sensor -> Res.string.devices_kind_sensor
    DeviceKind.Other -> Res.string.devices_kind_other
}

@Preview
@Composable
private fun DeviceCardOnlinePreview() {
    PreviewCard(
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
    )
}

@Preview
@Composable
private fun DeviceCardOfflinePreview() {
    PreviewCard(
        DeviceUiModel(
            id = "2",
            name = "Fechadura da porta",
            serialNumber = "XYZ987654",
            kind = DeviceKind.Lock,
            origin = DeviceOrigin.Linked,
            isOnline = false,
            productId = "PRODUTO-2",
            model = "Smart Lock",
        ),
    )
}

@Preview
@Composable
private fun DeviceCardSharedPreview() {
    PreviewCard(
        DeviceUiModel(
            id = "3",
            name = "Sensor de presença",
            serialNumber = null,
            kind = DeviceKind.Sensor,
            origin = DeviceOrigin.Shared,
            isOnline = true,
            productId = "PRODUTO-3",
            model = "Motion",
        ),
    )
}

@Preview
@Composable
private fun DeviceCardNoActionPreview() {
    PreviewCard(
        DeviceUiModel(
            id = "4",
            name = "Lâmpada da varanda",
            serialNumber = null,
            kind = DeviceKind.Light,
            origin = DeviceOrigin.Linked,
            isOnline = true,
            productId = "PRODUTO-4",
            model = "Bulb",
        ),
    )
}

@Composable
private fun PreviewCard(device: DeviceUiModel) {
    MiboTheme {
        Surface {
            DeviceCard(
                device = device,
                onClick = {},
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
