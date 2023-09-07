package xcj.app.appsets.ui.compose.group

import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import xcj.app.appsets.R
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.usecase.GroupInfoState
import xcj.app.appsets.usecase.UserRelationsCase


@Preview(showBackground = true)
@Composable
fun GroupInfoPagePreview() {
    //GroupInfoPage(null)
}

@Composable
fun GroupInfoPage(
    tabVisibilityState: MutableState<Boolean>,
    onBackClick: () -> Unit,
    onUserAvatarClick: (UserInfo?) -> Unit,
    onTalkToGroupClick: (GroupInfo) -> Unit,
    onJoinGroupRequestClick: (GroupInfo) -> Unit
) {

    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    val mainViewModel: MainViewModel = viewModel(LocalContext.current as AppCompatActivity)
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        val groupInfoState = mainViewModel.groupInfoUseCase?.groupInfoState?.value
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth()) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_round_arrow_24),
                    contentDescription = "go back",
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(onClick = onBackClick)
                        .padding(12.dp)
                )
                Text(text = "群组信息", modifier = Modifier.align(Alignment.Center))
                if (groupInfoState is GroupInfoState.GroupInfoWrapper) {
                    if (UserRelationsCase.getInstance()
                            .hasGroupRelated(groupInfoState.groupInfo.groupId)
                        || groupInfoState.groupInfo.public == 1
                    ) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(horizontal = 12.dp)
                        ) {
                            Button(
                                onClick = {
                                    onTalkToGroupClick(groupInfoState.groupInfo)
                                }) {
                                Text(text = "聊天")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .padding(horizontal = 12.dp)
                        ) {
                            Button(
                                onClick = {
                                    onJoinGroupRequestClick(groupInfoState.groupInfo)
                                }) {
                                Text(text = "申请加入")
                            }
                        }
                    }
                }
            }
        }
        Divider(modifier = Modifier.height(0.5.dp), color = MaterialTheme.colorScheme.outline)
        if (groupInfoState is GroupInfoState.Loading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp), contentAlignment = Alignment.Center
            ) {
                Column {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "加载中")
                }
            }
        } else if (groupInfoState is GroupInfoState.GroupInfoWrapper) {
            val scrollState = rememberScrollState()
            Column(
                Modifier.verticalScroll(scrollState)
            ) {
                val groupInfoWrapper = groupInfoState as GroupInfoState.GroupInfoWrapper
                Row(Modifier.padding(12.dp)) {
                    LocalOrRemoteImage(
                        modifier = Modifier
                            .size(98.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        any = groupInfoWrapper.groupInfo.iconUrl,
                        defaultColor = MaterialTheme.colorScheme.primaryContainer
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(text = "群组名称")
                        Text(
                            text = groupInfoWrapper.groupInfo.name ?: "",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(text = "群介绍")
                        Text(
                            text = groupInfoWrapper.groupInfo.introduction ?: "没有介绍",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(text = "群组成员", modifier = Modifier.padding(12.dp))
                if (!groupInfoWrapper.groupInfo.userInfoList.isNullOrEmpty()) {
                    groupInfoWrapper.groupInfo.userInfoList!!.forEach { userInfo ->
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)) {
                            Row {
                                LocalOrRemoteImage(
                                    any = userInfo.avatarUrl,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable {
                                            onUserAvatarClick(userInfo)
                                        })
                                Spacer(modifier = Modifier.width(10.dp))
                                Column() {
                                    Text(text = userInfo.name ?: "Unset Name")
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(text = userInfo.introduction ?: "")
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "没有群组成员")
                    }
                }
                Spacer(modifier = Modifier.height(68.dp))
            }
        }
    }
}