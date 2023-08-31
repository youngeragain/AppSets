package xcj.app.appsets.ui.compose.settings

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.compose_share.compose.BackActionTopBar


@Preview(showBackground = true)
@Composable
fun SettingsPagePreView() {
    SettingsPage(mutableStateOf(false), {}, {}, {})
}

@Composable
fun SettingsPage(
    tabVisibilityState: MutableState<Boolean>,
    onBackClick: () -> Unit,
    onAboutClick: () -> Unit,
    onAddInClick: () -> Unit
) {
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    Column() {
        BackActionTopBar(backButtonRightText = "设置", onBackAction = onBackClick)
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        )
        {
            Text(
                text = "会话设置",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "聊天气泡方向", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = MaterialTheme.colorScheme.secondary
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color)
                }
                Text(text = "左右交错", fontSize = 13.sp, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "全部靠左", fontSize = 13.sp, modifier = Modifier.padding(10.dp))
            }

            Text(text = "数据发送方式", fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                val color = MaterialTheme.colorScheme.secondary
                Canvas(modifier = Modifier.size(10.dp)) {
                    drawCircle(color)
                }
                Text(text = "中转发送", fontSize = 13.sp, modifier = Modifier.padding(10.dp))
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row {
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = "直接发送", fontSize = 13.sp, modifier = Modifier.padding(10.dp))
            }
            Text(
                text = "加载项",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Column {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onAddInClick),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "查看",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                    )
                }
            }
            Text(
                text = "关于",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 12.dp),
                fontSize = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onAboutClick),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "关于AppSets",
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)
                )
            }
        }
    }
}

