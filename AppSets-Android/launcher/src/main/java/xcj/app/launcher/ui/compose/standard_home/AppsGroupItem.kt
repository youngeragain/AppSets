package xcj.app.launcher.ui.compose.standard_home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import xcj.app.launcher.ui.model.StyledAppDefinition
import xcj.app.starter.android.AppDefinition

@Composable
fun AppsGroupItem(
    styledApp: StyledAppDefinition,
    appNameColor: Color,
    onAppClick: (StyledAppDefinition) -> Unit
) {
    val viewModel = viewModel<StandardWindowHomeViewModel>()
    val settings by viewModel.settings
    val space = settings.appCardSpace.dp
    val context = LocalContext.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                onAppClick(styledApp)
            })
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = styledApp.appDefinition.icon,
            contentDescription = styledApp.appDefinition.description,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp)),

            )
        Row(
            modifier = Modifier
                .padding(horizontal = space, vertical = space / 2)
        ) {
            Text(
                text = styledApp.appDefinition.name ?: "",
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = appNameColor,
                fontSize = 14.sp,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}