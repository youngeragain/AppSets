package xcj.app.appsets.ui.compose.login

import androidx.compose.animation.AnimatedContent
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewWrapper
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResult
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.custom_component.preview_tooling.DesignPreviewCompositionLocalProvider
import xcj.app.appsets.ui.compose.theme.ExtraLarge2
import xcj.app.appsets.ui.model.UserInfoForCreate
import xcj.app.appsets.ui.model.page_state.SignUpPageUIState
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.RuntimeSingleStateUpdater
import xcj.app.compose_share.components.BackActionTopBar
import xcj.app.compose_share.components.DesignTextField
import xcj.app.compose_share.components.StatusBarWithTopActionBarSpacer
import xcj.app.compose_share.foundation_extension.ProjectPreviewWrapperProviderImpl
import xcj.app.compose_share.modifier.hazeSourceIfAvailable
import xcj.app.compose_share.modifier.rememberHazeStateIfAvailable
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "SignUpPage"

@PreviewWrapper(wrapper = ProjectPreviewWrapperProviderImpl::class)
@Preview(showBackground = true)
@Composable
fun SignUpPagePreview() {
    val signUpPageUIState by remember {
        mutableStateOf<SignUpPageUIState>(SignUpPageUIState.SignUpStart())
    }
    val userInfoForCreate by remember {
        mutableStateOf(UserInfoForCreate())
    }
    DesignPreviewCompositionLocalProvider {
        SignUpPage(
            signUpPageUIState = signUpPageUIState,
            userInfoForCreate = userInfoForCreate,
            onBackClick = {},
            onSelectUserAvatarClick = { requestKey, composeStateUpdater ->

            },
            onConfirmClick = { userInfoForCreate ->

            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SignUpPage(
    signUpPageUIState: SignUpPageUIState,
    userInfoForCreate: UserInfoForCreate,
    onBackClick: () -> Unit,
    onSelectUserAvatarClick: (String, ComposeStateUpdater<*>) -> Unit,
    onConfirmClick: (UserInfoForCreate) -> Unit,
) {

    LaunchedEffect(key1 = signUpPageUIState, block = {
        if (signUpPageUIState is SignUpPageUIState.SignUpSuccess) {
            onBackClick()
        }
    })
    val systemUseCase = LocalUseCaseOfSystem.current
    DisposableEffect(Unit) {
        onDispose {
            systemUseCase.onComposeDispose("page dispose")
        }
    }
    val hazeState = rememberHazeStateIfAvailable()

    VerticalOverscrollBox {

        Column(
            modifier = Modifier
                .hazeSourceIfAvailable(hazeState)
                .fillMaxWidth()
                .imePadding()
                .verticalScroll(rememberScrollState())
        ) {
            StatusBarWithTopActionBarSpacer()
            Column(modifier = Modifier.padding(horizontal = 12.dp)) {
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                )
                {
                    Text(
                        text = String.format(
                            stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                            stringResource(id = xcj.app.appsets.R.string.avatar),
                            "*",
                        ),
                        modifier = Modifier.padding(vertical = 12.dp),
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .size(250.dp)
                            .clip(ExtraLarge2)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                ExtraLarge2
                            )
                            .clickable(
                                onClick = {
                                    val composeStateUpdater =
                                        RuntimeSingleStateUpdater.fromState(userInfoForCreate.userAvatarUriProvider) { markKey, input ->
                                            PurpleLogger.current.d(
                                                TAG,
                                                "userInfoForCreate.userAvatarUriProvider, inputHandleDSL:\nmarkKey:$markKey\ninput:$input"
                                            )
                                            if (input !is ContentSelectionResult.RichMediaContentSelectionResult) {
                                                return@fromState
                                            }
                                            val uriProviders = input.selectedProvider.provide()
                                            if (uriProviders.isEmpty()) {
                                                return@fromState
                                            }
                                            update(uriProviders.first())
                                        }
                                    onSelectUserAvatarClick(
                                        "SIGN_UP_IMAGE_SELECT_REQUEST",
                                        composeStateUpdater
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        val avatarUri =
                            userInfoForCreate.userAvatarUriProvider.value?.provideUri()
                        AnimatedContent(
                            targetState = avatarUri != null,
                            contentAlignment = Alignment.Center
                        ) { hasUri ->
                            if (hasUri) {
                                AnyImage(
                                    model = avatarUri,
                                    modifier = Modifier
                                )
                            } else {
                                Icon(
                                    painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_add_24),
                                    contentDescription = stringResource(xcj.app.appsets.R.string.add)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = String.format(
                        stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                        stringResource(id = xcj.app.appsets.R.string.account),
                        "*"
                    ),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.account.value,
                    onValueChange = {
                        userInfoForCreate.account.value = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.account),
                            fontSize = 12.sp
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = String.format(
                        stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                        stringResource(id = xcj.app.appsets.R.string.password),
                        "*",
                    ),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.password.value,
                    onValueChange = {
                        userInfoForCreate.password.value = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.password),
                            fontSize = 12.sp
                        )
                    })
                Text(
                    text = String.format(
                        stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                        stringResource(id = xcj.app.appsets.R.string.name),
                        "*"
                    ),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userName.value,
                    onValueChange = {
                        val userNameFiltered = if (it.length > 30) {
                            it.substring(0, 30)
                        } else {
                            it
                        }
                        userInfoForCreate.userName.value = userNameFiltered
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.name),
                            fontSize = 12.sp
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = String.format(
                        stringResource(id = xcj.app.appsets.R.string.a_is_required_template),
                        stringResource(id = xcj.app.appsets.R.string.brief),
                        "*"
                    ),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userIntroduction.value,
                    onValueChange = {
                        userInfoForCreate.userIntroduction.value = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.personal_profile),
                            fontSize = 12.sp
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(xcj.app.appsets.R.string.label),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userTags.value,
                    onValueChange = {
                        userInfoForCreate.userTags.value = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.personal_label_placeholder),
                            fontSize = 12.sp
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(xcj.app.appsets.R.string.sex),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold

                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userSex.value,
                    onValueChange = {
                        userInfoForCreate.userSex.value = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.sex_placeholder),
                            fontSize = 12.sp
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(xcj.app.appsets.R.string.age),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userAge.value,
                    onValueChange = {
                        val toIntOrNull = it.toIntOrNull()
                        val userAgeFiltered = if (toIntOrNull != null) {
                            if (toIntOrNull > 150) {
                                "150"
                            } else {
                                toIntOrNull.toString()
                            }
                        } else {
                            ""
                        }
                        userInfoForCreate.userAge.value = userAgeFiltered
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.age),
                            fontSize = 12.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(xcj.app.appsets.R.string.phone),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userPhone.value,
                    onValueChange = {
                        val userPhoneFiltered = it.toIntOrNull()?.toString() ?: ""
                        userInfoForCreate.userPhone.value = userPhoneFiltered
                    },
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.phone_number),
                            fontSize = 12.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(xcj.app.appsets.R.string.email),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold

                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userEmail.value,
                    onValueChange = {
                        userInfoForCreate.userEmail.value = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.email_address),
                            fontSize = 12.sp
                        )
                    },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Email,
                        autoCorrectEnabled = true
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(xcj.app.appsets.R.string.area),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userArea.value,
                    onValueChange = {
                        userInfoForCreate.userArea.value = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.area),
                            fontSize = 12.sp
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(xcj.app.appsets.R.string.address),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userAddress.value,
                    onValueChange = {
                        userInfoForCreate.userAddress.value = it
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.address),
                            fontSize = 12.sp
                        )
                    })
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = xcj.app.appsets.R.string.website),
                    modifier = Modifier.padding(vertical = 10.dp),
                    fontWeight = FontWeight.Bold
                )
                DesignTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = userInfoForCreate.userWebsite.value,
                    onValueChange = {
                        val userWebsiteFiltered = if (it.length > 100) {
                            it.substring(0, 100)
                        } else {
                            it
                        }
                        userInfoForCreate.userWebsite.value = userWebsiteFiltered
                    },
                    placeholder = {
                        Text(
                            text = stringResource(id = xcj.app.appsets.R.string.website),
                            fontSize = 12.sp
                        )
                    },

                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Uri,
                        autoCorrectEnabled = true
                    )
                )
                Spacer(modifier = Modifier.height(150.dp))
            }
        }


        BackActionTopBar(
            hazeState = hazeState,
            onBackClick = onBackClick,
            backButtonText = stringResource(xcj.app.appsets.R.string.create_account),
            endButtonText = stringResource(id = xcj.app.starter.R.string.ok),
            onEndButtonClick = {
                onConfirmClick(userInfoForCreate)
            }
        )

        SignUpIndicator(signUpPageUIState = signUpPageUIState)
    }
}

@Composable
private fun SignUpIndicator(signUpPageUIState: SignUpPageUIState) {
    val isShow: Boolean =
        signUpPageUIState is SignUpPageUIState.SignUpping || signUpPageUIState is SignUpPageUIState.SignUpPageFailed
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
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Image(
                        modifier = Modifier.size(68.dp),
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_launcher_foreground),
                        contentDescription = null
                    )
                    val tipsIntRes =
                        signUpPageUIState.tips ?: xcj.app.appsets.R.string.processing
                    val text = stringResource(tipsIntRes)
                    Text(text, fontSize = 12.sp)
                }
            }
        }
    }
}

