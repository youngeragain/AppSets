package xcj.app.compose_share.components

import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DesignHDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outline
    )
}

@Composable
fun DesignVDivider(modifier: Modifier = Modifier) {
    VerticalDivider(
        modifier = modifier,
        color = MaterialTheme.colorScheme.outline
    )
}