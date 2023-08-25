package xcj.app.appsets.ui.compose.group

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import xcj.app.appsets.R
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ui.compose.BackActionTopBar
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateGroupPage(
    groupIconState: State<MediaStoreDataUriWrapper?>,
    onBackAction: () -> Unit,
    onConfirmAction: (String, String, Boolean, String) -> Unit,
    onSelectGroupIconClick: () -> Unit
) {
    val viewModel = viewModel<MainViewModel>(LocalContext.current as AppCompatActivity)
    DisposableEffect(key1 = true, effect = {
        onDispose {
            viewModel.bottomMenuUseCase.tabVisibilityState.value = true
        }
    })
    SideEffect {
        viewModel.bottomMenuUseCase.tabVisibilityState.value = false
    }
    Column {
        var groupName by remember {
            mutableStateOf("")
        }
        var groupMembersCount by remember {
            mutableStateOf("")
        }
        var groupIntroduction by remember {
            mutableStateOf("")
        }
        var isPublic by remember {
            mutableStateOf(false)
        }
        BackActionTopBar(
            backButtonRightText = "创建群组",
            endButtonText = "确定",
            onBackAction = onBackAction,
            onConfirmClick = {
                onConfirmAction(groupName, groupMembersCount, isPublic, groupIntroduction)
            })
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Column {
                    Text(text = "群组标志图 (必填)", modifier = Modifier.padding(vertical = 12.dp))
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
                        if (groupIconState.value != null) {
                            SideEffect {
                                Log.e("CreateGroupPage", "groupIcon:${groupIconState.value}")
                            }
                            LocalOrRemoteImage(
                                any = groupIconState.value?.provideUri(),
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(RoundedCornerShape(44.dp))
                            )
                        } else {
                            Image(
                                modifier = Modifier.size(88.dp),
                                painter = painterResource(id = R.drawable.outline_emoji_nature_24),
                                contentDescription = "avatar"
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))

                }
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onSelectGroupIconClick) {
                    Text(text = "选择")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "状态")
            Row() {
                FilterChip(
                    selected = isPublic,
                    onClick = {
                        isPublic = true
                    },
                    label = {
                        Text(text = "公开")
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                FilterChip(
                    selected = !isPublic,
                    onClick = {
                        isPublic = false
                    },
                    label = {
                        Text(text = "私有")
                    },
                    shape = RoundedCornerShape(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "名称 (必填)", modifier = Modifier.padding(vertical = 10.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                value = groupName,
                onValueChange = {
                    groupName = if (it.length > 30) {
                        it.substring(0, 30)
                    } else
                        it
                }, placeholder = {
                    Text(text = "群组名称")
                })
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "最大成员数量", modifier = Modifier.padding(vertical = 10.dp))
            TextField(
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                value = groupMembersCount,
                keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                onValueChange = {
                    groupMembersCount = it.toIntOrNull()?.toString() ?: ""
                }, placeholder = {
                    Text(text = "数量")
                })
            Spacer(modifier = Modifier.height(12.dp))
            Text(text = "介绍", modifier = Modifier.padding(vertical = 10.dp))
            TextField(modifier = Modifier.fillMaxWidth(), value = groupIntroduction,
                onValueChange = {
                    groupIntroduction = it
                }, placeholder = {
                    Text(text = "群组描述")
                })
            Spacer(modifier = Modifier.height(68.dp))
        }
    }
}