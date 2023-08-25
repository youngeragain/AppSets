package xcj.app.appsets.ui.compose.outside

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import xcj.app.appsets.server.model.UserScreenInfo
import xcj.app.appsets.ui.compose.BackActionTopBar

@Composable
fun ScreenEditPage(
    tabVisibilityState: MutableState<Boolean>,
    screenInfoState: State<UserScreenInfo?>,
    onBackAction: () -> Unit,
    onPublicStateChanged: (Boolean) -> Unit
) {
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    Column {
        var isPublic by remember {
            mutableStateOf(screenInfoState.value?.isPublic == 1)
        }
        BackActionTopBar(backButtonRightText = "Screen", onBackAction = {
            onBackAction()
            val lastIsPublic = screenInfoState.value?.isPublic == 1
            if (isPublic != lastIsPublic) {
                onPublicStateChanged(isPublic)
            }
        })
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "状态")
            Spacer(modifier = Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth()) {

                val text = if (isPublic) {
                    "公开"
                } else {
                    "私有"
                }
                Text(text = text)
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = isPublic,
                    onCheckedChange = {
                        isPublic = it
                    })
            }
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {

                    }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "删除")
            }

        }
    }
}