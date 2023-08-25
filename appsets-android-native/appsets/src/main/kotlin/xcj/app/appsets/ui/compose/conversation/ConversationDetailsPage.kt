package xcj.app.appsets.ui.compose.conversation

import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.SemanticsPropertyReceiver
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.launch
import xcj.app.appsets.R
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.ui.compose.LocalBackPressedDispatcher
import xcj.app.appsets.ui.compose.LocalOrRemoteImage
import xcj.app.appsets.ui.compose.MainViewModel
import xcj.app.appsets.ui.compose.PageRouteNameProvider

@UnstableApi
@Composable
fun ConversationDetailsPage(
    tabVisibilityState: MutableState<Boolean>,
    onBackClick: () -> Unit,
    onUserAvatarClick: (ImMessage) -> Unit,
    onMoreClick: ((ImObj?) -> Unit)? = null
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
        Modifier
            .fillMaxSize()
        //.nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {
        // tonalElevation = 0.dp,
        Surface(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.Companion.systemBars))
                Box {
                    Row {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_round_arrow_24),
                            contentDescription = "back",
                            modifier = Modifier
                                .clip(CircleShape)
                                .clickable(onClick = onBackClick)
                                .padding(12.dp)
                        )
                    }
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .align(Alignment.Center), contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = mainViewModel.conversationUseCase?.currentSession?.imObj?.name
                                ?: "未命名",
                            modifier = Modifier,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                        ) {

                            Icon(
                                painterResource(id = R.drawable.outline_more_vert_24),
                                "more",
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .clickable {
                                        onMoreClick?.invoke(mainViewModel.conversationUseCase?.currentSession?.imObj)
                                    }
                                    .padding(12.dp)
                            )
                        }

                    }
                }
            }
        }
        val scrollState = rememberLazyListState()
        Messages(
            modifier = Modifier.weight(1f),
            messages = mainViewModel.conversationUseCase?.currentSession?.conversionState?.messages,
            navigateToProfile = {},
            scrollState = scrollState,
            onUserAvatarClick = onUserAvatarClick
        )
        val scope = rememberCoroutineScope()
        UserInput(
            onMessageSent = {// imMessage ->
                //vm.addMessage(imMessage, true)
            },
            resetScroll = {
                scope.launch {
                    scrollState.scrollToItem(0)
                }
            },
            // Use navigationBarsPadding() imePadding() and , to move the input panel above both the
            // navigation bar, and on-screen keyboard (IME)
            modifier = Modifier
                .navigationBarsPadding()
                .imePadding()
        )
    }
}
@Composable
fun AnotherNewestImMessageBubble(hasNewestImMessages:Boolean){
    if(hasNewestImMessages){
        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
        ) {
            Text(
                text = "有新的其他对话消息，点击查看", modifier = Modifier
                    .background(MaterialTheme.colorScheme.primary)
                    .align(Alignment.Center)
            )
        }
    }
}


@Composable
fun UserAvatarCompose(modifier: Modifier, imMessage: ImMessage) {
    val avatarContainerModifier = Modifier
    Row(modifier = avatarContainerModifier) {
        LocalOrRemoteImage(
            modifier = modifier
                .size(42.dp)
                .clip(RoundedCornerShape(8.dp)),
            any = imMessage.msgFromInfo.avatarUrl,
            defaultColor = MaterialTheme.colorScheme.secondaryContainer
        )
    }
}


@UnstableApi
@Composable
fun Messages(
    modifier: Modifier = Modifier,
    messages: List<ImMessage>?,
    navigateToProfile: (String) -> Unit,
    scrollState: LazyListState,
    onUserAvatarClick: (ImMessage) -> Unit,
) {
    Box(modifier = modifier) {
        val viewModel = viewModel<MainViewModel>(LocalContext.current as AppCompatActivity)
        LazyColumn(
            reverseLayout = true,
            state = scrollState,
            // Add content padding so that the content can be scrolled (y-axis)
            // below the status bar + app bar
            // TODO: Get height from somewhere
            contentPadding =
            WindowInsets.statusBars.add(WindowInsets(top = 32.dp)).asPaddingValues(),
            modifier = Modifier
                .testTag("ConversationTestTag")
                .fillMaxSize()
        ) {
            if (!messages.isNullOrEmpty()) {
                for (index in messages.indices) {
                    val imMessage = messages[index]
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            val boxAlignmentModifier =
                                if (LocalAccountManager.isLoggedUser(imMessage.msgFromInfo.uid)) {
                                    Modifier.align(Alignment.CenterEnd)
                                } else {
                                    Modifier.align(Alignment.CenterStart)
                                }
                            Row(modifier = boxAlignmentModifier) {
                                if (imMessage.msgFromInfo.uid != LocalAccountManager._userInfo.value.uid) {
                                    UserAvatarCompose(
                                        modifier =
                                        Modifier.clickable {
                                            onUserAvatarClick(imMessage)
                                        }, imMessage = imMessage
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                }
                                when (imMessage) {
                                    is ImMessage.Text -> {
                                        Text(
                                            imMessage.text,
                                            modifier = Modifier.imMessageBackgroundCommonModifier(
                                                imMessage = imMessage
                                            )
                                        )
                                    }

                                    is ImMessage.Image -> {
                                        Box(
                                            modifier = Modifier.imMessageBackgroundImageModifier(
                                                imMessage = imMessage
                                            )
                                        ) {
                                            LocalOrRemoteImage(
                                                modifier = Modifier
                                                    .height(180.dp)
                                                    .width(100.dp)
                                                    .clip(RoundedCornerShape(2.dp)),
                                                any = imMessage.imageJson
                                            )
                                        }
                                    }

                                    is ImMessage.HTML -> {
                                        Column(
                                            modifier = Modifier.imMessageBackgroundCommonModifier(
                                                imMessage = imMessage
                                            )
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(150.dp)
                                                    .background(Color.Red)
                                                    .clickable {
                                                        Log.e(
                                                            "ImMessage.HTML",
                                                            "to web browser, url${imMessage.url}"
                                                        )
                                                    }) {
                                                Text(
                                                    "网页浏览器:${imMessage.content}",
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }

                                        }
                                    }

                                    is ImMessage.Voice -> {
                                        Column(
                                            modifier = Modifier.imMessageBackgroundCommonModifier(
                                                imMessage = imMessage
                                            )
                                        ) {
                                            Text(imMessage.voiceJson.name)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(onClick = {
                                                Log.e(
                                                    "ImMessage.Voice",
                                                    "download file to local fs, url:[${imMessage.voiceJson.url}]"
                                                )
                                            }) {
                                                Text("下载文件")
                                            }
                                            Button(onClick = {
                                                Log.e(
                                                    "ImMessage.Voice",
                                                    "playback audio from local fs, file content:[${imMessage.content}]"
                                                )
                                            }) {
                                                Text("播放")
                                            }
                                        }
                                    }

                                    is ImMessage.Video -> {
                                        Column(
                                            modifier = Modifier.imMessageBackgroundCommonModifier(
                                                imMessage = imMessage
                                            )
                                        ) {
                                            Text(imMessage.videoJson.name)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                Modifier
                                                    .size(100.dp)
                                                    .background(
                                                        Color.White,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                            ) {
                                                Text(
                                                    text = "封面",
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(onClick = {
                                                Log.e(
                                                    "ImMessage.Video",
                                                    "download file to local fs, url:[${imMessage.videoJson.url}"
                                                )
                                            }) {
                                                Text("下载文件")
                                            }
                                            Button(onClick = {
                                                Log.e(
                                                    "ImMessage.Video",
                                                    "playback video from local fs, file content:[${imMessage.content}]"
                                                )
                                            }) {
                                                Text("播放")
                                            }
                                        }
                                    }

                                    is ImMessage.Music -> {
                                        Column(
                                            modifier = Modifier.imMessageBackgroundCommonModifier(
                                                imMessage = imMessage
                                            )
                                        ) {
                                            Text(imMessage.musicJson.name)
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Box(
                                                Modifier
                                                    .size(100.dp)
                                                    .background(
                                                        Color.White,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                            ) {
                                                Text(
                                                    text = "封面",
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(onClick = {
                                                Log.e(
                                                    "ImMessage.Music",
                                                    "download file to local fs, url:[${imMessage.musicJson.url}]"
                                                )
                                            }) {
                                                Text("下载文件")
                                            }
                                            if (viewModel.mediaUseCase.audioPlayerState.value?.playbackStateCompat?.state == PlaybackStateCompat.STATE_PLAYING &&
                                                viewModel.mediaUseCase.audioPlayerState.value?.id == imMessage.id
                                            ) {
                                                Button(onClick = {
                                                    viewModel.mediaUseCase.pauseAudio()
                                                    Log.e(
                                                        "ImMessage.Music",
                                                        "playback audio from local fs, file content:[${imMessage.content}]"
                                                    )
                                                }) {
                                                    Text("暂停")
                                                }
                                            } else {
                                                Button(onClick = {
                                                    viewModel.mediaUseCase.playAudio(
                                                        imMessage.id,
                                                        imMessage.musicJson
                                                    )
                                                    Log.e(
                                                        "ImMessage.Music",
                                                        "playback audio from local fs, file content:[${imMessage.content}]"
                                                    )
                                                }) {
                                                    Text("播放")
                                                }
                                            }
                                        }

                                    }

                                    is ImMessage.Ad -> {
                                        Column(
                                            modifier = Modifier.imMessageBackgroundCommonModifier(
                                                imMessage = imMessage
                                            )
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(150.dp)
                                                    .background(
                                                        Color.White,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                                    .clickable {
                                                        Log.e(
                                                            "ImMessage.Ad",
                                                            "显示广告 url:[${imMessage.content}]"
                                                        )
                                                    }
                                            ) {
                                                Text(
                                                    "广告",
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }

                                        }
                                    }

                                    is ImMessage.File -> {
                                        Column(
                                            modifier = Modifier.imMessageBackgroundCommonModifier(
                                                imMessage = imMessage
                                            )
                                        ) {
                                            Text(imMessage.fileJson.name)
                                            Button(onClick = {
                                                Log.e(
                                                    "ImMessage.File",
                                                    "download file to local fs, url:[${imMessage.fileJson.url}]"
                                                )
                                            }) {
                                                Text("下载文件")
                                            }
                                            Button(onClick = {
                                                Log.e(
                                                    "ImMessage.File",
                                                    "show file details, file content:[${imMessage.content}]"
                                                )
                                            }) {
                                                Text("查看")
                                            }

                                        }
                                    }

                                    is ImMessage.Location -> {
                                        Column(
                                            modifier = Modifier.imMessageBackgroundCommonModifier(
                                                imMessage = imMessage
                                            )
                                        ) {
                                            Box(
                                                Modifier
                                                    .size(150.dp)
                                                    .background(
                                                        Color.White,
                                                        RoundedCornerShape(10.dp)
                                                    )
                                            ) {
                                                Text(
                                                    "地理信息",
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                            }
                                        }
                                    }

                                    is ImMessage.System -> {}
                                }
                                if (imMessage.msgFromInfo.uid == LocalAccountManager._userInfo.value.uid) {
                                    Spacer(modifier = Modifier.width(10.dp))
                                    UserAvatarCompose(Modifier, imMessage)
                                }
                            }
                        }
                    }
                }
            }

        }
        /*// Jump to bottom button shows up when user scrolls past a threshold.
        // Convert to pixels:
        val jumpThreshold = with(LocalDensity.current) {
            JumpToBottomThreshold.toPx()
        }

        // Show the button if the first visible item is not the first one or if the offset is
        // greater than the threshold.
        val jumpToBottomButtonEnabled by remember {
            derivedStateOf {
                scrollState.firstVisibleItemIndex != 0 ||
                        scrollState.firstVisibleItemScrollOffset > jumpThreshold
            }
        }

        JumpToBottom(
            // Only show if the scroller is not at the bottom
            enabled = jumpToBottomButtonEnabled,
            onClicked = {
                scope.launch {
                    scrollState.animateScrollToItem(0)
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter)
        )*/
    }
}

@Composable
fun Modifier.imMessageBackgroundCommonModifier(imMessage: ImMessage): Modifier {
    if (LocalAccountManager.isLoggedUser(imMessage.msgFromInfo.uid)) {
        return this
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(
                    topStart = 28.dp,
                    topEnd = 0.dp,
                    bottomEnd = 28.dp,
                    bottomStart = 28.dp
                )
            )
            .widthIn(100.dp)
            .padding(horizontal = 14.dp, vertical = 20.dp)
    } else {
        return this
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(
                    topStart = 0.dp,
                    topEnd = 28.dp,
                    bottomEnd = 28.dp,
                    bottomStart = 28.dp
                )
            )
            .widthIn(100.dp)
            .padding(horizontal = 14.dp, vertical = 20.dp)
    }
}

@Composable
fun Modifier.imMessageBackgroundImageModifier(imMessage: ImMessage): Modifier {
    return if (LocalAccountManager.isLoggedUser(imMessage.msgFromInfo.uid)) {
        this
            .background(
                MaterialTheme.colorScheme.secondaryContainer,
                RoundedCornerShape(2.dp)
            )
            .widthIn(100.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    } else {
        this
            .background(
                MaterialTheme.colorScheme.primaryContainer,
                RoundedCornerShape(2.dp)
            )
            .widthIn(100.dp)
            .padding(horizontal = 2.dp, vertical = 2.dp)
    }
}


@UnstableApi
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UserInput(
    onMessageSent: () -> Unit,
    modifier: Modifier = Modifier,
    resetScroll: () -> Unit = {},
) {
    var currentInputSelector by rememberSaveable { mutableStateOf(InputSelector.NONE) }


    var textState by remember { mutableStateOf(TextFieldValue()) }

    // Used to decide if the keyboard should be shown
    var textFieldFocusState by remember { mutableStateOf(false) }
    val dismissKeyboard: (OnBackPressedCallback?) -> Unit = { callback ->
        currentInputSelector = InputSelector.NONE
        textFieldFocusState = false
    }

    // Intercept back navigation if there's a InputSelector visible
    if (currentInputSelector != InputSelector.NONE) {
        BackPressHandler(onBackPressed = dismissKeyboard)
    }
    //tonalElevation = 0.dp
    Surface() {
        Column(modifier = modifier) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            UserInputText(
                textFieldValue = textState,
                onTextChanged = { textState = it },
                // Only show the keyboard if there's no input selector and text field has focus
                keyboardShown = currentInputSelector == InputSelector.NONE && textFieldFocusState,
                // Close extended selector if text field receives focus
                onTextFieldFocused = { focused ->
                    if (focused) {
                        currentInputSelector = InputSelector.NONE
                        resetScroll()
                    }
                    textFieldFocusState = focused
                },
                focusState = textFieldFocusState,
                onInputMoreAction = {
                    viewModel.showSelectContentDialog(
                        context,
                        PageRouteNameProvider.ConversationDetailsPage
                    )
                },
                onMessageSent = {
                    viewModel.onSendMessage(context, currentInputSelector, textState.text)

                    onMessageSent()
                    // Reset text field and close keyboard
                    textState = TextFieldValue()
                    // Move scroll to bottom
                    resetScroll()
                    //dismissKeyboard(null)
                },
                onVoiceAction = {
                    viewModel.mediaUseCase.startRecord(context)
                }
            )
            AnimatedVisibility(visible = viewModel.mediaUseCase.recorderState.value?.first == true) {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp)
                        .clickable {
                            viewModel.mediaUseCase.stopRecord(context, "ui click")
                        },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (viewModel.mediaUseCase.recorderState.value != null) {
                        Text(
                            text = "${viewModel.mediaUseCase.recorderState.value?.second ?: 0} s",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(text = "点击停止")
                }
            }
            /*     UserInputSelector(
                     onSelectorChange = { currentInputSelector = it },
                     sendMessageEnabled = textState.text.isNotBlank(),
                     onMessageSent = {

                     },
                     currentInputSelector = currentInputSelector
                 )*/
            /*SelectorExpanded(
                onCloseRequested = dismissKeyboard,
                onTextAdded = { textState = textState.addText(it) },
                currentSelector = currentInputSelector
            )*/
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInputSelector(
    onSelectorChange:(InputSelector)->Unit,
    sendMessageEnabled:Boolean,
    onMessageSent:()->Unit,
    currentInputSelector: InputSelector
){
    val scrollableState = ScrollableState { 0f }
    Row(modifier = Modifier.scrollable(scrollableState, Orientation.Horizontal)) {
        AssistChip(onClick = {
           onSelectorChange(InputSelector.IMAGE)
        }, label = {
            Text(text = "图片")
        })
        AssistChip(onClick = {
            onSelectorChange(InputSelector.VIDEO)
        }, label = {
            Text(text = "视频")
        })
        AssistChip(onClick = {
            onSelectorChange(InputSelector.MUSIC)
        }, label = {
            Text(text = "音乐")
        })
        AssistChip(onClick = {
            onSelectorChange(InputSelector.VOICE)
        }, label = {
            Text(text = "语音")
        })
        AssistChip(onClick = {
            onSelectorChange(InputSelector.LOCATION)
        }, label = {
            Text(text = "位置")
        })
        AssistChip(onClick = {
            onSelectorChange(InputSelector.HTML)
        }, label = {
            Text(text = "网页")
        })
        AssistChip(onClick = {
            onSelectorChange(InputSelector.AD)
        }, label = {
            Text(text = "广告")
        })
        AssistChip(onClick = {
            onSelectorChange(InputSelector.FILE)
        }, label = {
            Text(text = "文件")
        })
    }
}


@Composable
fun BackPressHandler(onBackPressed: (OnBackPressedCallback?) -> Unit) {
    // Safely update the current `onBack` lambda when a new one is provided
    val currentOnBackPressed by rememberUpdatedState(onBackPressed)
    // Remember in Composition a back callback that calls the `onBackPressed` lambda
    val backCallback = remember {
        object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBackPressed(this)
            }
        }
    }

    val backDispatcher = LocalBackPressedDispatcher.current

    // Whenever there's a new dispatcher set up the callback
    DisposableEffect(backDispatcher) {
        backDispatcher.addCallback(backCallback)
        // When the effect leaves the Composition, or there's a new dispatcher, remove the callback
        onDispose {
            backCallback.remove()
        }
    }
}

val KeyboardShownKey = SemanticsPropertyKey<Boolean>("KeyboardShownKey")
var SemanticsPropertyReceiver.keyboardShownProperty by KeyboardShownKey

@UnstableApi
@ExperimentalFoundationApi
@Composable
private fun UserInputText(
    keyboardType: KeyboardType = KeyboardType.Text,
    onTextChanged: (TextFieldValue) -> Unit,
    textFieldValue: TextFieldValue,
    keyboardShown: Boolean,
    onTextFieldFocused: (Boolean) -> Unit,
    focusState: Boolean,
    onInputMoreAction: () -> Unit,
    onMessageSent: (String) -> Unit,
    onVoiceAction: () -> Unit,
) {
    Surface {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .semantics {
                    contentDescription = "Input something"
                    keyboardShownProperty = keyboardShown
                    //spk.setValue(keyboardShown)
                }
        ) {
            Box(modifier = Modifier.height(64.dp)) {
                var lastFocusState by remember { mutableStateOf(false) }
                BasicTextField(
                    value = textFieldValue,

                    onValueChange = { onTextChanged(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp)
                        .align(Alignment.CenterStart)
                        .onFocusChanged { state ->
                            if (lastFocusState != state.isFocused) {
                                onTextFieldFocused(state.isFocused)
                            }
                            lastFocusState = state.isFocused
                        },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = keyboardType,
                        imeAction = ImeAction.Send
                    ),
                    maxLines = 1,
                    cursorBrush = SolidColor(LocalContentColor.current),
                    textStyle = LocalTextStyle.current.copy(color = LocalContentColor.current)
                )

                val disableContentColor =
                    MaterialTheme.colorScheme.onSurfaceVariant
                if (textFieldValue.text.isEmpty() && !focusState) {
                    Text(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .padding(start = 12.dp),
                        text = "Text something",//stringResource(id = R.string.textfield_hint),
                        style = MaterialTheme.typography.bodyLarge.copy(color = disableContentColor)
                    )
                }
            }
            Divider(
                modifier = Modifier
                    .height(0.5.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp), color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)) {
                Row(
                    Modifier
                        .align(Alignment.CenterStart)
                ) {
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(id = R.drawable.round_add_circle_outline_24),
                        contentDescription = "more action",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onInputMoreAction)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(
                        tint = MaterialTheme.colorScheme.primary,
                        painter = painterResource(id = R.drawable.outline_keyboard_voice_24),
                        contentDescription = "voice action",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onVoiceAction)
                    )
                }
                androidx.compose.animation.AnimatedVisibility(
                    visible = textFieldValue.text.isNotEmpty(),
                    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                    exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 })
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                                .clickable {
                                    onMessageSent(textFieldValue.text)
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = MaterialTheme.colorScheme.primary
                            //colors = buttonColors,
                            //border = border,
                        ) {
                            Text(
                                text = "Send",//stringResource(id = R.string.send),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

}