package xcj.app.appsets.ui.compose.conversation

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import xcj.app.appsets.R
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.SystemContentInterface
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.compose.win11Snapshot.ComponentImageButton

@UnstableApi
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationOverviewPage(
    onAddFriendClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onConversionSessionClick: (Session) -> Unit,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onAvatarClick: (String, String) -> Unit,
    onUserRequestClick: (Boolean, Session, ImMessage.System) -> Unit
) {
    Column {
        val current = LocalContext.current
        val vm: MainViewModel = viewModel(current as AppCompatActivity)
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        Column(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .animateContentSize()
        )
        {
            var isShowAddActions by remember {
                mutableStateOf(false)
            }
            Row(horizontalArrangement = Arrangement.Start) {
                val titles by remember {
                    mutableStateOf(listOf("个人", "群组", "系统"))
                }
                titles.forEachIndexed { index, title ->
                    FilterChip(
                        selected = vm.conversationUseCase?.currentTab?.value == index,
                        onClick = {
                            vm.conversationUseCase?.onChipClick(index)
                        },
                        label = {
                            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        },
                        shape = RoundedCornerShape(16.dp)
                    )
                    if (index < titles.size - 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                if (isShowAddActions) {
                    Icon(
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .clip(CircleShape)
                            .clickable(onClick = {
                                isShowAddActions = false
                            })
                            .padding(12.dp),
                        painter = painterResource(id = R.drawable.ic_round_close_24),
                        contentDescription = "close",
                        tint = MaterialTheme.colorScheme.surfaceVariant
                    )
                } else {
                    ComponentImageButton(modifier = Modifier, onClick = {
                        isShowAddActions = true
                    })
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = isShowAddActions,
                enter = slideInVertically(
                    animationSpec = tween(250),
                    initialOffsetY = { 0 }) + fadeIn(),
                exit = slideOutVertically(animationSpec = tween(200), targetOffsetY = { 0 }),
                content = {
                    ConversationOverviewAddActionsComponent(
                        onAddFriendClick = onAddFriendClick,
                        onJoinGroupClick = onJoinGroupClick,
                        onCreateGroupClick = onCreateGroupClick
                    )
                })
        }

        Spacer(modifier = Modifier.height(12.dp))
        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline)
        val sortedSessions = vm.conversationUseCase?.currentSessions()
        val shouldShowEmpty = if (vm.conversationUseCase?.currentTab?.value == 2) {
            if (sortedSessions.isNullOrEmpty())
                true
            else
                sortedSessions.firstOrNull()?.conversionState?.messages.isNullOrEmpty()
        } else {
            sortedSessions.isNullOrEmpty()
        }
        if (shouldShowEmpty) {
            Box(modifier = Modifier
                .fillMaxSize()
                .weight(1f)) {
                val text = when (vm.conversationUseCase?.currentTab?.value) {
                    0 -> {
                        "空的个人列表"
                    }

                    1 -> {
                        "空的群组列表"
                    }

                    else -> {
                        "空的系统消息列表"
                    }
                }
                Text(text = text, modifier = Modifier.align(Alignment.Center))
            }
        } else {
            if (sortedSessions != null) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(bottom = 68.dp)
                ) {
                    if (vm.conversationUseCase?.currentTab?.value == 2) {
                        itemsIndexed(sortedSessions) { index, session ->
                            if (!session.conversionState?.messages.isNullOrEmpty()) {
                                Column(
                                    modifier = Modifier
                                        .padding(horizontal = 8.dp)
                                        .fillMaxWidth()
                                ) {
                                    session.conversionState?.messages!!.forEach { message ->
                                        SideEffect {
                                            Log.e("SystemImMessageContent", "message:${message}")
                                        }
                                        if (message is ImMessage.System) {
                                            SystemImMessageContent(
                                                session,
                                                message,
                                                onSystemImMessageClick,
                                                onAvatarClick,
                                                onUserRequestClick
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(68.dp))
                            }
                        }
                    } else {
                        itemsIndexed(sortedSessions) { index, session ->
                            if (session.isTitle) {
                                Column(Modifier.fillMaxWidth()) {
                                    Text(
                                        text = session.imObj.name,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp,
                                        modifier = Modifier.padding(vertical = 10.dp)
                                    )
                                    if (index == 0 && sortedSessions[1].isTitle) {
                                        Box(
                                            Modifier
                                                .height(120.dp)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "没有对话")
                                        }
                                    } else if (index > 0 && index == sortedSessions.size - 1) {
                                        Box(
                                            Modifier
                                                .height(120.dp)
                                                .fillMaxWidth(),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = "没有对话")
                                        }
                                    }
                                }
                            } else {
                                Column(
                                    Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            onConversionSessionClick(session)
                                        }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Row {
                                        LocalOrRemoteImage(
                                            modifier = Modifier
                                                .size(42.dp)
                                                .background(
                                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clip(RoundedCornerShape(8.dp)),
                                            any = session.imObj.avatar,
                                            defaultColor = MaterialTheme.colorScheme.secondaryContainer
                                        )
                                        Column(
                                            modifier = Modifier
                                                .padding(horizontal = 8.dp)
                                                .weight(1f)
                                        ) {
                                            //名称
                                            Text(
                                                text = session.imObj.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            if (session.isO2O) {
                                                Text(
                                                    text = session.lastMsg?.contentByMyType()
                                                        ?: "无消息",
                                                    fontSize = 13.sp
                                                )
                                            } else if (!session.isTitle) {
                                                val message = session.lastMsg
                                                if (message == null) {
                                                    Text(text = "无消息", fontSize = 13.sp)
                                                } else {
                                                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                                                        LocalOrRemoteImage(
                                                            modifier = Modifier
                                                                .size(20.dp)
                                                                .clip(RoundedCornerShape(2.dp)),
                                                            any = message.msgFromInfo.avatarUrl,
                                                            defaultColor = MaterialTheme.colorScheme.primaryContainer
                                                        )
                                                        Spacer(modifier = Modifier.width(4.dp))
                                                        Text(
                                                            text = message.contentByMyType(),
                                                            fontSize = 13.sp
                                                        )
                                                    }
                                                }
                                            }
                                            Spacer(Modifier.height(12.dp))
                                            Divider(
                                                thickness = 0.5.dp,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                        if (session.lastMsg != null) {
                                            Text(session.lastMsg!!.dateStr ?: "", fontSize = 10.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }
        }
    }
}

@Composable
fun ConversationOverviewAddActionsComponent(
    onAddFriendClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(12.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = "添加好友", modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onAddFriendClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "添加群组", modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onJoinGroupClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "创建群组",
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onCreateGroupClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
fun SystemImMessageContent(
    session: Session, imMessage: ImMessage.System,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onAvatarClick: (String, String) -> Unit,
    onUserRequestClick: (Boolean, Session, ImMessage.System) -> Unit
) {

    Spacer(modifier = Modifier.height(10.dp))
    Box(
        modifier = Modifier.background(
            MaterialTheme.colorScheme.secondaryContainer,
            RoundedCornerShape(16.dp)
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            val contentObject =
                imMessage.systemContentJson.contentObject
            when (contentObject) {
                is SystemContentInterface.FriendRequestJson -> {
                    FriendRequestCard(session,
                        imMessage,
                        contentObject,
                        onSystemImMessageClick,
                        { userId -> onAvatarClick("user", userId) },
                        onUserRequestClick
                    )
                }

                is SystemContentInterface.GroupRequestJson -> {
                    GroupRequestCard(
                        session,
                        imMessage,
                        contentObject,
                        onSystemImMessageClick,
                        onAvatarClick,
                        onUserRequestClick
                    )
                }

                else -> Unit
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GroupRequestCard(
    session: Session,
    imMessage: ImMessage.System,
    contentObject: SystemContentInterface.GroupRequestJson,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onAvatarClick: (String, String) -> Unit,
    onUserRequestClick: (Boolean, Session, ImMessage.System) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LocalOrRemoteImage(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp)),
            any = imMessage.fromUserInfo.avatarUrl,
            defaultColor = MaterialTheme.colorScheme.secondaryContainer
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "加入群组申请",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp
        )
        Text(text = imMessage.dateStr ?: "", fontSize = 10.sp)
    }
    Spacer(modifier = Modifier.height(20.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Row(
            modifier = Modifier.clickable {
                onAvatarClick("user", contentObject.uid)
            }
        ) {
            LocalOrRemoteImage(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp)),
                any = contentObject.avatarUrl,
                defaultColor = MaterialTheme.colorScheme.secondaryContainer
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = contentObject.name ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = R.drawable.round_forward_24),
                contentDescription = "join group"
            )
        }
        Row(
            modifier = Modifier.clickable {
                onAvatarClick("group", contentObject.uid)
            }
        ) {
            LocalOrRemoteImage(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp)),
                any = contentObject.groupIconUrl,
                defaultColor = MaterialTheme.colorScheme.secondaryContainer
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = contentObject.groupName ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
        }
    }
    Column(modifier = Modifier
        .padding(vertical = 18.dp)
        .clickable {
            onSystemImMessageClick(session, imMessage)
        }
    ) {
        Text(text = contentObject.hello, maxLines = 5, overflow = TextOverflow.Ellipsis)
    }
    AnimatedContent(
        targetState = imMessage.handling.value,
        label = "accept_join_group_handing"
    ) { handling ->
        Box(modifier = Modifier.fillMaxWidth()) {
            if (handling) {
                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "处理中")
                }
            } else {
                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    Button(onClick = {
                        onUserRequestClick(true, session, imMessage)
                    }) {
                        Text(text = "同意")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        onUserRequestClick(false, session, imMessage)
                    }) {
                        Text(text = "拒绝")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun FriendRequestCard(
    session: Session,
    imMessage: ImMessage.System,
    contentObject: SystemContentInterface.FriendRequestJson,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onUserAvatarClick: (String) -> Unit,
    onUserRequestClick: (Boolean, Session, ImMessage.System) -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        LocalOrRemoteImage(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(4.dp)),
            any = imMessage.fromUserInfo.avatarUrl,
            defaultColor = MaterialTheme.colorScheme.secondaryContainer
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = "新朋友申请",
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f),
            maxLines = 1, overflow = TextOverflow.Ellipsis, fontSize = 13.sp
        )
        Text(text = imMessage.dateStr ?: "", fontSize = 10.sp)
    }
    Spacer(modifier = Modifier.height(20.dp))
    Row(modifier = Modifier.clickable {
        onUserAvatarClick(contentObject.uid)
    }) {
        LocalOrRemoteImage(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp)),
            any = contentObject.avatarUrl,
            defaultColor = MaterialTheme.colorScheme.secondaryContainer
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = contentObject.name ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = 13.sp
        )
    }
    Column(modifier = Modifier
        .padding(vertical = 18.dp)
        .clickable {
            onSystemImMessageClick(session, imMessage)
        }
    ) {
        Text(text = contentObject.hello, maxLines = 5, overflow = TextOverflow.Ellipsis)
    }
    AnimatedContent(
        targetState = imMessage.handling.value,
        label = "accept_request_friend_handing"
    ) { handling ->
        Box(modifier = Modifier.fillMaxWidth()) {
            if (handling) {
                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    CircularProgressIndicator(modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = "处理中")
                }
            } else {
                Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                    Button(onClick = {
                        onUserRequestClick(true, session, imMessage)
                    }) {
                        Text(text = "同意")
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Button(onClick = {
                        onUserRequestClick(false, session, imMessage)
                    }) {
                        Text(text = "拒绝")
                    }
                }
            }
        }
    }
}
