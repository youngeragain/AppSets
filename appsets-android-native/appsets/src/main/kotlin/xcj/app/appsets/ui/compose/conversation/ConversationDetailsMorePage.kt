package xcj.app.appsets.ui.compose.conversation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.ui.compose.BackActionTopBar
import xcj.app.appsets.usecase.UserRelationsCase


@Preview(showBackground = true)
@Composable
fun ConversationDetailsMorePagePreView() {
//    ConversationDetailsMorePage({ })
}

@Composable
fun ConversationDetailsMorePage(
    tabVisibilityState: MutableState<Boolean>,
    imObj: ImObj?,
    onBackClick: () -> Unit,
    onRequestAddFriend: (String) -> Unit,
    onRequestDeleteFriend: (String) -> Unit,
    onShowUserInfoClick: (String) -> Unit,
    onRequestJoinGroup: (String) -> Unit,
    onRequestLeaveGroup: (String) -> Unit,
    onRequestDeleteGroup: (String) -> Unit,
    onShowGroupInfoClick: (String) -> Unit,
) {
    DisposableEffect(key1 = true, effect = {
        onDispose {
            tabVisibilityState.value = true
        }
    })
    SideEffect {
        tabVisibilityState.value = false
    }
    Column() {
        BackActionTopBar(backButtonRightText = "会话信息", onBackAction = onBackClick)
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            if (imObj is ImObj.ImSingle) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onShowUserInfoClick(imObj.uid)
                    }
                    .padding(12.dp)) {
                    Text(text = "查看详情")
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (UserRelationsCase.getInstance().hasUserRelated(imObj.id)) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onRequestDeleteFriend(imObj.uid)
                        }
                        .padding(12.dp)) {
                        Text(text = "解除关系")
                    }
                } else {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onRequestAddFriend(imObj.uid)
                        }
                        .padding(12.dp)) {
                        Text(text = "添加好友")
                    }
                }
            } else if (imObj is ImObj.ImGroup) {
                Row(modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        onShowGroupInfoClick(imObj.groupId)
                    }
                    .padding(12.dp)) {
                    Text(text = "查看详情")
                }
                Spacer(modifier = Modifier.height(12.dp))
                if (UserRelationsCase.getInstance().hasGroupRelated(imObj.id)) {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onRequestLeaveGroup(imObj.groupId)
                        }
                        .padding(12.dp)) {
                        Text(text = "解除关系")
                    }
                } else {
                    Row(modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            onRequestJoinGroup(imObj.groupId)
                        }
                        .padding(12.dp)) {
                        Text(text = "申请加入")
                    }
                }
            }

        }
    }
}

