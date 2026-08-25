package intelbras.mobi.smart.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import mibosmart.shared.generated.resources.Res
import mibosmart.shared.generated.resources.compose_multiplatform
import org.jetbrains.compose.resources.painterResource
import intelbras.mobi.smart.ui.theme.MiboTheme

@Composable
fun MiboWordmark(
    name: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Icon(
            painter = painterResource(Res.drawable.compose_multiplatform),
            contentDescription = "Mibo Logo",
            modifier = Modifier.size(34.dp),
            tint = MiboTheme.colors.primary,
        )
        Text(
            text = name,
            style = MiboTheme.typography.title,
            color = MiboTheme.colors.text,
        )
    }
}

@Preview
@Composable
private fun MiboWordmarkPreview() {
    MiboWordmark(name = "MiboSmart")
}
