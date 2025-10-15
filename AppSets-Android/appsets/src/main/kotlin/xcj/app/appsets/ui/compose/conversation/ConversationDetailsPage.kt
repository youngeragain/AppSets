@file:OptIn(ExperimentalHazeMaterialsApi::class, ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.conversation

import android.net.Uri
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.ReceiveContentListener
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil3.compose.rememberAsyncImagePainter
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.IMObj
import xcj.app.appsets.im.InputSelector
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.AdMessage
import xcj.app.appsets.im.message.FileMessage
import xcj.app.appsets.im.message.HTMLMessage
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.im.message.ImageMessage
import xcj.app.appsets.im.message.LocationMessage
import xcj.app.appsets.im.message.MusicMessage
import xcj.app.appsets.im.message.TextMessage
import xcj.app.appsets.im.message.VideoMessage
import xcj.app.appsets.im.message.VoiceMessage
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.BackPressHandler
import xcj.app.appsets.ui.compose.custom_component.HideNavBar
import xcj.app.appsets.ui.compose.custom_component.third_part.waveslider.WaveSlider
import xcj.app.appsets.ui.compose.custom_component.third_part.waveslider.WaveSliderDefaults
import xcj.app.appsets.usecase.SessionState
import xcj.app.appsets.util.DesignRecorder
import xcj.app.appsets.util.ktx.asComponentActivityOrNull
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.components.DesignHDivider
import xcj.app.compose_share.modifier.combinedClickableSingle
import xcj.app.starter.android.ktx.startWithHttpSchema
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "ConversationDetailsPage"

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@OptIn(ExperimentalHazeMaterialsApi::class)
@Composable
fun ConversationDetailsPage(
    sessionState: SessionState,
    recorderState: DesignRecorder.AudioRecorderState,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
    onInputMoreAction: (String) -> Unit,
    onVoiceAction: () -> Unit,
    onVoiceStopClick: (Boolean) -> Unit,
    onVoicePauseClick: () -> Unit,
    onVoiceResumeClick: () -> Unit,
    onMoreClick: ((IMObj) -> Unit),
) {
    AnimatedContent(
        targetState = sessionState,
        transitionSpec = {
            fadeIn(tween()) togetherWith fadeOut(tween())
        }
    ) { targetSessionState ->
        when (targetSessionState) {
            is SessionState.None -> {
                SessionObjectNotFound()
            }

            is SessionState.Normal -> {
                SessionObjectNormal(
                    sessionState = targetSessionState,
                    recorderState = recorderState,
                    onBackClick = onBackClick,
                    onBioClick = onBioClick,
                    onImMessageContentClick = onImMessageContentClick,
                    onInputMoreAction = onInputMoreAction,
                    onVoiceAction = onVoiceAction,
                    onVoiceStopClick = onVoiceStopClick,
                    onVoicePauseClick = onVoicePauseClick,
                    onVoiceResumeClick = onVoiceResumeClick,
                    onMoreClick = onMoreClick
                )
            }
        }
    }
}

@Composable
fun SessionObjectNotFound() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(stringResource(xcj.app.appsets.R.string.can_not_found_session_object))
    }
}

@Composable
fun SessionObjectNormal(
    sessionState: SessionState.Normal,
    recorderState: DesignRecorder.AudioRecorderState,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
    onInputMoreAction: (String) -> Unit,
    onVoiceAction: () -> Unit,
    onVoiceStopClick: (Boolean) -> Unit,
    onVoicePauseClick: () -> Unit,
    onVoiceResumeClick: () -> Unit,
    onMoreClick: ((IMObj) -> Unit),
) {
    HideNavBar()
    val conversationUseCase = LocalUseCaseOfConversation.current

    val context = LocalContext.current

    var searchKeywords by remember {
        mutableStateOf("")
    }
    val quickAccessSessions by remember {
        derivedStateOf<List<Session>> {
            conversationUseCase.getAllSimpleSessionsByKeywords(searchKeywords)
        }
    }
    val scrollState = rememberLazyListState()
    val session = sessionState.session
    val conversationState = session.conversationState
    val coroutineScope = rememberCoroutineScope()
    val complexContentSending by conversationUseCase.complexContentSendingState

    val hapticFeedback = LocalHapticFeedback.current


    val isShowJumpToLatestButton by remember {
        derivedStateOf {
            scrollState.firstVisibleItemIndex != 0 ||
                    scrollState.firstVisibleItemScrollOffset > 50
        }
    }

    val hazeState = remember { HazeState() }

    var inputTextFiledValue by remember { mutableStateOf(TextFieldValue()) }

    val receivedContents = remember { mutableStateListOf<Uri>() }

    val textFieldAdviser = remember {
        TextFieldAdviser()
    }

    // Remember the ReceiveContentListener object as it is created inside a Composable scope
    val receiveContentListener = remember {
        ReceiveContentListener { transferableContent ->
            PurpleLogger.current.d(TAG, "onReceive")
            receivedContents.clear()
            transferableContent.consume { clipDataItem ->
                val uri = clipDataItem.uri
                if (uri != null) {
                    receivedContents.add(uri)
                    return@consume true
                } else if (!clipDataItem.text.isNullOrEmpty()) {
                    inputTextFiledValue = TextFieldValue(clipDataItem.text.toString())
                    return@consume true
                } else {
                    return@consume false
                }
            }
        }
    }

    val appSetsModuleSettings = remember {
        AppSetsModuleSettings.get()
    }


    DisposableEffect(Unit) {
        onDispose {
            conversationUseCase.onComposeDispose("page dispose")
        }
    }

    LaunchedEffect(complexContentSending) {
        if (complexContentSending) {
            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    LaunchedEffect(inputTextFiledValue, receivedContents.size) {
        coroutineScope.launch {
            textFieldAdviser.makeAdviser(inputTextFiledValue, receivedContents)
        }
    }

    Box {
        ImMessageListComponent(
            modifier = Modifier
                .imePadding(),
            appSetsModuleSettings = appSetsModuleSettings,
            session = session,
            messages = conversationState.messages,
            scrollState = scrollState,
            hazeState = hazeState,
            onBioClick = onBioClick,
            onImMessageContentClick = onImMessageContentClick
        )
        TopBarComponent(
            modifier = Modifier
                .align(Alignment.TopCenter),
            session = session,
            quickAccessSessions = quickAccessSessions,
            hazeState = hazeState,
            isShowJumpToLatestButton = isShowJumpToLatestButton,
            onBackClick = onBackClick,
            onMoreClick = {
                onMoreClick(session.imObj)
            },
            onJumpToLatestClick = {
                coroutineScope.launch {
                    if (scrollState.firstVisibleItemIndex > 25) {
                        scrollState.scrollToItem(0)
                    } else {
                        scrollState.animateScrollToItem(0)
                    }
                }
            },
            onBioClick = onBioClick,
            onQuickAccessSessionClick = { quickAccessSession ->
                conversationUseCase.updateCurrentSessionBySession(quickAccessSession)
            }
        )
        UserInputComponent(
            // Use navigationBarsPadding() imePadding() and , to move the input panel above both the
            // navigation bar, and on-screen keyboard (IME)
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .imePadding(),
            hazeState = hazeState,
            inputTextState = inputTextFiledValue,
            textFieldAdviser = textFieldAdviser,
            receiveContentListener = receiveContentListener,
            recorderState = recorderState,
            onTextChanged = {
                inputTextFiledValue = it
                if (it.text.isEmpty()) {
                    searchKeywords = ""
                }
            },
            onSendClick = { inputSelector ->
                if (!inputTextFiledValue.text.isEmpty() && !inputTextFiledValue.text.isBlank()) {
                    conversationUseCase.sendMessage(
                        context,
                        inputSelector,
                        inputTextFiledValue.text
                    )
                }
                // Reset text field and close keyboard
                inputTextFiledValue = TextFieldValue()
                // Move scroll to bottom
            },
            onSearchIconClick = { keywords ->
                searchKeywords = keywords
            },
            resetScroll = {
                PurpleLogger.current.d(TAG, "reset scroll")
                coroutineScope.launch {
                    delay(200)
                    if (scrollState.firstVisibleItemIndex > 20) {
                        scrollState.scrollToItem(0)
                    } else {
                        scrollState.animateScrollToItem(0)
                    }
                }
            },
            onInputMoreAction = onInputMoreAction,
            onVoiceAction = onVoiceAction,
            onVoiceStopClick = onVoiceStopClick,
            onVoicePauseClick = onVoicePauseClick,
            onVoiceResumeClick = onVoiceResumeClick,
            onRemoveAdviseClick = { advise ->
                textFieldAdviser.removeAdvise(advise)
                //remove associate content in receiveContents
            }

        )

        ComplexContentSendingIndicator(isShow = complexContentSending)
    }
}

@Composable
fun ComplexContentSendingIndicator(isShow: Boolean) {
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
                    .background(
                        shape = MaterialTheme.shapes.extraLarge,
                        color = MaterialTheme.colorScheme.surface
                    )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .padding(vertical = 32.dp, horizontal = 42.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        painterResource(xcj.app.compose_share.R.drawable.ic_ios_share_24),
                        contentDescription = null
                    )
                    Text(stringResource(xcj.app.appsets.R.string.processing), fontSize = 12.sp)
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TopBarComponent(
    modifier: Modifier,
    session: Session,
    quickAccessSessions: List<Session>,
    hazeState: HazeState,
    isShowJumpToLatestButton: Boolean,
    onBackClick: () -> Unit,
    onMoreClick: () -> Unit,
    onJumpToLatestClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onQuickAccessSessionClick: (Session) -> Unit,
) {
    val hapticFeedback = LocalHapticFeedback.current
    Column(
        modifier = modifier
            .fillMaxWidth()
    ) {
        TopToolBar(
            session = session,
            hazeState = hazeState,
            onBackClick = onBackClick,
            onBioClick = onBioClick,
            onMoreClick = onMoreClick
        )

        if (quickAccessSessions.isNotEmpty()) {
            LazyRow(
                modifier = Modifier
                    .animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    if (isShowJumpToLatestButton) {
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                                .hazeEffect(
                                    hazeState,
                                    HazeMaterials.thin()
                                )
                                .clickable {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onJumpToLatestClick()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .animateItem(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_keyboard_arrow_down_24),
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape),
                                contentDescription = null
                            )
                            Text(
                                modifier = Modifier,
                                text = stringResource(xcj.app.appsets.R.string.show_latest),
                                fontSize = 12.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
                items(quickAccessSessions) { session ->
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            .hazeEffect(
                                hazeState,
                                HazeMaterials.thin()
                            )
                            .clickable {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onQuickAccessSessionClick(session)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .animateItem(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UserAvatar2Component(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape),
                            imObj = session.imObj
                        )
                        Text(
                            modifier = Modifier,
                            text = session.imObj.name,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun TopToolBar(
    session: Session,
    hazeState: HazeState,
    onBackClick: () -> Unit,
    onBioClick: (Bio) -> Unit,
    onMoreClick: () -> Unit,
) {
    Column(
        modifier = Modifier.hazeEffect(
            hazeState,
            HazeMaterials.thin()
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_arrow_back_24),
                contentDescription = stringResource(id = xcj.app.appsets.R.string.return_),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onBackClick)
                    .padding(12.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier
                    .clickable {
                        onBioClick(session.imObj.bio)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                UserAvatar2Component(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(MaterialTheme.shapes.extraLarge),
                    imObj = session.imObj
                )
                Text(
                    text = session.imObj.name,
                    modifier = Modifier,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                painterResource(id = xcj.app.compose_share.R.drawable.ic_outline_more_vert_24),
                stringResource(id = xcj.app.appsets.R.string.more),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(onClick = onMoreClick)
                    .padding(12.dp)
            )
        }
        DesignHDivider()
    }
}


@Composable
private fun UserAvatarComponent(modifier: Modifier, imMessage: IMMessage<*>) {
    AnyImage(
        modifier = modifier,
        model = imMessage.fromInfo.bioUrl,
        error = imMessage.fromInfo.bioName
    )
}

@Composable
private fun UserAvatar2Component(modifier: Modifier, imObj: IMObj?) {
    AnyImage(
        modifier = modifier,
        model = imObj?.avatarUrl,
        error = imObj?.name
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ImMessageListComponent(
    modifier: Modifier = Modifier,
    appSetsModuleSettings: AppSetsModuleSettings,
    session: Session,
    messages: List<IMMessage<*>>,
    hazeState: HazeState,
    scrollState: LazyListState,
    onBioClick: (Bio) -> Unit,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    Box(modifier = modifier) {
        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            // Add content padding so that the content can be scrolled (y-axis)
            // below the status bar + app bar
            // TODO: Get height from somewhere
            contentPadding = PaddingValues(
                top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding() + 150.dp,
                bottom = WindowInsets.navigationBars.asPaddingValues()
                    .calculateBottomPadding() + 150.dp
            ),
            modifier = Modifier
                .testTag("ConversationTestTag")
                .fillMaxSize()
                .hazeSource(hazeState)
        ) {
            itemsIndexed(
                items = messages,
                key = { index, imMessage -> index }
            ) { _, imMessage ->
                ImMessageItemWrapperComponent(
                    modifier = Modifier.animateItem(),
                    appSetsModuleSettings = appSetsModuleSettings,
                    session = session,
                    imMessage = imMessage,
                    onBioClick = onBioClick,
                    onImMessageContentClick = onImMessageContentClick
                )
            }
        }
    }
}

@Composable
private fun ImMessageItemWrapperComponent(
    modifier: Modifier,
    appSetsModuleSettings: AppSetsModuleSettings,
    session: Session,
    imMessage: IMMessage<*>,
    onBioClick: (Bio) -> Unit,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.messageBubbleBoxModifier(
                this@Box,
                appSetsModuleSettings,
                imMessage
            )
        ) {
            ImMessageItemStartComponent(appSetsModuleSettings, session, imMessage, onBioClick)
            ImMessageItemCenterComponent(appSetsModuleSettings, imMessage, onImMessageContentClick)
            ImMessageItemEndComponent(appSetsModuleSettings, session, imMessage, onBioClick)
        }
    }
}

@Composable
private fun Modifier.messageBubbleBoxModifier(
    boxScope: BoxScope,
    appSetsModuleSettings: AppSetsModuleSettings,
    imMessage: IMMessage<*>
): Modifier {
    with(boxScope) {
        return when (appSetsModuleSettings.imBubbleAlignment) {
            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_START -> {
                align(Alignment.CenterStart)
            }

            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_END -> {
                align(Alignment.CenterEnd)
            }

            else -> {
                if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
                    align(Alignment.CenterEnd)
                } else {
                    align(Alignment.CenterStart)
                }
            }
        }
    }
}

@Composable
private fun ImMessageItemCenterComponent(
    appSetsModuleSettings: AppSetsModuleSettings,
    imMessage: IMMessage<*>,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    val messageSendInfo = imMessage.messageSending?.sendInfoState?.value
    val horizontalAlignment = when (appSetsModuleSettings.imBubbleAlignment) {
        AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_START -> {
            Alignment.Start
        }

        AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_END -> {
            Alignment.End
        }

        else -> {
            if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
                Alignment.End
            } else {
                Alignment.Start
            }
        }
    }
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        if (appSetsModuleSettings.isImMessageShowDate) {
            Text(
                text = imMessage.readableDate,
                fontSize = 10.sp
            )
        } else {
            Spacer(modifier = Modifier.height(12.dp))
        }
        when (imMessage) {
            is TextMessage -> {
                SelectionContainer {
                    ImMessageItemTextComponent(
                        appSetsModuleSettings,
                        imMessage,
                        onImMessageContentClick
                    )
                }
            }

            is ImageMessage -> {
                ImMessageItemImageComponent(imMessage, onImMessageContentClick)
            }

            is VoiceMessage -> {
                ImMessageItemVoiceComponent(imMessage, onImMessageContentClick)
            }

            is MusicMessage -> {
                SelectionContainer {
                    ImMessageItemMusicComponent(imMessage, onImMessageContentClick)
                }
            }

            is VideoMessage -> {
                ImMessageItemVideoComponent(imMessage, onImMessageContentClick)
            }

            is FileMessage -> {
                SelectionContainer {
                    ImMessageItemFileComponent(imMessage, onImMessageContentClick)
                }
            }

            is HTMLMessage -> {
                SelectionContainer {
                    ImMessageItemHTMLComponent(imMessage, onImMessageContentClick)
                }
            }

            is AdMessage -> {
                ImMessageItemADComponent(imMessage, onImMessageContentClick)
            }

            is LocationMessage -> {
                SelectionContainer {
                    ImMessageItemLocationComponent(imMessage, onImMessageContentClick)
                }
            }
        }

        Row {
            if (messageSendInfo?.failureReason != null) {
                Text(text = stringResource(xcj.app.appsets.R.string.failure), fontSize = 10.sp)
            }
        }
    }
}

@Composable
private fun ImMessageItemEndComponent(
    appSetsModuleSettings: AppSetsModuleSettings,
    session: Session,
    imMessage: IMMessage<*>,
    onBioClick: (Bio) -> Unit,
) {
    Row(modifier = Modifier.padding(12.dp)) {
        when (appSetsModuleSettings.imBubbleAlignment) {
            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_END -> {
                UserAvatarComponent(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable {
                            onBioClick(imMessage.fromInfo)
                        },
                    imMessage
                )
            }

            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_START_END -> {
                if (
                    !session.isO2O && imMessage.fromInfo.uid == LocalAccountManager.userInfo.uid
                ) {
                    UserAvatarComponent(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(MaterialTheme.shapes.large)
                            .clickable {
                                onBioClick(imMessage.fromInfo)
                            },
                        imMessage
                    )
                }
            }
        }
    }
}

@Composable
private fun ImMessageItemLocationComponent(
    imMessage: LocationMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    Column(
        modifier = Modifier
            .imMessageBackgroundLocationModifier(
                imMessage = imMessage
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable {
                onImMessageContentClick(imMessage)
            }
    ) {
        Box {
            Column(
                modifier = Modifier
                    .width(150.dp)
                    .align(Alignment.Center)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        painter = painterResource(xcj.app.compose_share.R.drawable.ic_location_on_24),
                        contentDescription = "location"
                    )
                    Text(
                        text = stringResource(xcj.app.appsets.R.string.geographical_information),
                        fontSize = 12.sp
                    )
                }
                val imMessageMetadata = imMessage.metadata
                if (true) {
                    Text(text = imMessageMetadata.data.info ?: "", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ImMessageItemFileComponent(
    imMessage: FileMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    Column(
        modifier = Modifier
            .imMessageBackgroundFileModifier(
                imMessage = imMessage
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable {
                onImMessageContentClick(imMessage)
            }
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                contentDescription = "file"
            )
            Text(stringResource(xcj.app.appsets.R.string.download_file), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ImMessageItemADComponent(
    imMessage: AdMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    Column(
        modifier = Modifier
            .imMessageBackgroundADModifier(
                imMessage = imMessage
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable {
                onImMessageContentClick(imMessage)
                PurpleLogger.current.d(
                    TAG,
                    "ImMessage.Ad show url:[${imMessage.metadata.data}]"
                )
            }
    ) {
        Box(
            Modifier
                .size(150.dp)
        ) {
            Text(
                stringResource(xcj.app.appsets.R.string.advertisement),
                modifier = Modifier.align(Alignment.Center)
            )
        }

    }
}

@Composable
private fun ImMessageItemMusicComponent(
    imMessage: MusicMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
    var waveValue by remember {
        mutableFloatStateOf(0f)
    }
    var isPlaying by remember {
        mutableStateOf(false)
    }
    val audioPlayerState = mediaRemoteExoUseCase.audioPlayerState.value
    LaunchedEffect(audioPlayerState) {
        if (audioPlayerState.id == imMessage.id) {
            waveValue = audioPlayerState.progress
            isPlaying =
                mediaRemoteExoUseCase.isPlaying
        }
    }
    Column(
        modifier = Modifier
            .imMessageBackgroundMusicModifier(
                imMessage = imMessage
            )
            .width(200.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.End
        ) {
            Icon(
                painterResource(xcj.app.compose_share.R.drawable.ic_audiotrack_24),
                contentDescription = null
            )

        }
        Text(text = imMessage.metadata.description, fontSize = 14.sp)
        WaveSlider(
            waveValue,
            onValueChange = {
                waveValue = it
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            animationOptions = WaveSliderDefaults.animationOptions(animateWave = isPlaying),
            waveOptions = WaveSliderDefaults.waveOptions(
                amplitude = if (isPlaying) {
                    15f
                } else {
                    0f
                }
            ),
            colors = WaveSliderDefaults.colors(),
            steps = 0
        )
        IconButton(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            onClick = {
                onImMessageContentClick(imMessage)
            }
        ) {
            val playButtonRes = if (isPlaying) {
                xcj.app.compose_share.R.drawable.ic_round_pause_circle_filled_24
            } else {
                xcj.app.compose_share.R.drawable.ic_play_circle_filled_24
            }
            Icon(
                modifier = Modifier.size(42.dp),
                painter = painterResource(playButtonRes),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ImMessageItemVideoComponent(
    imMessage: VideoMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    val videoLocalData = imMessage.metadata.localData as? Pair<*, *>
    val localVideoFirstFrameUri = (videoLocalData?.second as? UriProvider)?.provideUri()
    Box(
        modifier = Modifier
            .imMessageBackgroundVideoModifier(
                imMessage = imMessage
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable {
                onImMessageContentClick(imMessage)
            },
        contentAlignment = Alignment.Center
    ) {
        AnyImage(
            modifier = Modifier
                .height(355.dp)
                .width(200.dp)
                .clip(MaterialTheme.shapes.extraLarge),
            model = imMessage.metadata.companionUrl,
            placeHolder = rememberAsyncImagePainter(localVideoFirstFrameUri),
            error = rememberAsyncImagePainter(localVideoFirstFrameUri)
        )
        IconButton(
            onClick = {
                onImMessageContentClick(imMessage)
            },
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Icon(
                painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                contentDescription = null,
            )
        }
    }
}

@Composable
private fun ImMessageItemVoiceComponent(
    imMessage: VoiceMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
    var waveValue by remember {
        mutableFloatStateOf(0f)
    }
    var isPlaying by remember {
        mutableStateOf(false)
    }
    val audioPlayerState = mediaRemoteExoUseCase.audioPlayerState.value
    LaunchedEffect(audioPlayerState) {
        if (audioPlayerState.id == imMessage.id) {
            waveValue = audioPlayerState.progress
            isPlaying =
                mediaRemoteExoUseCase.isPlaying
        }
    }
    Row(
        modifier = Modifier
            .imMessageBackgroundVoiceModifier(
                imMessage = imMessage
            )
            .width(200.dp)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            painterResource(xcj.app.compose_share.R.drawable.ic_graphic_eq_24),
            contentDescription = null
        )
        WaveSlider(
            value = waveValue,
            onValueChange = {
                waveValue = it
            },
            modifier = Modifier.weight(1f),
            animationOptions = WaveSliderDefaults.animationOptions(animateWave = isPlaying),
            waveOptions = WaveSliderDefaults.waveOptions(
                amplitude = if (isPlaying) {
                    15f
                } else {
                    0f
                }
            ),
            steps = 0
        )
        IconButton(onClick = {
            onImMessageContentClick(imMessage)
        }) {
            val playButtonRes = if (isPlaying) {
                xcj.app.compose_share.R.drawable.ic_round_pause_circle_filled_24
            } else {
                xcj.app.compose_share.R.drawable.ic_play_circle_filled_24
            }
            Icon(
                painter = painterResource(playButtonRes),
                contentDescription = null
            )
        }
    }
}

@Composable
private fun ImMessageItemHTMLComponent(
    imMessage: HTMLMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    Column(
        modifier = Modifier
            .imMessageBackgroundHTMLModifier(
                imMessage = imMessage
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable {
                onImMessageContentClick(imMessage)
            }
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painterResource(xcj.app.compose_share.R.drawable.ic_outline_language_24),
                contentDescription = null
            )
            Text(stringResource(xcj.app.appsets.R.string.show_web_content))
        }
    }
}

@Composable
private fun ImMessageItemImageComponent(
    imMessage: ImageMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    val localUri = (imMessage.metadata.localData as? UriProvider)?.provideUri()
    Box(
        modifier = Modifier
            .imMessageBackgroundImageModifier(
                imMessage = imMessage
            )
            .clip(MaterialTheme.shapes.extraLarge)
            .clickable {
                onImMessageContentClick(imMessage)
            }
    ) {
        AnyImage(
            modifier = Modifier
                .height(355.dp)
                .width(200.dp)
                .clip(MaterialTheme.shapes.extraLarge),
            model = imMessage.metadata.url,
            placeHolder = rememberAsyncImagePainter(localUri),
            error = rememberAsyncImagePainter(localUri)
        )
    }
}

@Composable
private fun ImMessageItemTextComponent(
    appSetsModuleSettings: AppSetsModuleSettings,
    imMessage: TextMessage,
    onImMessageContentClick: (IMMessage<*>) -> Unit,
) {
    Text(
        modifier = Modifier
            .imMessageBackgroundTextModifier(
                appSetsModuleSettings = appSetsModuleSettings,
                imMessage = imMessage
            ),
        text = imMessage.metadata.data
    )
}

@Composable
private fun ImMessageItemStartComponent(
    appSetsModuleSettings: AppSetsModuleSettings,
    session: Session,
    imMessage: IMMessage<*>,
    onBioClick: (Bio) -> Unit,
) {
    Row(modifier = Modifier.padding(12.dp)) {
        when (appSetsModuleSettings.imBubbleAlignment) {
            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_START -> {
                UserAvatarComponent(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(MaterialTheme.shapes.large)
                        .clickable {
                            onBioClick(imMessage.fromInfo)
                        },
                    imMessage = imMessage
                )
            }

            AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_START_END -> {
                if (
                    !session.isO2O && imMessage.fromInfo.uid != LocalAccountManager.userInfo.uid
                ) {
                    UserAvatarComponent(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(MaterialTheme.shapes.large)
                            .clickable {
                                onBioClick(imMessage.fromInfo)
                            },
                        imMessage = imMessage
                    )
                }

            }
        }
    }
}

@Composable
private fun UserInputComponent(
    modifier: Modifier = Modifier,
    hazeState: HazeState,
    inputTextState: TextFieldValue,
    textFieldAdviser: TextFieldAdviser,
    receiveContentListener: ReceiveContentListener,
    recorderState: DesignRecorder.AudioRecorderState,
    onTextChanged: (TextFieldValue) -> Unit,
    onSendClick: (Int) -> Unit,
    onSearchIconClick: (String) -> Unit,
    resetScroll: () -> Unit,
    onInputMoreAction: (String) -> Unit,
    onVoiceAction: () -> Unit,
    onVoiceStopClick: (Boolean) -> Unit,
    onVoicePauseClick: () -> Unit,
    onVoiceResumeClick: () -> Unit,
    onRemoveAdviseClick: (TextFieldAdviser.Advise) -> Unit,
) {

    val hapticFeedback = LocalHapticFeedback.current

    var currentInputSelector by rememberSaveable { mutableIntStateOf(InputSelector.NONE) }

    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        val dismissKeyboard: (OnBackPressedCallback?) -> Unit = { callback ->
            currentInputSelector = InputSelector.NONE
            textFieldFocusState = false
        }
        BackPressHandler(onBackPressed = dismissKeyboard)
    }
    var expandUserInput by remember { mutableStateOf(false) }


    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        LazyRow(
            reverseLayout = true,
            contentPadding = PaddingValues(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(textFieldAdviser.advises) { advise ->
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                        .hazeEffect(
                            hazeState,
                            HazeMaterials.thin()
                        )
                        .clickable {
                            hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            val overrideInputSelect = advise.inputSelector
                            onSendClick(overrideInputSelect)
                            resetScroll()
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .animateItem(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    when (advise) {
                        is TextFieldAdviser.WebContent -> {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_outline_language_24),
                                contentDescription = null
                            )
                            Text(
                                text = stringResource(xcj.app.appsets.R.string.send_as_web_content),
                                fontSize = 12.sp
                            )
                        }

                        is TextFieldAdviser.BaseUriContent -> {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_insert_drive_file_24),
                                contentDescription = null
                            )
                            Text(
                                text = stringResource(xcj.app.appsets.R.string.send_as_file),
                                fontSize = 12.sp
                            )
                        }

                        is TextFieldAdviser.ImageUriContent -> {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_photo_24),
                                contentDescription = null
                            )
                            Text(
                                text = stringResource(xcj.app.appsets.R.string.send_as_image),
                                fontSize = 12.sp
                            )

                            Icon(
                                modifier = Modifier.clickable {
                                    onRemoveAdviseClick(advise)
                                },
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_close_24),
                                contentDescription = null
                            )

                        }

                        is TextFieldAdviser.VideoUriContent -> {
                            Icon(
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_slow_motion_video_24),
                                contentDescription = null
                            )
                            Text(
                                text = stringResource(xcj.app.appsets.R.string.send_as_video),
                                fontSize = 12.sp
                            )
                            Icon(
                                modifier = Modifier.clickable {
                                    onRemoveAdviseClick(advise)
                                },
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_close_24),
                                contentDescription = null
                            )
                        }
                    }

                }
            }
        }
        Column(
            modifier = Modifier
                .hazeEffect(
                    hazeState,
                    HazeMaterials.thin()
                )
                .navigationBarsPadding()
                .animateContentSize(alignment = Alignment.BottomCenter)
        ) {
            UserInputText(
                textFieldValue = inputTextState,
                onTextChanged = onTextChanged,
                textFieldAdviser = textFieldAdviser,
                receiveContentListener = receiveContentListener,
                expandUserInput = expandUserInput,
                // Only show the keyboard if there's no input selector and text field has focus
                keyboardShown = currentInputSelector == InputSelector.NONE && textFieldFocusState,
                focusState = textFieldFocusState,
                onExpandUserInputClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    expandUserInput = !expandUserInput
                },
                // Close extended selector if text field receives focus
                onTextFieldFocused = { focused ->
                    if (focused) {
                        currentInputSelector = InputSelector.NONE
                        resetScroll()
                    }
                    textFieldFocusState = focused
                },
                onInputMoreAction = { action ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onInputMoreAction(action)
                },
                onSendClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    expandUserInput = false
                    onSendClick(currentInputSelector)
                    resetScroll()
                },
                onSearchIconClick = { keywords ->
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSearchIconClick(keywords)
                },
                onVoiceAction = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onVoiceAction()
                }
            )

            AudioRecordSpace(
                recorderState = recorderState,
                onStopClick = onVoiceStopClick,
                onPauseClick = onVoicePauseClick,
                onResumeClick = onVoiceResumeClick
            )
        }
    }
}

@Composable
fun AudioRecordSpace(
    recorderState: DesignRecorder.AudioRecorderState,
    onStopClick: (Boolean) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            LaunchedEffect(recorderState.seconds) {
                if (recorderState.seconds == recorderState.maxRecordSeconds) {
                    PurpleLogger.current.d(TAG, "auto send record audio by reached max seconds!")
                    onStopClick(true)
                }
            }
            if (recorderState.isStarted) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = String.format(
                            stringResource(xcj.app.appsets.R.string.auto_send_if_x_seconds),
                            recorderState.maxRecordSeconds
                        ),
                        fontSize = 8.sp
                    )
                    Text(
                        modifier = Modifier.combinedClickableSingle(
                            onClick = {
                                onStopClick(false)
                            }
                        ),
                        text = stringResource(xcj.app.appsets.R.string.click_to_stop),
                        fontSize = 12.sp
                    )
                }
                Row(
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    val seconds = "${recorderState.seconds}"
                    AnimatedContent(
                        targetState = seconds,
                        transitionSpec = {
                            fadeIn(tween()) + slideInVertically(
                                tween(),
                                initialOffsetY = { it }) togetherWith fadeOut(
                                tween()
                            ) + slideOutVertically(tween(), targetOffsetY = { -it })
                        }
                    ) { targetSeconds ->
                        Text(
                            text = targetSeconds,
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "s",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    modifier = Modifier.animateContentSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(onClick = {
                        if (!recorderState.isStarted) {
                            return@TextButton
                        }
                        if (recorderState.isPaused) {
                            onResumeClick()
                        } else {
                            onPauseClick()
                        }
                    }) {
                        val textRes = if (recorderState.isPaused) {
                            xcj.app.appsets.R.string.resume
                        } else {
                            xcj.app.appsets.R.string.pause
                        }
                        Text(text = stringResource(textRes))
                    }
                    if (recorderState.isPaused) {
                        TextButton(onClick = {
                            onStopClick(true)
                        }) {
                            Text(text = stringResource(xcj.app.appsets.R.string.send))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserInputText(
    keyboardType: KeyboardType = KeyboardType.Text,
    textFieldValue: TextFieldValue,
    textFieldAdviser: TextFieldAdviser,
    receiveContentListener: ReceiveContentListener,
    expandUserInput: Boolean,
    keyboardShown: Boolean,
    focusState: Boolean,
    onExpandUserInputClick: () -> Unit,
    onTextChanged: (TextFieldValue) -> Unit,
    onTextFieldFocused: (Boolean) -> Unit,
    onInputMoreAction: (String) -> Unit,
    onSendClick: () -> Unit,
    onSearchIconClick: (String) -> Unit,
    onVoiceAction: () -> Unit,
) {

    val context = LocalContext.current

    val activity = context.asComponentActivityOrNull()


    val userInputTargetHeight = getUserInputHeight(activity, expandUserInput)

    val userInputHeightState by animateDpAsState(
        targetValue = userInputTargetHeight,
        animationSpec = tween()
    )

    var lastFocusState by remember { mutableStateOf(false) }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription =
                    ContextCompat.getString(context, xcj.app.appsets.R.string.input_something)
                keyboardShownProperty = keyboardShown
                //spk.setValue(keyboardShown)
            }
    ) {
        Box(Modifier.padding(horizontal = 12.dp)) {
            val boxModifier = if (expandUserInput) {
                Modifier
                    .height(userInputHeightState)
                    .border(
                        2.dp, MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
            } else {
                Modifier
                    .height(userInputHeightState)
            }

            Box(modifier = boxModifier) {
                BasicTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                        .align(Alignment.CenterStart)
                        .onFocusChanged { state ->
                            if (lastFocusState != state.isFocused) {
                                onTextFieldFocused(state.isFocused)
                            }
                            lastFocusState = state.isFocused
                        }
                        .contentReceiver(receiveContentListener),
                    value = textFieldValue,
                    onValueChange = {
                        onTextChanged(it)
                    },
                    keyboardActions = KeyboardActions(
                        onSend = {
                            onSendClick()
                        }
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Send
                    ),
                    maxLines = if (expandUserInput) {
                        Int.MAX_VALUE
                    } else {
                        5
                    },
                    cursorBrush = SolidColor(LocalContentColor.current),
                    textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
                )

                val disableContentColor =
                    MaterialTheme.colorScheme.onSurfaceVariant
                if (textFieldValue.text.isEmpty() || !focusState) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp),
                        text = stringResource(xcj.app.appsets.R.string.text_something),
                        style = MaterialTheme.typography.bodyLarge.copy(color = disableContentColor)
                    )
                }
            }
        }

        if (!expandUserInput) {
            DesignHDivider(modifier = Modifier)
        }
        Spacer(Modifier.heightIn(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    tint = MaterialTheme.colorScheme.primary,
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_add_circle_outline_24),
                    contentDescription = stringResource(xcj.app.appsets.R.string.more_action),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .clickable(
                            onClick = {
                                onInputMoreAction("IM_CONTENT_SELECT_REQUEST")
                            }
                        )
                )
                Icon(
                    tint = MaterialTheme.colorScheme.primary,
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_outline_keyboard_voice_24),
                    contentDescription = stringResource(xcj.app.appsets.R.string.voice_action),
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .combinedClickableSingle(
                            onClick = {
                                onVoiceAction()
                            }
                        )
                )
            }

            androidx.compose.animation.AnimatedVisibility(
                modifier = Modifier
                    .align(Alignment.CenterEnd),
                visible = textFieldValue.text.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .animateContentSize(
                            animationSpec = tween()
                        ),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            onExpandUserInputClick()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {

                        AnimatedContent(
                            targetState = expandUserInput,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            contentAlignment = Alignment.Center
                        ) { targetExpandUserInput ->
                            val expandIconRes = if (targetExpandUserInput) {
                                xcj.app.compose_share.R.drawable.ic_round_close_fullscreen_24
                            } else {
                                xcj.app.compose_share.R.drawable.ic_open_in_full_24px
                            }
                            Icon(
                                painter = painterResource(id = expandIconRes),
                                contentDescription = stringResource(id = xcj.app.appsets.R.string.send)
                            )
                        }
                    }
                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            onSearchIconClick(textFieldValue.text)
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_search_24),
                            contentDescription = stringResource(id = xcj.app.appsets.R.string.search)
                        )
                    }

                    IconButton(
                        modifier = Modifier,
                        onClick = {
                            onSendClick()
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_send_24),
                            contentDescription = stringResource(id = xcj.app.appsets.R.string.send)
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun getUserInputHeight(activity: ComponentActivity?, expand: Boolean): Dp {
    if (!expand) {
        return 64.dp
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
        return 640.dp
    }
    if (activity?.isLaunchedFromBubble == true) {
        return 320.dp
    } else {
        return 640.dp
    }
}

@Composable
private fun Modifier.imMessageBackgroundTextModifier(
    appSetsModuleSettings: AppSetsModuleSettings,
    imMessage: IMMessage<*>
): Modifier {
    return when (appSetsModuleSettings.imBubbleAlignment) {
        AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_START -> {
            this
                .background(
                    MaterialTheme.colorScheme.secondaryContainer,
                    RoundedCornerShape(
                        topStart = 2.dp,
                        topEnd = 20.dp,
                        bottomEnd = 20.dp,
                        bottomStart = 20.dp
                    )
                )
                .widthIn(100.dp, max = TextFieldDefaults.MinWidth)
                .padding(12.dp)
        }

        AppSetsModuleSettings.IM_BUBBLE_ALIGNMENT_ALL_END -> {
            this
                .background(
                    MaterialTheme.colorScheme.tertiaryContainer,
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 2.dp,
                        bottomEnd = 20.dp,
                        bottomStart = 20.dp
                    )
                )
                .widthIn(100.dp, max = TextFieldDefaults.MinWidth)
                .padding(12.dp)
        }

        else -> {
            if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
                this
                    .background(
                        MaterialTheme.colorScheme.tertiaryContainer,
                        RoundedCornerShape(
                            topStart = 20.dp,
                            topEnd = 2.dp,
                            bottomEnd = 20.dp,
                            bottomStart = 20.dp
                        )
                    )
                    .widthIn(100.dp, max = TextFieldDefaults.MinWidth)
                    .padding(12.dp)
            } else {
                this
                    .background(
                        MaterialTheme.colorScheme.secondaryContainer,
                        RoundedCornerShape(
                            topStart = 2.dp,
                            topEnd = 20.dp,
                            bottomEnd = 20.dp,
                            bottomStart = 20.dp
                        )
                    )
                    .widthIn(100.dp, max = TextFieldDefaults.MinWidth)
                    .padding(12.dp)
            }
        }
    }
}

@Composable
private fun Modifier.imMessageBackgroundADModifier(imMessage: IMMessage<*>): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
        background(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    } else {
        background(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    }
}

@Composable
private fun Modifier.imMessageBackgroundLocationModifier(imMessage: IMMessage<*>): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
        background(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp, max = TextFieldDefaults.MinWidth)
            .padding(2.dp)
    } else {
        background(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp, max = TextFieldDefaults.MinWidth)
            .padding(2.dp)
    }
}

@Composable
private fun Modifier.imMessageBackgroundFileModifier(imMessage: IMMessage<*>): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
        background(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    } else {
        background(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    }
}

@Composable
private fun Modifier.imMessageBackgroundHTMLModifier(imMessage: IMMessage<*>): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
        background(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    } else {
        background(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    }
}

@Composable
private fun Modifier.imMessageBackgroundImageModifier(imMessage: IMMessage<*>): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
        background(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    } else {
        background(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    }
}

@Composable
private fun Modifier.imMessageBackgroundVideoModifier(imMessage: IMMessage<*>): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
        background(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    } else {
        background(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    }
}

@Composable
private fun Modifier.imMessageBackgroundVoiceModifier(imMessage: IMMessage<*>): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
        background(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    } else {
        background(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    }
}

@Composable
private fun Modifier.imMessageBackgroundMusicModifier(imMessage: IMMessage<*>): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.fromInfo.uid)) {
        background(
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    } else {
        background(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.shapes.extraLarge
        )
            .widthIn(50.dp)
            .padding(2.dp)
    }
}

class TextFieldAdviser {

    abstract class Advise {
        var adopted: Boolean = false
        abstract var inputSelector: Int
    }

    data class WebContent(val uri: String, val tips: String? = null) : Advise() {
        override var inputSelector: Int = InputSelector.HTML
    }

    data class BaseUriContent(val uri: String, val tips: String? = null) : Advise() {
        override var inputSelector: Int = InputSelector.FILE
    }

    data class ImageUriContent(val uri: String, val tips: String? = null) : Advise() {
        override var inputSelector: Int = InputSelector.IMAGE
    }

    data class VideoUriContent(val uri: String, val tips: String? = null) : Advise() {
        override var inputSelector: Int = InputSelector.VIDEO
    }

    data class FileUriContent(val uri: String, val tips: String? = null) : Advise() {
        override var inputSelector: Int = InputSelector.FILE
    }

    data class MusicUriContent(val uri: String, val tips: String? = null) : Advise() {
        override var inputSelector: Int = InputSelector.MUSIC
    }

    private val _advise: MutableList<Advise> = mutableStateListOf()
    val advises: List<Advise> = _advise

    suspend fun makeAdviser(
        textFieldValue: TextFieldValue,
        receivedContents: SnapshotStateList<Uri>,
    ) {
        val advises = analysis(textFieldValue, receivedContents)
        _advise.clear()
        _advise.addAll(advises)
    }

    suspend fun analysis(
        textFieldValue: TextFieldValue,
        receivedContents: SnapshotStateList<Uri>,
    ): List<Advise> =
        withContext(Dispatchers.IO) {
            val results = mutableListOf<Advise>()
            if (textFieldValue.text.startWithHttpSchema()) {
                results.add(WebContent(textFieldValue.text))
            }
            if (textFieldValue.text.startsWith("content://") || textFieldValue.text.startsWith("file://")) {
                results.add(BaseUriContent(textFieldValue.text))
            }
            if (receivedContents.isNotEmpty()) {
                receivedContents.forEach { uri ->
                    results.add(ImageUriContent(uri.toString()))
                }
            }
            return@withContext results
        }

    fun removeAdvise(advise: Advise) {
        _advise.remove(advise)
    }
}