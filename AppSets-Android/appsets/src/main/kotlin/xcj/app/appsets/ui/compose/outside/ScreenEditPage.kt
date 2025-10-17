package xcj.app.appsets.ui.compose.outside

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.compose_share.components.BackActionTopBar

@Composable
fun ScreenEditPage(
    screenInfo: ScreenInfo?,
    onBackClick: () -> Unit,
    onPublicStateChanged: (Boolean) -> Unit
) {
    var isPublic by remember {
        mutableStateOf(screenInfo?.isPublic == 1)
    }
    val hazeState = rememberHazeState()
    val density = LocalDensity.current
    var backActionBarSize by remember {
        mutableStateOf(IntSize.Zero)
    }
    val backActionsHeight by remember {
        derivedStateOf {
            with(density) {
                backActionBarSize.height.toDp()
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .hazeSource(hazeState)
                .padding(start = 12.dp, end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(
                modifier = Modifier.height(
                    WindowInsets.statusBars.asPaddingValues()
                        .calculateTopPadding() + backActionsHeight + 12.dp
                )
            )
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
            modifier = Modifier.onPlaced {
                backActionBarSize = it.size
            },
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