package xcj.app.appsets.ui.compose.settings

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel

@UnstableApi
@Composable
fun LiteSettingsPanelDialog(
    isShowLiteSettingsPanel: MutableState<Boolean>,
    onSettingsUserNameClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSettingsLoginClick: () -> Unit,
) {
    val context = LocalContext.current
    val mainViewModel: MainViewModel = viewModel(context as AppCompatActivity)
    Dialog(onDismissRequest = {
        isShowLiteSettingsPanel.value = false
    }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(1f)
                .fillMaxHeight(0.7f)
                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(22.dp))
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(1f)
                    .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 52.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth(1f)) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_round_close_24),
                        contentDescription = "close",
                        tint = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .clip(CircleShape)
                            .clickable {
                                isShowLiteSettingsPanel.value = false
                            }
                            .padding(12.dp)


                    )
                }
                val loginState by LocalAccountManager.provideState<Boolean>()
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(
                            onClick = onSettingsUserNameClick
                        )
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val userInfoState by LocalAccountManager.provideState<UserInfo>()
                    if (loginState) {
                        LocalOrRemoteImage(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape),
                            any = userInfoState.avatarUrl,
                            defaultColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    } else {
                        Icon(
                            painterResource(id = R.drawable.outline_face_24),
                            null,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val name = if (loginState) {
                        userInfoState.name ?: "蒋开心"
                    } else {
                        "登录AppSets"
                    }
                    Text(text = name)
                }
                if (LocalAccountManager.isLogged()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedButton(onClick = {
                        mainViewModel.qrCodeUseCase!!.genQrCode()
                    }) {
                        Text(text = "二维码登录")
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    OutlinedButton(onClick = {
                        mainViewModel.qrCodeUseCase!!.toScanQrCodePage(context)
                    }) {
                        Text(text = "扫描二维码")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }
                val qrcode = mainViewModel.qrCodeUseCase!!.qrcodeState
                AnimatedVisibility(visible = qrcode.value?.first != null && qrcode.value?.second == "0") {
                    Image(
                        painter = BitmapPainter(qrcode.value!!.first!!.asImageBitmap()),
                        contentDescription = "qrcode"
                    )
                }
                if (qrcode.value?.second == "1") {
                    Box(Modifier.padding(16.dp)) {
                        Button(onClick = {
                            mainViewModel.qrCodeUseCase?.doConfirm()
                        }) {
                            Text(text = "设备已扫描，确认此设备登录?")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onSettingsClick)
                        .padding(vertical = 12.dp, horizontal = 12.dp)
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = R.drawable.ic_outline_settings_24),
                        null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "应用设置")
                }
                val loginOrLogOutText = if (loginState) {
                    "退出登录"
                } else {
                    "登录或注册"
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(onClick = onSettingsLoginClick)
                        .padding(vertical = 12.dp, horizontal = 12.dp)
                ) {
                    Spacer(modifier = Modifier.width(32.dp))
                    Text(text = loginOrLogOutText)
                }
            }

            val packageVersionName = mainViewModel.appSetsUseCase.getAppSetsPackageVersionName()
            if (!packageVersionName.isNullOrEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Divider(Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "version: $packageVersionName", fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}