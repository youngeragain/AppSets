package xcj.app.appsets.ui.compose.user

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.theme.ExtraLarge2
import xcj.app.appsets.ui.model.UserInfoForModify
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.RuntimeSingleStateUpdater
import xcj.app.appsets.util.reflect.TAG
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.DesignTextField
import xcj.app.starter.android.util.PurpleLogger

@Composable
fun ProfileModification(
    userInfo: UserInfo,
    onSelectUserAvatarClick: (String, ComposeStateUpdater<*>) -> Unit,
    onConfirmClick: (UserInfoForModify) -> Unit,
) {
    val userInfoForModify = remember {
        UserInfoForModify()
    }
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(horizontal = 12.dp)
            ) {
                FilledTonalButton(
                    onClick = {
                        onConfirmClick(userInfoForModify)
                    }
                ) {
                    Text(text = stringResource(id = xcj.app.starter.R.string.ok))
                }
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        DesignHDivider()
        Spacer(modifier = Modifier.height(12.dp))
        Column(modifier = Modifier.padding(horizontal = 12.dp)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(id = xcj.app.appsets.R.string.avatar),
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = FontWeight.Bold
                )
                AnyImage(
                    model = userInfoForModify.userAvatarUriProvider.value?.provideUri()
                        ?: userInfo.bioUrl,
                    modifier = Modifier
                        .size(250.dp)
                        .clip(ExtraLarge2)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = ExtraLarge2
                        )
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        val composeStateUpdater =
                            RuntimeSingleStateUpdater.fromState(userInfoForModify.userAvatarUriProvider) { markKey, input ->
                                PurpleLogger.current.d(
                                    TAG,
                                    "userInfoForModify.userAvatarUriProvider, input:$input"
                                )
                            }
                        onSelectUserAvatarClick(
                            "USER_PROFILE_MODIFY_AVATAR_IMAGE_SELECT_REQUEST",
                            composeStateUpdater
                        )
                    }
                ) {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.choose))
                }

            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.name),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userName.value,
                onValueChange = {
                    val newUserName = if (it.length > 30) {
                        it.substring(0, 30)
                    } else {
                        it
                    }
                    userInfoForModify.userName.value = newUserName
                },
                placeholder = {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.name),
                        fontSize = 12.sp
                    )
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.brief),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userIntroduction.value,
                onValueChange = {
                    userInfoForModify.userIntroduction.value = it
                },
                placeholder = {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.brief),
                        fontSize = 12.sp
                    )
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.label),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userTags.value,
                onValueChange = {
                    userInfoForModify.userTags.value = it
                },
                placeholder = {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.personal_label_placeholder),
                        fontSize = 12.sp
                    )
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.sex),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userSex.value,
                onValueChange = {
                    userInfoForModify.userSex.value = it
                },
                placeholder = {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.sex_placeholder),
                        fontSize = 12.sp
                    )
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.age),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userAge.value,
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
                    userInfoForModify.userAge.value = userAge
                },
                placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.age), fontSize = 12.sp)
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.phone),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userPhone.value,
                onValueChange = {
                    userInfoForModify.userPhone.value = it
                },
                placeholder = {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.phone_number),
                        fontSize = 12.sp
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.email),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userEmail.value,
                onValueChange = {
                    userInfoForModify.userEmail.value = it
                },
                placeholder = {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.email_address),
                        fontSize = 12.sp
                    )
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.area),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userArea.value,
                onValueChange = {
                    userInfoForModify.userArea.value = it
                },
                placeholder = {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.area),
                        fontSize = 12.sp
                    )
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.address),
                modifier = Modifier.padding(vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoForModify.userAddress.value,
                onValueChange = {
                    userInfoForModify.userAddress.value = it
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
                value = userInfoForModify.userWebsite.value,
                onValueChange = {
                    val userWebsite = if (it.length > 100) {
                        it.substring(0, 100)
                    } else {
                        it
                    }
                    userInfoForModify.userWebsite.value = userWebsite
                },
                placeholder = {
                    Text(
                        text = stringResource(id = xcj.app.appsets.R.string.website),
                        fontSize = 12.sp
                    )
                })
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}