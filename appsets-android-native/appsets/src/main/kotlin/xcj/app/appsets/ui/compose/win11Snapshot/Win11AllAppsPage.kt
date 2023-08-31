package xcj.app.appsets.ui.compose.win11Snapshot

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel

@UnstableApi
@Composable
fun Win11AllAppsPage(
    tabVisibilityState: MutableState<Boolean>,
    onBackClick: () -> Unit
) {
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    Column(
        modifier = Modifier
            .padding(top = 32.dp, bottom = 32.dp, start = 10.dp, end = 10.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "所有应用",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.CenterStart)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .background(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                    .clip(RoundedCornerShape(6.dp))
                    .clickable(onClick = onBackClick)
                    .padding(12.dp, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_arrow_ios_24),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.CenterVertically)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    text = "返回",
                    fontSize = 12.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
        var isShowPinAppAlertDialog by remember {
            mutableStateOf(false)
        }
        var pinAppPackageName: String? by remember {
            mutableStateOf(null)
        }
        val vm: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
        if (isShowPinAppAlertDialog) {
            AlertDialog(
                onDismissRequest = {
                    isShowPinAppAlertDialog = false
                },
                confirmButton = {
                    Text(text = "是", Modifier.clickable {
                        vm.pinApp(pinAppPackageName)
                        isShowPinAppAlertDialog = false
                        pinAppPackageName = null
                    })
                },
                text = {
                    Text(text = "固定到主屏幕?")
                }
            )
        }
        val context = LocalContext.current
        val iconModifier = Modifier
            .size(52.dp)
            .background(
                MaterialTheme.colorScheme.inverseOnSurface,
                RoundedCornerShape(10.dp)
            )
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
            .padding(6.dp)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(vm.win11SnapShotUseCase.allApps) { index, appDefinition ->
                if (index == 0)
                    Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (appDefinition.isPinned) {
                                Toast
                                    .makeText(
                                        context,
                                        "此应用已经固定，是否取消固定?",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            } else {
                                pinAppPackageName = appDefinition.packageName
                                isShowPinAppAlertDialog = true
                            }

                        }, verticalAlignment = Alignment.CenterVertically
                ) {
                    LocalOrRemoteImage(
                        modifier = iconModifier,
                        any = appDefinition.icon
                    )
                    Text(
                        text = appDefinition.name,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                if (index == vm.win11SnapShotUseCase.allApps.size - 1) {
                    Spacer(modifier = Modifier.height(48.dp))
                }
            }
        }


    }
}