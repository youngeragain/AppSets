package xcj.app.appsets.ui.compose.conversation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.model.FriendRequestFeedbackJson
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupJoinRequestFeedbackJson
import xcj.app.appsets.im.model.GroupRequestJson
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.components.DesignVDivider
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.ShowNavBarWhenOnLaunch
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.SessionState
import xcj.app.appsets.util.DesignRecorder

private const val TAG = "ConversationOverviewPage"

@Composable
fun ConversationOverviewPage(
    sessionState: SessionState,
    isShowAddActions: Boolean,
    recorderState: DesignRecorder.AudioRecorderState,
    onBioClick: (Bio) -> Unit,
    onImMessageContentClick: (ImMessage) -> Unit,
    onAddFriendClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onConversionSessionClick: (Session, Boolean) -> Unit,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onUserRequestClick: (Boolean, Session, SystemMessage) -> Unit,
    onInputMoreAction: (String) -> Unit,
    onVoiceAction: () -> Unit,
    onVoiceStopClick: (Boolean) -> Unit,
    onVoicePauseClick: () -> Unit,
    onVoiceResumeClick: () -> Unit,
    onMoreClick: ((ImObj) -> Unit),
    onLandscapeModeEndBackClick: () -> Unit
) {
    ShowNavBarWhenOnLaunch()
    if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
        ConversationOverviewPortrait(
            isShowAddActions = isShowAddActions,
            onAddFriendClick = onAddFriendClick,
            onJoinGroupClick = onJoinGroupClick,
            onCreateGroupClick = onCreateGroupClick,
            onConversionSessionClick = { session ->
                onConversionSessionClick(session, true)
            },
            onSystemImMessageClick = onSystemImMessageClick,
            onBioClick = onBioClick,
            onUserRequestClick = onUserRequestClick
        )
    } else {
        ConversationOverviewLandscape(
            isShowAddActions = isShowAddActions,
            sessionState = sessionState,
            recorderState = recorderState,
            onAddFriendClick = onAddFriendClick,
            onJoinGroupClick = onJoinGroupClick,
            onCreateGroupClick = onCreateGroupClick,
            onConversionSessionClick = { session ->
                onConversionSessionClick(session, false)
            },
            onSystemImMessageClick = onSystemImMessageClick,
            onBioClick = onBioClick,
            onImMessageContentClick = onImMessageContentClick,
            onUserRequestClick = onUserRequestClick,
            onInputMoreAction = onInputMoreAction,
            onVoiceAction = onVoiceAction,
            onVoiceStopClick = onVoiceStopClick,
            onVoicePauseClick = onVoicePauseClick,
            onVoiceResumeClick = onVoiceResumeClick,
            onMoreClick = onMoreClick,
            onLandscapeModeEndBackClick = onLandscapeModeEndBackClick
        )
    }
}

@Composable
fun ConversationOverviewLandscapeEnd(
    sessionState: SessionState,
    recorderState: DesignRecorder.AudioRecorderState,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onImMessageContentClick: (ImMessage) -> Unit,
    onMoreClick: ((ImObj) -> Unit),
    onInputMoreAction: (String) -> Unit,
    onVoiceAction: () -> Unit,
    onVoiceStopClick: (Boolean) -> Unit,
    onVoicePauseClick: () -> Unit,
    onVoiceResumeClick: () -> Unit,
) {
    AnimatedContent(
        targetState = sessionState,
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(),
                initialOffsetX = {
                    it / 20
                }
            ) + fadeIn() togetherWith slideOutHorizontally(
                animationSpec = tween(),
                targetOffsetX = {
                    -it / 20
                }
            ) + fadeOut()
        },
        label = "conversation_details_panel_animate"
    ) { targetSessionState ->
        when (targetSessionState) {
            is SessionState.None -> {
                SessionObjectNotFound()
            }

            is SessionState.Normal -> {
                ConversationDetailsPage(
                    sessionState = targetSessionState,
                    recorderState = recorderState,
                    onBackClick = onBackClick,
                    onBioClick = onBioClick,
                    onImMessageContentClick = onImMessageContentClick,
                    onMoreClick = onMoreClick,
                    onInputMoreAction = onInputMoreAction,
                    onVoiceAction = onVoiceAction,
                    onVoiceStopClick = onVoiceStopClick,
                    onVoicePauseClick = onVoicePauseClick,
                    onVoiceResumeClick = onVoiceResumeClick
                )
            }
        }
    }
}

@Composable
fun ConversationOverviewLandscape(
    modifier: Modifier = Modifier,
    sessionState: SessionState,
    isShowAddActions: Boolean,
    recorderState: DesignRecorder.AudioRecorderState,
    onAddFriendClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onConversionSessionClick: (Session) -> Unit,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit,
    onImMessageContentClick: (ImMessage) -> Unit,
    onUserRequestClick: (Boolean, Session, SystemMessage) -> Unit,
    onInputMoreAction: (String) -> Unit,
    onVoiceAction: () -> Unit,
    onVoiceStopClick: (Boolean) -> Unit,
    onVoicePauseClick: () -> Unit,
    onVoiceResumeClick: () -> Unit,
    onMoreClick: ((ImObj) -> Unit),
    onLandscapeModeEndBackClick: () -> Unit,
) {
    Row(modifier.fillMaxSize()) {
        ConversationOverviewPortrait(
            Modifier.width(410.dp),
            isShowAddActions,
            onAddFriendClick,
            onJoinGroupClick,
            onCreateGroupClick,
            onConversionSessionClick,
            onSystemImMessageClick,
            onBioClick,
            onUserRequestClick
        )
        DesignVDivider()
        Box(
            Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            ConversationOverviewLandscapeEnd(
                sessionState = sessionState,
                recorderState = recorderState,
                onBackClick = onLandscapeModeEndBackClick,
                onBioClick = onBioClick,
                onImMessageContentClick = onImMessageContentClick,
                onMoreClick = onMoreClick,
                onInputMoreAction = onInputMoreAction,
                onVoiceAction = onVoiceAction,
                onVoiceStopClick = onVoiceStopClick,
                onVoicePauseClick = onVoicePauseClick,
                onVoiceResumeClick = onVoiceResumeClick
            )
        }
    }
}

@Composable
fun ConversationOverviewPortrait(
    modifier: Modifier = Modifier,
    isShowAddActions: Boolean,
    onAddFriendClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onCreateGroupClick: () -> Unit,
    onConversionSessionClick: (Session) -> Unit,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit,
    onUserRequestClick: (Boolean, Session, SystemMessage) -> Unit
) {
    val tabs = remember {
        listOf(
            ConversationUseCase.AI,
            ConversationUseCase.USER,
            ConversationUseCase.GROUP,
            ConversationUseCase.SYSTEM,
        )
    }
    val conversationUseCase = LocalUseCaseOfConversation.current
    val pagerState =
        rememberPagerState(tabs.indexOf(conversationUseCase.currentTab.value)) { tabs.size }
    val scope = rememberCoroutineScope()
    Column(
        modifier = modifier
    ) {
        ConversationOverviewTabs(
            tabs = tabs,
            currentTab = tabs[pagerState.currentPage],
            isShowAddActions = isShowAddActions,
            onTabClick = { tab ->
                scope.launch {
                    pagerState.animateScrollToPage(tabs.indexOf(tab))
                }
            },
            onAddFriendClick = onAddFriendClick,
            onJoinGroupClick = onJoinGroupClick,
            onCreateGroupClick = onCreateGroupClick
        )
        LaunchedEffect(pagerState.currentPage) {
            conversationUseCase.updateCurrentTab(tabs[pagerState.currentPage])
        }
        HorizontalPager(
            modifier = Modifier.weight(1f),
            state = pagerState
        ) { index ->
            val currentTab = tabs[index]
            when (currentTab) {
                ConversationUseCase.AI -> {
                    ConversationOverviewSessionsOfAI(
                        onConversionSessionClick = onConversionSessionClick
                    )
                }

                ConversationUseCase.USER -> {
                    ConversationOverviewSessionsOfUser(
                        onConversionSessionClick = onConversionSessionClick
                    )
                }

                ConversationUseCase.GROUP -> {
                    ConversationOverviewSessionsOfGroup(
                        onConversionSessionClick = onConversionSessionClick
                    )
                }

                ConversationUseCase.SYSTEM -> {
                    ConversationOverviewSessionsOfSystem(
                        onSystemImMessageClick = onSystemImMessageClick,
                        onBioClick = onBioClick,
                        onUserRequestClick = onUserRequestClick
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationOverviewSessionsOfSystem(
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit,
    onUserRequestClick: (Boolean, Session, SystemMessage) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        val conversationUseCase = LocalUseCaseOfConversation.current
        val sessions = conversationUseCase.currentTabSessions()
        val currentTab = conversationUseCase.currentTab.value
        if (sessions.flatMap { it.conversationState.messages }.isEmpty()) {
            Text(
                text = stringResource(getEmptyPromptTextFor(currentTab)),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 98.dp)
            ) {
                itemsIndexed(sessions) { index, session ->
                    ConversationOverviewSystemImMessageItemComponent(
                        modifier = Modifier.animateItem(),
                        session = session,
                        onSystemImMessageClick = onSystemImMessageClick,
                        onBioClick = onBioClick,
                        onUserRequestClick = onUserRequestClick
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationOverviewSessionsOfGroup(
    onConversionSessionClick: (Session) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        val conversationUseCase = LocalUseCaseOfConversation.current
        val sessions = conversationUseCase.currentTabSessions()
        val currentTab = conversationUseCase.currentTab.value
        if (sessions.isEmpty()) {
            Text(
                text = stringResource(getEmptyPromptTextFor(currentTab)),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 98.dp)
            ) {
                itemsIndexed(sessions) { index, session ->
                    ConversationOverviewSimpleItemComponent(
                        modifier = Modifier.animateItem(),
                        currentTab = currentTab,
                        session = session,
                        onConversionSessionClick = onConversionSessionClick
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationOverviewSessionsOfUser(
    onConversionSessionClick: (Session) -> Unit
) {
    Box(Modifier.fillMaxSize()) {
        val conversationUseCase = LocalUseCaseOfConversation.current
        val sessions = conversationUseCase.currentTabSessions()
        val currentTab = conversationUseCase.currentTab.value
        if (sessions.isEmpty()) {
            Text(
                text = stringResource(getEmptyPromptTextFor(currentTab)),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 98.dp)
            ) {
                itemsIndexed(sessions) { index, session ->
                    ConversationOverviewSimpleItemComponent(
                        modifier = Modifier.animateItem(),
                        currentTab = currentTab,
                        session = session,
                        onConversionSessionClick = onConversionSessionClick
                    )
                }
            }
        }
    }
}

@Composable
fun ConversationOverviewSessionsOfAI(
    onConversionSessionClick: (Session) -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        val conversationUseCase = LocalUseCaseOfConversation.current
        val sessions = conversationUseCase.currentTabSessions()
        val currentTab = conversationUseCase.currentTab.value
        if (sessions.isEmpty()) {
            Text(
                text = stringResource(getEmptyPromptTextFor(currentTab)),
                fontSize = 12.sp,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 98.dp)
            ) {
                itemsIndexed(sessions) { index, session ->
                    ConversationOverviewSimpleItemComponent(
                        modifier = Modifier.animateItem(),
                        currentTab = currentTab,
                        session = session,
                        onConversionSessionClick = onConversionSessionClick
                    )
                }
            }
        }
    }
}


@Composable
fun ConversationOverviewTabs(
    tabs: List<String>,
    currentTab: String,
    isShowAddActions: Boolean,
    onTabClick: (String) -> Unit,
    onAddFriendClick: () -> Unit,
    onJoinGroupClick: () -> Unit,
    onCreateGroupClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
        AnimatedVisibility(
            visible = isShowAddActions,
            enter = expandVertically(
                animationSpec = tween(),
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = tween(),
            ),
            content = {
                ConversationOverviewAddActionsComponent(
                    onAddFriendClick = onAddFriendClick,
                    onJoinGroupClick = onJoinGroupClick,
                    onCreateGroupClick = onCreateGroupClick
                )
            }
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tabs.forEachIndexed { index, tab ->
                FilterChip(
                    selected = currentTab == tab,
                    onClick = {
                        onTabClick(tab)
                    },
                    label = {
                        Text(text = stringResource(getTabTitleText(tab)), fontSize = 12.sp)
                    },
                    shape = CircleShape,
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = MaterialTheme.colorScheme.outline,
                        enabled = true,
                        selected = currentTab == tab
                    )
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        DesignHDivider()
        Spacer(modifier = Modifier.height(6.dp))
    }
}

@Composable
fun ConversationOverviewSystemImMessageItemComponent(
    modifier: Modifier,
    session: Session,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit,
    onUserRequestClick: (Boolean, Session, SystemMessage) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        session.conversationState.messages.forEach { message ->
            if (message is SystemMessage) {
                SystemImMessageContent(
                    session,
                    message,
                    onSystemImMessageClick,
                    onBioClick,
                    onUserRequestClick
                )
            }
        }
    }

}

@Composable
fun ConversationOverviewSimpleItemComponent(
    modifier: Modifier,
    currentTab: String,
    session: Session,
    onConversionSessionClick: (Session) -> Unit
) {
    Row(
        modifier = modifier
            .clickable {
                onConversionSessionClick(session)
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        AnyImage(
            modifier = Modifier
                .size(48.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline,
                    shape = MaterialTheme.shapes.large
                )
                .clip(MaterialTheme.shapes.large),
            any = session.imObj.avatarUrl,
            defaultColor = MaterialTheme.colorScheme.outline
        )
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            //名称
            Text(
                text = session.imObj.name,
                fontSize = 14.sp
            )
            if (session.isO2O) {
                val context = LocalContext.current
                Text(
                    text = ImMessage.readableContent(context, session.latestImMessage)
                        ?: stringResource(xcj.app.appsets.R.string.no_news),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                Row(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val imMessage = session.latestImMessage
                    if (imMessage == null) {
                        Text(
                            text = stringResource(xcj.app.appsets.R.string.no_news),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        AnyImage(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(MaterialTheme.shapes.small),
                            any = imMessage.fromInfo.bioUrl,
                            defaultColor = MaterialTheme.colorScheme.outline
                        )
                        val context = LocalContext.current
                        Text(
                            text = ImMessage.readableContent(context, imMessage)
                                ?: stringResource(xcj.app.appsets.R.string.no_news),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                }
            }
            Spacer(Modifier.height(6.dp))
        }
        val latestImMessage = session.latestImMessage
        if (latestImMessage != null) {
            Row {
                Text(latestImMessage.readableDate, fontSize = 10.sp)
            }
        }
        if (currentTab != ConversationUseCase.AI
            && currentTab != ConversationUseCase.SYSTEM
            && !session.imObj.isRelated
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                DesignVDivider()
                val textOverviewSessionEndShowTextId =
                    if (session.imObj.id == LocalAccountManager.userInfo.id) {
                        xcj.app.appsets.R.string.i
                    } else {
                        xcj.app.appsets.R.string.temporary
                    }
                Text(
                    text = stringResource(id = textOverviewSessionEndShowTextId),
                    fontSize = 10.sp
                )
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
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = MaterialTheme.shapes.extraLarge
                )
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(xcj.app.appsets.R.string.add_friend),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface, CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onAddFriendClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                fontSize = 12.sp
            )
            Text(
                text = stringResource(xcj.app.appsets.R.string.add_group),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface, CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onJoinGroupClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                fontSize = 12.sp
            )
            Text(
                text = stringResource(xcj.app.appsets.R.string.create_group),
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface, CircleShape
                    )
                    .clip(CircleShape)
                    .clickable(onClick = onCreateGroupClick)
                    .padding(horizontal = 12.dp, vertical = 12.dp), fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SystemImMessageContent(
    session: Session,
    imMessage: SystemMessage,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit,
    onUserRequestClick: (Boolean, Session, SystemMessage) -> Unit
) {
    Box(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraLarge)
            .border(
                1.dp,
                MaterialTheme.colorScheme.outline,
                MaterialTheme.shapes.extraLarge
            )
    ) {
        Column(Modifier.padding(16.dp)) {
            val systemContentInterface = imMessage.systemContentInterface
            when (systemContentInterface) {
                is FriendRequestJson -> {
                    FriendRequestCard(
                        session,
                        imMessage,
                        systemContentInterface,
                        onSystemImMessageClick,
                        onBioClick,
                        onUserRequestClick
                    )
                }

                is GroupRequestJson -> {
                    GroupRequestCard(
                        session,
                        imMessage,
                        systemContentInterface,
                        onSystemImMessageClick,
                        onBioClick,
                        onUserRequestClick
                    )
                }

                is FriendRequestFeedbackJson -> {
                    FriendRequestFeedbackCard(
                        session,
                        imMessage,
                        systemContentInterface,
                        onSystemImMessageClick,
                        onBioClick
                    )
                }

                is GroupJoinRequestFeedbackJson -> {
                    GroupJoinRequestFeedbackCard(
                        session,
                        imMessage,
                        systemContentInterface,
                        onSystemImMessageClick,
                        onBioClick
                    )
                }

                else -> Unit
            }
        }
    }
}

@Composable
fun GroupJoinRequestFeedbackCard(
    session: Session,
    imMessage: SystemMessage,
    groupJoinRequestFeedbackJson: GroupJoinRequestFeedbackJson,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnyImage(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp)),
                any = imMessage.fromInfo.bioUrl,
                defaultColor = MaterialTheme.colorScheme.secondaryContainer
            )
            Text(
                text = ImMessage.readableContent(context, imMessage) ?: "",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
            Text(text = imMessage.readableDate, fontSize = 10.sp)
        }
    }
}

@Composable
fun FriendRequestFeedbackCard(
    session: Session,
    imMessage: SystemMessage,
    friendRequestFeedbackJson: FriendRequestFeedbackJson,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit
) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AnyImage(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(4.dp)),
                any = imMessage.fromInfo.bioUrl,
                defaultColor = MaterialTheme.colorScheme.secondaryContainer
            )
            Text(
                text = ImMessage.readableContent(context, imMessage) ?: "",
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
            Text(text = imMessage.readableDate, fontSize = 10.sp)
        }
    }
}

@Composable
fun GroupRequestCard(
    session: Session,
    imMessage: SystemMessage,
    groupRequestJson: GroupRequestJson,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit,
    onUserRequestClick: (Boolean, Session, SystemMessage) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnyImage(
                modifier = Modifier
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    ),
                any = imMessage.fromInfo.bioUrl
            )
            Text(
                text = stringResource(xcj.app.appsets.R.string.apply_to_join_the_group),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
            Text(text = imMessage.readableDate, fontSize = 10.sp)
        }
        Row(
            modifier = Modifier

                .clip(MaterialTheme.shapes.extraLarge)
                .border(
                    1.dp, MaterialTheme.colorScheme.outline, MaterialTheme.shapes.extraLarge
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable(
                        onClick = {
                            val userInfo = UserInfo.basic(
                                groupRequestJson.uid,
                                groupRequestJson.name,
                                groupRequestJson.avatarUrl
                            )
                            onBioClick(userInfo)
                        }
                    ),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnyImage(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        ),
                    any = groupRequestJson.avatarUrl
                )
                Text(
                    text = groupRequestJson.name ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
            }
            Icon(
                modifier = Modifier.weight(1f),
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_forward_24),
                contentDescription = stringResource(xcj.app.appsets.R.string.join_group)
            )
            Row(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        val groupInfo = GroupInfo.basic(
                            groupRequestJson.groupId,
                            groupRequestJson.groupName,
                            groupRequestJson.groupIconUrl
                        )
                        onBioClick(groupInfo)
                    },
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AnyImage(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        ),
                    any = groupRequestJson.groupIconUrl
                )
                Text(
                    text = groupRequestJson.groupName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 13.sp
                )
            }
        }
        Text(
            text = groupRequestJson.hello,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = imMessage.handling.value,
                label = "accept_join_group_handle_animate"
            ) { handling ->

                if (handling) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(text = stringResource(xcj.app.appsets.R.string.processing))
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                onUserRequestClick(true, session, imMessage)
                            }
                        ) {
                            Text(text = stringResource(xcj.app.appsets.R.string.agree))
                        }
                        FilledTonalButton(
                            onClick = {
                                onUserRequestClick(false, session, imMessage)
                            }
                        ) {
                            Text(text = stringResource(xcj.app.starter.R.string.reject))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FriendRequestCard(
    session: Session,
    imMessage: SystemMessage,
    friendRequestJson: FriendRequestJson,
    onSystemImMessageClick: (Session, ImMessage) -> Unit,
    onBioClick: (Bio) -> Unit,
    onUserRequestClick: (Boolean, Session, SystemMessage) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AnyImage(
                modifier = Modifier
                    .size(24.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    ),
                any = imMessage.fromInfo.bioUrl
            )
            Text(
                text = stringResource(xcj.app.appsets.R.string.new_friend_application),
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
            Text(text = imMessage.readableDate, fontSize = 10.sp)
        }
        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.extraLarge)
                .clickable {
                    val userInfo = UserInfo.basic(
                        friendRequestJson.uid,
                        friendRequestJson.name,
                        friendRequestJson.avatarUrl
                    )
                    onBioClick(userInfo)
                },
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnyImage(
                modifier = Modifier
                    .size(36.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    ),
                any = friendRequestJson.avatarUrl,
                defaultColor = MaterialTheme.colorScheme.secondaryContainer
            )
            Text(
                text = friendRequestJson.name ?: "",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 13.sp
            )
        }
        Text(
            text = friendRequestJson.hello,
            maxLines = 5,
            overflow = TextOverflow.Ellipsis,
            fontSize = 12.sp
        )

        Box(modifier = Modifier.fillMaxWidth()) {
            AnimatedContent(
                targetState = imMessage.handling.value,
                label = "accept_request_friend_handle_animate"
            ) { handling ->
                if (handling) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        Text(text = stringResource(id = xcj.app.appsets.R.string.processing))
                    }
                } else {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FilledTonalButton(
                            onClick = {
                                onUserRequestClick(true, session, imMessage)
                            }
                        ) {
                            Text(text = stringResource(id = xcj.app.appsets.R.string.agree))
                        }
                        FilledTonalButton(
                            onClick = {
                                onUserRequestClick(false, session, imMessage)
                            }
                        ) {
                            Text(text = stringResource(id = xcj.app.starter.R.string.reject))
                        }
                    }
                }
            }
        }
    }
}

private fun getEmptyPromptTextFor(tab: String): Int {
    return when (tab) {
        ConversationUseCase.USER -> {
            xcj.app.appsets.R.string.empty_personal_list
        }

        ConversationUseCase.GROUP -> {
            xcj.app.appsets.R.string.empty_group_list
        }

        ConversationUseCase.SYSTEM -> {
            xcj.app.appsets.R.string.empty_system_message_list
        }

        else -> {
            xcj.app.appsets.R.string.empty_ai_message_list
        }
    }
}

private fun getTabTitleText(tab: String): Int {
    return when (tab) {
        ConversationUseCase.AI -> xcj.app.appsets.R.string.generative_ai
        ConversationUseCase.USER -> xcj.app.appsets.R.string.personal
        ConversationUseCase.GROUP -> xcj.app.appsets.R.string.group
        ConversationUseCase.SYSTEM -> xcj.app.appsets.R.string.system
        else -> xcj.app.appsets.R.string.no_content
    }
}
