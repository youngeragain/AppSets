package xcj.app.appsets.ui.compose.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.compose_share.components.DesignTextField
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.model.LoginSignUpState
import xcj.app.appsets.ui.model.SignUpUserInfo
import xcj.app.compose_share.components.BackActionTopBar

private const val TAG = "SignUpPage"

@Composable
fun SignUpPage(
    loginState: LoginSignUpState,
    onBackClick: () -> Unit,
    onSelectUserAvatarClick: (String) -> Unit,
    onConfirmClick: () -> Unit,
) {

    LaunchedEffect(key1 = loginState, block = {
        if (loginState is LoginSignUpState.SignUpFinish) {
            onBackClick()
        }
    })
    val systemUseCase = LocalUseCaseOfSystem.current
    DisposableEffect(Unit) {
        onDispose {
            systemUseCase.onComposeDispose("page dispose")
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
        ) {
            BackActionTopBar(
                backButtonRightText = stringResource(xcj.app.appsets.R.string.register_an_account),
                onBackClick = onBackClick,
                endButtonText = stringResource(id = xcj.app.starter.R.string.ok),
                onEndButtonClick = onConfirmClick
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                val signUpUserInfo = when (loginState) {
                    is LoginSignUpState.SignUp -> loginState.signUpUserInfo
                    is LoginSignUpState.SignUping -> loginState.signUpUserInfo
                    is LoginSignUpState.SignUpFinish -> loginState.signUpUserInfo
                    is LoginSignUpState.SignUpFail -> loginState.signUpUserInfo
                    else -> null
                }
                if (signUpUserInfo != null) {
                    Column(Modifier.padding(horizontal = 12.dp)) {
                        Spacer(Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.outline,
                                        MaterialTheme.shapes.extraLarge
                                    )
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = stringResource(id = xcj.app.appsets.R.string.tips)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(xcj.app.appsets.R.string.sign_up_tips),
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                        Text(
                            text = String.format(
                                stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                                stringResource(id = xcj.app.appsets.R.string.account),
                                stringResource(id = xcj.app.appsets.R.string.required)
                            ),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.account,
                            onValueChange = {
                                SignUpUserInfo.updateStateUserAccount(
                                    systemUseCase.loginSignUpState,
                                    it
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.account))
                            })
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = String.format(
                                stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                                stringResource(id = xcj.app.appsets.R.string.password),
                                stringResource(id = xcj.app.appsets.R.string.required)
                            ),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.password,
                            onValueChange = {
                                SignUpUserInfo.updateStateUserPassword(
                                    systemUseCase.loginSignUpState,
                                    it
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.password))
                            })

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Column {
                                Text(
                                    text = String.format(
                                        stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                                        stringResource(id = xcj.app.appsets.R.string.avatar),
                                        stringResource(id = xcj.app.appsets.R.string.required)
                                    ),
                                    modifier = Modifier.padding(vertical = 12.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(MaterialTheme.shapes.extraLarge)
                                        .border(
                                            1.dp, MaterialTheme.colorScheme.outline,
                                            MaterialTheme.shapes.extraLarge
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val avatarUri =
                                        signUpUserInfo.userAvatar?.provideUri()
                                            ?: xcj.app.compose_share.R.drawable.ic_outline_face_24
                                    AnyImage(
                                        any = avatarUri,
                                        modifier = Modifier
                                            .size(98.dp)
                                            .clip(MaterialTheme.shapes.extraLarge)
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))

                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = MaterialTheme.shapes.extraLarge,
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.extraLarge)
                                    .clickable(onClick = {
                                        onSelectUserAvatarClick("SIGN_UP_IMAGE_SELECT_REQUEST")
                                    })
                            ) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
                                    Text(text = stringResource(id = xcj.app.appsets.R.string.choose))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = String.format(
                                stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                                stringResource(id = xcj.app.appsets.R.string.name),
                                stringResource(id = xcj.app.appsets.R.string.required)
                            ),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userName,
                            onValueChange = {
                                val userName = if (it.length > 30) {
                                    it.substring(0, 30)
                                } else {
                                    it
                                }
                                SignUpUserInfo.updateStateUserName(
                                    systemUseCase.loginSignUpState,
                                    userName
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.name))
                            })
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = String.format(
                                stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                                stringResource(id = xcj.app.appsets.R.string.brief),
                                stringResource(id = xcj.app.appsets.R.string.required)
                            ),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userIntroduction,
                            onValueChange = {
                                SignUpUserInfo.updateStateUserIntroduction(
                                    systemUseCase.loginSignUpState,
                                    it
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(xcj.app.appsets.R.string.personal_profile))
                            })
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.label),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userTags,
                            onValueChange = {
                                SignUpUserInfo.updateStateUserTags(
                                    systemUseCase.loginSignUpState,
                                    it
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(xcj.app.appsets.R.string.personal_label_placeholder))
                            })
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.sex),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userSex,
                            onValueChange = {
                                SignUpUserInfo.updateStateUserSex(
                                    systemUseCase.loginSignUpState,
                                    it
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(xcj.app.appsets.R.string.sex_placeholder))
                            })
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.age),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userAge,
                            onValueChange = {
                                val toIntOrNull = it.toIntOrNull()
                                val userAge = if (toIntOrNull != null) {
                                    if (toIntOrNull > 150) {
                                        "150"
                                    } else {
                                        toIntOrNull.toString()
                                    }
                                } else {
                                    ""
                                }
                                SignUpUserInfo.updateStateUserAge(
                                    systemUseCase.loginSignUpState,
                                    userAge
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.age))
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.phone),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userPhone,
                            onValueChange = {
                                val userPhone = it.toIntOrNull()?.toString() ?: ""
                                SignUpUserInfo.updateStateUserPhone(
                                    systemUseCase.loginSignUpState,
                                    userPhone
                                )
                            }, placeholder = {
                                Text(text = stringResource(xcj.app.appsets.R.string.phone_number))
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.email),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userEmail,

                            onValueChange = {
                                SignUpUserInfo.updateStateUserEmail(
                                    systemUseCase.loginSignUpState,
                                    it
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(xcj.app.appsets.R.string.email_address))
                            },
                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Email,
                                autoCorrectEnabled = true
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.area),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userArea,
                            onValueChange = {
                                SignUpUserInfo.updateStateUserArea(
                                    systemUseCase.loginSignUpState,
                                    it
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.area))
                            })
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(xcj.app.appsets.R.string.address),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userAddress,
                            onValueChange = {
                                SignUpUserInfo.updateStateUserAddress(
                                    systemUseCase.loginSignUpState,
                                    it
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.address))
                            })
                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.website),
                            modifier = Modifier.padding(vertical = 10.dp)
                        )
                        DesignTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = signUpUserInfo.userWebsite,
                            onValueChange = {
                                val userWebsite = if (it.length > 100) {
                                    it.substring(0, 100)
                                } else {
                                    it
                                }
                                SignUpUserInfo.updateStateUserWebsite(
                                    systemUseCase.loginSignUpState,
                                    userWebsite
                                )
                            },
                            placeholder = {
                                Text(text = stringResource(id = xcj.app.appsets.R.string.website))
                            },

                            keyboardOptions = KeyboardOptions.Default.copy(
                                keyboardType = KeyboardType.Uri,
                                autoCorrectEnabled = true
                            )
                        )
                        Spacer(modifier = Modifier.height(120.dp))
                    }
                }
            }
        }
        SignUpIndicator(loginSignUpState = loginState)
    }
}

@Composable
fun SignUpIndicator(loginSignUpState: LoginSignUpState) {
    val isShow: Boolean =
        loginSignUpState is LoginSignUpState.SignUping || loginSignUpState is LoginSignUpState.SignUpFail
    AnimatedVisibility(
        visible = isShow,
        enter = fadeIn(tween()) + scaleIn(
            tween(),
            2f
        ),
        exit = fadeOut(tween()) + scaleOut(
            tween(),
            0.2f
        ),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .widthIn(max = 180.dp)
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .padding(vertical = 12.dp, horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Image(
                        modifier = Modifier.size(68.dp),
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                    val tipsIntRes = loginSignUpState.tips
                    val text = if (tipsIntRes != null) {
                        stringResource(tipsIntRes)
                    } else {
                        stringResource(xcj.app.appsets.R.string.processing)
                    }
                    Text(text, fontSize = 12.sp)
                }
            }
        }
    }
}