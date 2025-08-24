package xcj.app.appsets.ui.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignHDivider

@Composable
fun AboutPage(
    updateHistory: List<UpdateCheckResult>,
    onBackClick: () -> Unit,
    onWebsiteClick: () -> Unit,
    onHistoryExpandStateChanged: (Boolean) -> Unit,
    onDispose: () -> Unit,
) {
    DisposableEffect(key1 = true, effect = {
        onDispose(onDispose)
    })
    Column {
        BackActionTopBar(
            onBackClick = onBackClick,
            backButtonRightText = stringResource(xcj.app.appsets.R.string.about)
        )
        Column(
            modifier = Modifier
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Image(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                contentDescription = null
            )
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        )
                        .clickable(onClick = onWebsiteClick)
                        .padding(12.dp)
                ) {
                    Text(text = stringResource(xcj.app.appsets.R.string.website))
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            Column {
                var historyExpandState by remember {
                    mutableStateOf(false)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        )
                        .clickable {
                            historyExpandState = !historyExpandState
                            onHistoryExpandStateChanged(historyExpandState)
                        }
                        .padding(12.dp)) {
                    Text(text = stringResource(xcj.app.appsets.R.string.version_update_history))
                    Spacer(modifier = Modifier.weight(1f))
                    val rotateState by animateFloatAsState(
                        if (historyExpandState) {
                            -180f
                        } else {
                            0f
                        }
                    )
                    Icon(
                        modifier = Modifier.rotate(rotateState),
                        painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_keyboard_arrow_down_24),
                        contentDescription = "expand"
                    )
                }
                AnimatedVisibility(visible = historyExpandState) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(1f)
                            .animateContentSize()
                    ) {
                        updateHistory.forEach {
                            Text(
                                modifier = Modifier.padding(12.dp),
                                text = "${
                                    stringResource(
                                        xcj.app.appsets.R.string.version_x,
                                        it.newestVersion ?: ""
                                    )
                                }\n${
                                    it.publishDateTime ?: ""
                                }\n${it.updateChangesHtml ?: ""}",
                                fontSize = 12.sp
                            )
                            DesignHDivider()
                        }
                        Spacer(modifier = Modifier.height(128.dp))
                    }
                }
            }
        }
    }
}