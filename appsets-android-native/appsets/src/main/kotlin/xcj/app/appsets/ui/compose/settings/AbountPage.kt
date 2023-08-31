package xcj.app.appsets.ui.compose.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import xcj.app.appsets.R
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.compose_share.compose.BackActionTopBar

@Composable
fun AboutPage(
    tabVisibilityState: MutableState<Boolean>,
    updateHistory: List<UpdateCheckResult>,
    onBackAction: () -> Unit,
    onHistoryExpandStateChanged: (Boolean) -> Unit,
    onDispose: () -> Unit
) {
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
            onDispose()
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    Column {
        BackActionTopBar(
            onBackAction = onBackAction,
            backButtonRightText = "关于AppSets"
        )
        var historyExpandState by remember {
            mutableStateOf(false)
        }
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(1f)
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        historyExpandState = !historyExpandState
                        onHistoryExpandStateChanged(historyExpandState)
                    }
                    .padding(12.dp)) {
                Text(text = "版本更新历史")
                Spacer(modifier = Modifier.weight(1f))
                val resId = if (historyExpandState) {
                    R.drawable.baseline_keyboard_arrow_up_24
                } else {
                    R.drawable.baseline_keyboard_arrow_down_24
                }
                Icon(
                    painter = painterResource(id = resId),
                    contentDescription = "expand"
                )
            }
            AnimatedVisibility(visible = historyExpandState) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .verticalScroll(rememberScrollState())
                        .animateContentSize()
                ) {
                    updateHistory.forEach {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "版本:${it.newestVersion}")
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(text = it.updateChangesHtml ?: "")
                        }
                        Divider(
                            modifier = Modifier.height(0.5.dp),
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(modifier = Modifier.height(128.dp))
                }
            }
        }
    }
}