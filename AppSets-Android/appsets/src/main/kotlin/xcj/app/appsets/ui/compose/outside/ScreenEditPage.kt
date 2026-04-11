package xcj.app.appsets.ui.compose.outside

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer
import xcj.app.compose_share.modifier.hazeSourceIfAvailable
import xcj.app.compose_share.modifier.rememberHazeStateIfAvailable

@Composable
fun ScreenEditPage(
    screenInfo: ScreenInfo?,
    onBackClick: () -> Unit,
    onPublicStateChanged: (Boolean) -> Unit
) {
    var isPublic by remember {
        mutableStateOf(screenInfo?.isPublic == 1)
    }
    val hazeState = rememberHazeStateIfAvailable()
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .hazeSourceIfAvailable(hazeState)
                .padding(start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatusBarWithTopActionBarSpacer()
            Text(text = stringResource(id = xcj.app.appsets.R.string.status))

            Row(modifier = Modifier.fillMaxWidth()) {
                val text = if (isPublic) {
                    xcj.app.appsets.R.string.public_
                } else {
                    xcj.app.appsets.R.string.private_
                }
                Text(text = stringResource(id = text))
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isPublic,
                    onCheckedChange = {
                        isPublic = it
                    })
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {

                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = stringResource(xcj.app.appsets.R.string.delete))
            }

        }
        BackActionTopBar(
            hazeState = hazeState,
            backButtonText = "Screen",
            onBackClick = {
                onBackClick()
                val lastIsPublic = screenInfo?.isPublic == 1
                if (isPublic != lastIsPublic) {
                    onPublicStateChanged(isPublic)
                }
            }
        )
    }
}