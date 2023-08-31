package xcj.app.appsets.ui.compose.login

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.usecase.UserLoginUseCase

@Composable
fun LoginPage(
    loginSignUpState: State<UserLoginUseCase.LoginSignUpState?>,
    qrCodeState: State<Pair<Bitmap?, String>?>,
    onSignUpButtonClick: () -> Unit,
    onQRCodeLoginButtonClick: () -> Unit,
    onScanQRCodeButtonClick: () -> Unit,
    onLoginConfirmButtonClick: (String, String) -> Unit
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .widthIn(300.dp, 411.dp)
            .padding(start = 12.dp, end = 12.dp, top = 32.dp)
            .verticalScroll(scrollState)
    ) {
        AnimatedVisibility(visible = loginSignUpState.value is UserLoginUseCase.LoginSignUpState.Logining) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "正在登录")
                }
            }
        }
        Column() {
            Button(onClick = onSignUpButtonClick) {
                Text(text = "注册")
            }
            Button(onClick = onQRCodeLoginButtonClick) {
                Text(text = "二维码登录", color = Color.White)
            }
            Button(onClick = onScanQRCodeButtonClick) {
                Text(text = "扫描二维码", color = Color.White)
            }
            Button(onClick = {}) {
                Text(text = "验证码登录")
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Column(Modifier.fillMaxWidth()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
            ) {
                androidx.compose.animation.AnimatedVisibility(visible = qrCodeState.value?.first != null && qrCodeState.value?.second == "0") {
                    Image(
                        painter = BitmapPainter(qrCodeState.value!!.first!!.asImageBitmap()),
                        contentDescription = "qrcode"
                    )
                }
                androidx.compose.animation.AnimatedVisibility(visible = qrCodeState.value?.second == "-1") {
                    OutlinedButton(onClick = onQRCodeLoginButtonClick) {
                        Text(text = "失效，重新生成")
                    }
                }
                val b = qrCodeState.value?.second == "1" || qrCodeState.value?.second == "2"
                if (b) {
                    val text = if (qrCodeState.value?.second == "1") {
                        "已扫描"
                    } else {
                        "已确认"
                    }
                    Text(text = text)
                }

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(text = "提示", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "AppSets为你提供类似应用商店，社交，聊天等功能，开发版无法保证你账号的数据和隐私安全\n* 注册时使用消息摘要算法对账号密码处理的情况，需要以同等方式处理后再填入输入框",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "登录", fontWeight = FontWeight.Bold, fontSize = 32.sp, color = Color.Black)

        }
        Spacer(modifier = Modifier.height(22.dp))
        var accountText by remember {
            mutableStateOf("")
        }
        TextField(value = accountText ?: "", modifier = Modifier.fillMaxWidth(), singleLine = true,
            onValueChange = {
                accountText = it
            }, placeholder = {
                Text("账号")
            })
        Spacer(modifier = Modifier.height(12.dp))
        var passwordText by remember {
            mutableStateOf("")
        }
        TextField(value = passwordText ?: "", modifier = Modifier.fillMaxWidth(), singleLine = true,
            onValueChange = {
                passwordText = it
            }, placeholder = {
                Text("密码")
            }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row {
            Button(onClick = {
                onLoginConfirmButtonClick(accountText, passwordText)
            }) {
                Text(text = "确定", color = Color.White)
            }
        }

    }
}