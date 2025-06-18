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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfUserInfo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.theme.BigAvatarShape
import xcj.app.appsets.ui.model.UserInfoModification
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.DesignTextField

@Composable
fun ProfileModificationComponent(
    userInfo: UserInfo,
    onSelectUserAvatarClick: (String) -> Unit,
    onConfirmClick: () -> Unit,
) {
    val userInfoUseCase = LocalUseCaseOfUserInfo.current
    val userInfoModification by userInfoUseCase.userInfoModificationState
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
                    onClick = onConfirmClick
                ) {
                    Text(text = stringResource(id = xcj.app.starter.R.string.ok))
                }
            }
        }
        DesignHDivider()
        Column(Modifier.padding(horizontal = 12.dp)) {
            Column(Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(id = xcj.app.appsets.R.string.avatar),
                    modifier = Modifier.padding(vertical = 12.dp)
                )
                AnyImage(
                    any = userInfoModification.userAvatarUri?.provideUri() ?: userInfo.bioUrl,
                    modifier = Modifier
                        .size(250.dp)
                        .clip(BigAvatarShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline,
                            shape = BigAvatarShape
                        )
                        .align(Alignment.CenterHorizontally)
                )
                Spacer(modifier = Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        onSelectUserAvatarClick("USER_PROFILE_MODIFY_AVATAR_IMAGE_SELECT_REQUEST")
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.choose))
                }

            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.name),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(), value = userInfoModification.userName,
                onValueChange = {
                    val newUserName = if (it.length > 30) {
                        it.substring(0, 30)
                    } else {
                        it
                    }
                    UserInfoModification.updateStateUserName(
                        userInfoUseCase.userInfoModificationState,
                        newUserName
                    )
                }, placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.name))
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.brief),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoModification.userIntroduction,
                onValueChange = {
                    UserInfoModification.updateStateUserIntroduction(
                        userInfoUseCase.userInfoModificationState,
                        it
                    )
                },
                placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.brief))
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.label),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(), value = userInfoModification.userTags,
                onValueChange = {
                    UserInfoModification.updateStateUserTags(
                        userInfoUseCase.userInfoModificationState,
                        it
                    )
                }, placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.personal_label_placeholder))
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.sex),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(), value = userInfoModification.userSex,
                onValueChange = {
                    UserInfoModification.updateStateUserSex(
                        userInfoUseCase.userInfoModificationState,
                        it
                    )
                }, placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.sex_placeholder))
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.age),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoModification.userAge,
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
                    UserInfoModification.updateStateUserAge(
                        userInfoUseCase.userInfoModificationState,
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
                text = stringResource(id = xcj.app.appsets.R.string.phone),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(),
                value = userInfoModification.userPhone,
                onValueChange = {
                    UserInfoModification.updateStateUserPhone(
                        userInfoUseCase.userInfoModificationState,
                        it
                    )
                }, placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.phone_number))
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.email),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(), value = userInfoModification.userEmail,
                onValueChange = {
                    UserInfoModification.updateStateUserEmail(
                        userInfoUseCase.userInfoModificationState,
                        it
                    )
                }, placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.email_address))
                },
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Email)
            )
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.area),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(), value = userInfoModification.userArea,
                onValueChange = {
                    UserInfoModification.updateStateUserArea(
                        userInfoUseCase.userInfoModificationState,
                        it
                    )
                }, placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.area))
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.address),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(), value = userInfoModification.userAddress,
                onValueChange = {
                    UserInfoModification.updateStateUserAddress(
                        userInfoUseCase.userInfoModificationState,
                        it
                    )
                }, placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.address))
                })
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = xcj.app.appsets.R.string.website),
                modifier = Modifier.padding(vertical = 10.dp)
            )
            DesignTextField(
                modifier = Modifier.fillMaxWidth(), value = userInfoModification.userWebsite,
                onValueChange = {
                    val userWebsite = if (it.length > 100) {
                        it.substring(0, 100)
                    } else {
                        it
                    }
                    UserInfoModification.updateStateUserWebsite(
                        userInfoUseCase.userInfoModificationState,
                        userWebsite
                    )
                }, placeholder = {
                    Text(text = stringResource(id = xcj.app.appsets.R.string.website))
                })
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}