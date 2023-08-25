package xcj.app.appsets.ui.compose.login

import android.net.Uri
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.R
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.usecase.UserLoginUseCase


@Composable
fun Branding(loginState: UserLoginUseCase.LoginSignUpState?) {
    Box(
        Modifier
            .padding(20.dp)
            .fillMaxWidth(), contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                modifier = Modifier.size(98.dp),
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "brand icon"
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (loginState is UserLoginUseCase.LoginSignUpState.SignUping) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = loginState.tips ?: "注册中",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            } else if (loginState is UserLoginUseCase.LoginSignUpState.SignUpFail) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = loginState.tips ?: "注册失败",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
}


@Composable
fun SignUpPage(
    loginState: UserLoginUseCase.LoginSignUpState? = null,
    userAvatar: Uri? = null,
    onBackAction: () -> Unit,
    onSelectUserAvatarClick: () -> Unit,
    onConfirmClick: (
        String, String, String, String, String,
        String, String, String, String, String, String, String
    ) -> Unit,
) {

    LaunchedEffect(key1 = loginState, block = {
        if (loginState is UserLoginUseCase.LoginSignUpState.SignUpFinish) {
            onBackAction()
        }
    })


    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentWidth(align = Alignment.CenterHorizontally)
            .widthIn(max = 640.dp)
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, MaterialTheme.colorScheme.secondary, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(text = "建议", color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "使用消息摘要算法(例如MD5)对账号密码处理后填写到输入框内,增加账号密码安全性",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            val showBranding = loginState is UserLoginUseCase.LoginSignUpState.SignUping ||
                    loginState is UserLoginUseCase.LoginSignUpState.SignUpFail
            Spacer(
                modifier = Modifier
                    .weight(1f, fill = showBranding)
                    .animateContentSize()
            )
            AnimatedVisibility(
                showBranding,
                Modifier.fillMaxWidth()
            ) {
                Branding(loginState)
            }
            var account by remember {
                mutableStateOf("")
            }
            var password by remember {
                mutableStateOf("")
            }
            var userName by remember {
                mutableStateOf("")
            }

            var userIntroduction by remember {
                mutableStateOf("")
            }

            var userTags by remember {
                mutableStateOf("")
            }

            var userSex by remember {
                mutableStateOf("")
            }

            var userAge by remember {
                mutableStateOf("")
            }

            var userPhone by remember {
                mutableStateOf("")
            }

            var userEmail by remember {
                mutableStateOf("")
            }
            var userArea by remember {
                mutableStateOf("")
            }

            var userAddress by remember {
                mutableStateOf("")
            }

            var userWebsite by remember {
                mutableStateOf("")
            }

            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_round_arrow_24),
                        contentDescription = "go back",
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable(onClick = onBackAction)
                            .padding(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "注册账号",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(horizontal = 12.dp)
                ) {
                    Button(onClick = {
                        onConfirmClick(
                            account,
                            password,
                            userName,
                            userIntroduction,
                            userTags,
                            userSex,
                            userAge,
                            userPhone,
                            userEmail,
                            userArea,
                            userAddress,
                            userWebsite
                        )
                    }) {
                        Text(text = "确认")
                    }
                }
            }
            Divider(Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
            Column(Modifier.padding(horizontal = 12.dp)) {
                Text(text = "账号 (必填)", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = account,
                    onValueChange = {
                        account = it
                    }, placeholder = {
                        Text(text = "账号")
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "密码 (必填)", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = password,
                    onValueChange = {
                        password = it
                    }, placeholder = {
                        Text(text = "密码")
                    })

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column {
                        Text(text = "头像 (必填)", modifier = Modifier.padding(vertical = 12.dp))
                        Box(
                            modifier = Modifier
                                .size(98.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (userAvatar != null) {
                                SideEffect {
                                    Log.e("SignUpPage", "userAvatar:$userAvatar")
                                }
                                LocalOrRemoteImage(
                                    any = userAvatar,
                                    modifier = Modifier
                                        .size(88.dp)
                                        .clip(RoundedCornerShape(44.dp))
                                )
                            } else {
                                Image(
                                    modifier = Modifier.size(88.dp),
                                    painter = painterResource(id = R.drawable.outline_face_24),
                                    contentDescription = "avatar"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onSelectUserAvatarClick) {
                        Text(text = "选择")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "名称 (必填)", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = userName,
                    onValueChange = {
                        userName = if (it.length > 30) {
                            it.substring(0, 30)
                        } else
                            it
                    }, placeholder = {
                        Text(text = "名称")
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "简介 (必填)", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = userIntroduction,
                    onValueChange = {
                        userIntroduction = it
                    }, placeholder = {
                        Text(text = "个人简介")
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "标签", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = userTags,
                    onValueChange = {
                        userTags = it
                    }, placeholder = {
                        Text(text = "个人标签,比如爱好")
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "性别", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = userSex,
                    onValueChange = {
                        userSex = it
                    }, placeholder = {
                        Text(text = "男/女/中性/其他")
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "年龄", modifier = Modifier.padding(vertical = 10.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userAge,
                    onValueChange = {
                        val toIntOrNull = it.toIntOrNull()
                        userAge = if (toIntOrNull != null) {
                            if (toIntOrNull > 150) {
                                "150"
                            } else {
                                toIntOrNull.toString()
                            }
                        } else {
                            ""
                        }
                    },
                    placeholder = {
                        Text(text = "年龄")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "电话", modifier = Modifier.padding(vertical = 10.dp))
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userPhone,
                    onValueChange = {
                        userPhone = it.toIntOrNull()?.toString() ?: ""
                    }, placeholder = {
                        Text(text = "电话号码")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "邮箱", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = userEmail,
                    onValueChange = {
                        userEmail = it
                    }, placeholder = {
                        Text(text = "邮箱地址")
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "地区", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = userArea,
                    onValueChange = {
                        userArea = it
                    }, placeholder = {
                        Text(text = "地区")
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "地址", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = userAddress,
                    onValueChange = {
                        userAddress = it
                    }, placeholder = {
                        Text(text = "地址")
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "网站", modifier = Modifier.padding(vertical = 10.dp))
                TextField(modifier = Modifier.fillMaxWidth(), value = userWebsite,
                    onValueChange = {
                        userWebsite = if (it.length > 100) {
                            it.substring(0, 100)
                        } else
                            it
                    }, placeholder = {
                        Text(text = "网站")
                    })
                Spacer(
                    modifier = Modifier
                        .height(96.dp)
                        .imePadding()
                        .navigationBarsPadding()
                )

            }

        }
    }
}