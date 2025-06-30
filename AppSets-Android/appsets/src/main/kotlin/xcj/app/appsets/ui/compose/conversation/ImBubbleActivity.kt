package xcj.app.appsets.ui.compose.conversation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.withCreated
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.im.InputSelector
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.HTMLMessage
import xcj.app.appsets.im.message.ImageMessage
import xcj.app.appsets.im.message.MusicMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.message.VideoMessage
import xcj.app.appsets.im.message.VoiceMessage
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.ui.compose.LocalUseCaseOfActivityLifecycle
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfGroupInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaAudioRecorder
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfNowSpaceContent
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreen
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.LocalUseCaseOfUserInfo
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.apps.AppDetailsPage
import xcj.app.appsets.ui.compose.apps.DownloadBottomSheetComponent
import xcj.app.appsets.ui.compose.group.GroupInfoPage
import xcj.app.appsets.ui.compose.main.DesignNaviHost
import xcj.app.appsets.ui.compose.main.ImmerseContentContainer
import xcj.app.appsets.ui.compose.main.handleApplicationDownload
import xcj.app.appsets.ui.compose.main.navigateToCreateAppPage
import xcj.app.appsets.ui.compose.main.navigateToUserInfoPage
import xcj.app.appsets.ui.compose.main.navigateToVideoPlaybackActivity
import xcj.app.appsets.ui.compose.main.onBioClick
import xcj.app.appsets.ui.compose.main.showContentSelectionDialog
import xcj.app.appsets.ui.compose.main.showPictureViewDialog
import xcj.app.appsets.ui.compose.main.showWebBrowserDialog
import xcj.app.appsets.ui.compose.outside.RestrictedContentDialog
import xcj.app.appsets.ui.compose.outside.ScreenDetailsPage
import xcj.app.appsets.ui.compose.outside.ScreenEditPage
import xcj.app.appsets.ui.compose.privacy.PrivacyPage
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.compose.user.UserProfilePage
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.ui.viewmodel.ImBubbleViewModel
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.compose_share.components.BottomSheetContainer
import xcj.app.compose_share.components.LocalAnyStateProvider
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.usecase.PlatformUseCase
import xcj.app.starter.android.util.PurpleLogger

class ImBubbleActivity : DesignComponentActivity() {
    companion object {
        private const val TAG = "ImBubbleActivity"
    }

    private val viewModel by viewModels<ImBubbleViewModel>()

    override fun requireViewModel(): ImBubbleViewModel? {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                ImBubblePages()
            }
        }
        lifecycleScope.launch {
            lifecycle.withCreated {
                viewModel.onActivityCreated(this@ImBubbleActivity)
                viewModel.handleIntent(intent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        PurpleLogger.current.d(TAG, "onNewIntent")
        viewModel.handleIntent(intent)
    }
}

@Composable
fun ImBubblePages() {
    val viewModel = viewModel<ImBubbleViewModel>()
    CompositionLocalProvider(
        LocalUseCaseOfActivityLifecycle provides viewModel.activityLifecycleUseCase,
        LocalUseCaseOfSystem provides viewModel.systemUseCase,
        LocalUseCaseOfNavigation provides viewModel.navigationUseCase,
        LocalUseCaseOfGroupInfo provides viewModel.groupInfoUseCase,
        LocalUseCaseOfScreen provides viewModel.screensUseCase,
        LocalUseCaseOfMediaRemoteExo provides viewModel.mediaRemoteExoUseCase,
        LocalUseCaseOfMediaAudioRecorder provides viewModel.mediaAudioRecorderUseCase,
        LocalUseCaseOfConversation provides viewModel.conversationUseCase,
        LocalUseCaseOfUserInfo provides viewModel.userInfoUseCase,
        LocalUseCaseOfNowSpaceContent provides viewModel.nowSpaceContentUseCase,
        LocalAnyStateProvider provides viewModel,
    ) {

        val navController = rememberNavController()

        Box(modifier = Modifier.fillMaxSize()) {
            ImSessionBubbleScaffoldContainer(navController = navController)

            ImmerseContentContainer(navController = navController)

            BottomSheetContainer()

        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ImSessionBubbleScaffoldContainer(navController: NavHostController) {
    ImSessionBubbleNaviHostPages(navController)
}

@Composable
fun ImSessionBubbleNaviHostPages(navController: NavHostController) {
    var isShowRestrictedContentDialog by remember {
        mutableStateOf(false)
    }
    var restrictedContentConfirmCallback: (() -> Unit)? by remember {
        mutableStateOf(null)
    }
    Box {
        DesignNaviHost(
            navController = navController,
            startDestination = PageRouteNames.ConversationDetailsPage,
        ) {
            composable(PageRouteNames.ConversationDetailsPage) {
                val context = LocalContext.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val anyStateProvider = LocalAnyStateProvider.current
                val mediaAudioRecorderUseCase = LocalUseCaseOfMediaAudioRecorder.current
                val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
                ConversationDetailsPage(
                    sessionState = conversationUseCase.currentSessionState.value,
                    recorderState = mediaAudioRecorderUseCase.recorderState.value,
                    onBackClick = {
                        navController.navigate(PageRouteNames.ConversationOverviewPage) {
                            popUpTo(PageRouteNames.ConversationOverviewPage) {
                                inclusive = true
                            }
                        }
                    },
                    onBioClick = { bio ->
                        onBioClick(context, navController, bio)
                    },
                    onImMessageContentClick = { imMessage ->
                        when (imMessage) {
                            is MusicMessage, is VoiceMessage -> {
                                val commonURIJson = CommonURIJson(
                                    imMessage.id,
                                    imMessage.metadata.description,
                                    imMessage.metadata.url ?: ""
                                )
                                mediaRemoteExoUseCase.playOrPauseAudio(commonURIJson)
                            }

                            is VideoMessage -> {
                                navigateToVideoPlaybackActivity(context, imMessage)
                            }

                            is ImageMessage -> {
                                val imMessageOfImageUrls =
                                    conversationUseCase.findCurrentSessionAllImMessageOfImage()
                                        .map { it.metadata.url }
                                showPictureViewDialog(
                                    anyStateProvider,
                                    context,
                                    imMessage.metadata.url,
                                    imMessageOfImageUrls
                                )
                            }

                            is HTMLMessage -> {
                                showWebBrowserDialog(
                                    context,
                                    anyStateProvider,
                                    imMessage.metadata.data
                                )
                            }
                        }
                    },
                    onInputMoreAction = { requestKey ->
                        showContentSelectionDialog(
                            context,
                            anyStateProvider,
                            navController,
                            PageRouteNames.ConversationDetailsPage,
                            requestKey,
                        )
                    },
                    onVoiceAction = {
                        mediaAudioRecorderUseCase.startRecord(context, navController)
                    },
                    onVoiceStopClick = { showSend ->
                        mediaAudioRecorderUseCase.stopRecord("UI click")
                        if (!showSend) {
                            return@ConversationDetailsPage
                        }
                        val uriProvider = mediaAudioRecorderUseCase.getRecordFileUriProvider()
                        if (uriProvider == null) {
                            return@ConversationDetailsPage
                        }
                        conversationUseCase.onSendMessage(
                            context,
                            InputSelector.VOICE,
                            uriProvider
                        )
                    },
                    onVoicePauseClick = {
                        mediaAudioRecorderUseCase.pauseRecord("UI click")
                    },
                    onVoiceResumeClick = {
                        mediaAudioRecorderUseCase.resumeRecord("UI click")
                    },
                    onMoreClick = { imObj ->
                        anyStateProvider.bottomSheetState()
                            .show {
                                ConversationDetailsMoreInfo(
                                    imObj = imObj,
                                    onBioClick = { bio ->
                                        onBioClick(context, navController, bio)
                                    },
                                    onRequestAddFriend = {},
                                    onRequestDeleteFriend = {},
                                    onRequestJoinGroup = {},
                                    onRequestLeaveGroup = {},
                                    onRequestDeleteGroup = {},
                                )
                            }
                    }
                )
            }

            composable(PageRouteNames.ConversationOverviewPage) {
                val context = LocalContext.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val anyStateProvider = LocalAnyStateProvider.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val mediaAudioRecorderUseCase = LocalUseCaseOfMediaAudioRecorder.current
                val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
                val recorderState = mediaAudioRecorderUseCase.recorderState
                ConversationOverviewPage(
                    sessionState = conversationUseCase.currentSessionState.value,
                    isShowActions = conversationUseCase.isShowActions.value,
                    recorderState = recorderState.value,
                    onBioClick = { bio ->
                        onBioClick(context, navController, bio)
                    },
                    onImMessageContentClick = { imMessage ->
                        when (imMessage) {
                            is MusicMessage, is VoiceMessage -> {
                                val commonURIJson = CommonURIJson(
                                    imMessage.id,
                                    imMessage.metadata.description,
                                    imMessage.metadata.url ?: ""
                                )
                                mediaRemoteExoUseCase.playOrPauseAudio(commonURIJson)
                            }

                            is VideoMessage -> {
                                navigateToVideoPlaybackActivity(context, imMessage)
                            }

                            is ImageMessage -> {
                                val imMessageOfImageUrls =
                                    conversationUseCase.findCurrentSessionAllImMessageOfImage()
                                        .map { it.metadata.url }
                                showPictureViewDialog(
                                    anyStateProvider,
                                    context,
                                    imMessage.metadata.url,
                                    imMessageOfImageUrls
                                )
                            }

                            is HTMLMessage -> {
                                showWebBrowserDialog(
                                    context,
                                    anyStateProvider,
                                    imMessage.metadata.data
                                )
                            }
                        }
                    },
                    onAddAIModelClick = {
                        navController.navigate(PageRouteNames.ConversationAIGCMarketPage)
                    },
                    onAddFriendClick = {
                        navController.navigate(PageRouteNames.SearchPage)
                    },
                    onAddGroupClick = {
                        navController.navigate(PageRouteNames.SearchPage)
                    },
                    onCreateGroupClick = {
                        navController.navigate(PageRouteNames.CreateGroupPage)
                    },
                    onConversionSessionClick = { session, shouldNavigateNewPage ->
                        conversationUseCase.updateCurrentSessionBySession(session)
                        if (shouldNavigateNewPage) {
                            navController.navigate(PageRouteNames.ConversationDetailsPage) {
                                popUpTo(PageRouteNames.ConversationDetailsPage) {
                                    inclusive = true
                                }
                            }
                        }
                    },
                    onSystemImMessageClick = { session, imMessage ->

                    },
                    onUserRequestClick = { result, session: Session, imMessage: SystemMessage ->
                        systemUseCase.handleUserRequestResult(
                            context, result, session, imMessage
                        )
                    },
                    onInputMoreAction = { requestKey ->
                        showContentSelectionDialog(
                            context,
                            anyStateProvider,
                            navController,
                            PageRouteNames.ConversationDetailsPage,
                            requestKey,
                        )
                    },
                    onVoiceAction = {
                        mediaAudioRecorderUseCase.startRecord(context, navController)
                    },
                    onVoiceStopClick = { showSend ->
                        if (!showSend) {
                            return@ConversationOverviewPage
                        }
                        val uriProvider = mediaAudioRecorderUseCase.getRecordFileUriProvider()
                        if (uriProvider == null) {
                            return@ConversationOverviewPage
                        }
                        conversationUseCase.onSendMessage(
                            context,
                            InputSelector.VOICE,
                            uriProvider
                        )
                    },
                    onVoicePauseClick = {
                        mediaAudioRecorderUseCase.pauseRecord("UI click")
                    },
                    onVoiceResumeClick = {
                        mediaAudioRecorderUseCase.resumeRecord("UI click")
                    },
                    onMoreClick = { imObj ->
                        anyStateProvider.bottomSheetState()
                            .show {
                                ConversationDetailsMoreInfo(
                                    imObj = imObj,
                                    onBioClick = { bio ->
                                        onBioClick(context, navController, bio)
                                    },
                                    onRequestAddFriend = {},
                                    onRequestDeleteFriend = {},
                                    onRequestJoinGroup = {},
                                    onRequestLeaveGroup = {},
                                    onRequestDeleteGroup = {},
                                )
                            }
                    },
                    onLandscapeModeEndBackClick = {
                        conversationUseCase.updateCurrentSession(null)
                    }
                )
            }

            composable(PageRouteNames.ScreenDetailsPage) {
                val context = LocalContext.current
                val screensUseCase = LocalUseCaseOfScreen.current
                val anyStateProvider = LocalAnyStateProvider.current
                ScreenDetailsPage(
                    viewScreenInfo = screensUseCase.currentViewScreenInfo.value,
                    onBackClick = navController::navigateUp,
                    onBioClick = { bio ->
                        onBioClick(context, navController, bio)
                    },
                    onEditClick = {
                        navController.navigate(PageRouteNames.ScreenEditPage)
                    },
                    onCollectClick = { category ->
                        screensUseCase.userClickCollectScreen(context, category)
                    },
                    onLikesClick = {
                        screensUseCase.userClickLikeScreen(context)
                    },
                    onInputReview = screensUseCase::onInputReview,
                    onReviewConfirm = {
                        screensUseCase.onReviewConfirm(context)
                    },
                    onScreenMediaClick = { url, urls ->
                        restrictedContentConfirmCallback = {
                            if (url.isVideoMedia) {
                                navigateToVideoPlaybackActivity(context, url)
                            } else {
                                showPictureViewDialog(
                                    anyStateProvider,
                                    context,
                                    url.mediaFileUrl,
                                    urls.map { fileUrl -> fileUrl.mediaFileUrl })
                            }

                        }
                        if (url.isRestrictedContent) {
                            isShowRestrictedContentDialog = true
                        } else {
                            restrictedContentConfirmCallback?.invoke()
                        }
                    },
                    onPageShowPrevious = {
                        screensUseCase.updatePageShowPrevious()
                    },
                    onPageShowNext = {
                        screensUseCase.updatePageShowNext()
                    }
                )
            }

            composable(PageRouteNames.UserProfilePage) {
                val context = LocalContext.current
                val userInfoUseCase = LocalUseCaseOfUserInfo.current
                val anyStateProvider = LocalAnyStateProvider.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val screensUseCase = LocalUseCaseOfScreen.current
                UserProfilePage(
                    onBackClick = navController::navigateUp,
                    userProfileState = userInfoUseCase.currentUserInfoState.value,
                    userApplications = userInfoUseCase.applicationsState.value,
                    userFollowers = userInfoUseCase.followerUsersState.value,
                    userFollowed = userInfoUseCase.followedUsersState.value,
                    isLoginUserFollowedThisUser = userInfoUseCase.loggedUserFollowedState.value,
                    userScreens = screensUseCase.userScreensContainer.screens,
                    onAddFriendClick = { userInfo ->
                        systemUseCase.requestAddFriend(
                            context,
                            userInfo.uid,
                            context.getString(xcj.app.appsets.R.string.hello_i_want_to_make_friends_with_you),
                            context.getString(xcj.app.appsets.R.string.nothing)
                        )
                    },
                    onFlipFollowClick = { userInfo ->
                        systemUseCase.flipFollowToUserState(userInfo) {
                            userInfoUseCase.updateUserFollowState()
                        }
                    },
                    onChatClick = { userInfo ->
                        conversationUseCase.updateCurrentSessionByBio(userInfo)
                        navController.navigate(PageRouteNames.ConversationDetailsPage) {
                            popUpTo(PageRouteNames.ConversationDetailsPage) {
                                inclusive = true
                            }
                        }
                    },
                    onBioClick = { bio ->
                        onBioClick(context, navController, bio)
                    },
                    onScreenMediaClick = { url, urls ->
                        restrictedContentConfirmCallback = {
                            if (url.isVideoMedia) {
                                navigateToVideoPlaybackActivity(context, url)
                            } else {
                                showPictureViewDialog(
                                    anyStateProvider,
                                    context,
                                    url.mediaFileUrl,
                                    urls.map { fileUrl -> fileUrl.mediaFileUrl })
                            }

                        }
                        if (url.isRestrictedContent) {
                            isShowRestrictedContentDialog = true
                        } else {
                            restrictedContentConfirmCallback?.invoke()
                        }
                    },
                    onLoadMoreScreens = { uid, force ->
                        screensUseCase.loadMore(uid, force)
                    },
                    onSelectUserAvatarClick = { requestKey ->
                        showContentSelectionDialog(
                            context,
                            anyStateProvider,
                            navController,
                            PageRouteNames.UserProfilePage,
                            requestKey,
                        )
                    },
                    onModifyProfileConfirmClick = {
                        userInfoUseCase.modifyUserInfo(context)
                    }
                )
            }

            composable(PageRouteNames.GroupInfoPage) {
                val context = LocalContext.current
                val groupInfoUseCase = LocalUseCaseOfGroupInfo.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val groupInfoState = groupInfoUseCase.groupInfoState.value
                GroupInfoPage(
                    groupInfoState = groupInfoState,
                    onBackClick = navController::navigateUp,
                    onBioClick = { bio ->
                        onBioClick(context, navController, bio)
                    },
                    onChatClick = { groupInfo ->
                        conversationUseCase.updateCurrentSessionByBio(groupInfo)
                        navController.navigate(PageRouteNames.ConversationDetailsPage) {
                            popUpTo(PageRouteNames.ConversationDetailsPage) {
                                inclusive = true
                            }
                        }
                    },
                    onJoinGroupRequestClick = { groupInfo ->
                        systemUseCase.requestJoinGroup(
                            context,
                            groupInfo.groupId,
                            context.getString(xcj.app.appsets.R.string.this_group_looks_interesting_can_i_join),
                            context.getString(xcj.app.appsets.R.string.nothing)
                        )
                    }
                )
            }

            composable(PageRouteNames.AppDetailsPage) {
                val application =
                    it.arguments?.getParcelable(Constants.APP_INFO) as? Application
                val context = LocalContext.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val anyStateProvider = LocalAnyStateProvider.current
                val coroutineScope = rememberCoroutineScope()
                AppDetailsPage(
                    application = application,
                    onBackClick = navController::navigateUp,
                    onGetApplicationClick = { application ->
                        anyStateProvider.bottomSheetState()
                            .show {
                                DownloadBottomSheetComponent(
                                    application = application,
                                    onDownloadInfoGetClick = { application, downloadInfo ->
                                        coroutineScope.launch {
                                            handleApplicationDownload(
                                                context,
                                                application,
                                                downloadInfo
                                            )
                                        }
                                    }
                                )
                            }
                    },
                    onShowApplicationCreatorClick = { application ->
                        navigateToUserInfoPage(context, navController, application.createUid)
                    },
                    onAddPlatformInfoClick = { platform ->
                        navigateToCreateAppPage(
                            navController,
                            application,
                            platform,
                            null,
                            ApplicationForCreate.CREATE_STEP_PLATFORM
                        )
                    },
                    onAddVersionInfoClick = { platform ->
                        navigateToCreateAppPage(
                            navController,
                            application,
                            platform,
                            null,
                            ApplicationForCreate.CREATE_STEP_VERSION
                        )
                    },
                    onAddScreenshotInfoClick = { platform, version ->
                        navigateToCreateAppPage(
                            navController,
                            application,
                            platform,
                            version,
                            ApplicationForCreate.CREATE_STEP_SCREENSHOT
                        )
                    },
                    onAddDownloadInfoClick = { platform, version ->
                        navigateToCreateAppPage(
                            navController,
                            application,
                            platform,
                            version,
                            ApplicationForCreate.CREATE_STEP_DOWNLOAD
                        )
                    },
                    onAppScreenShotClick = { screenshot, screenshotList ->
                        showPictureViewDialog(
                            anyStateProvider,
                            context,
                            screenshot.url,
                            screenshotList.map { it.url }
                        )
                    },
                    onJoinToChatClick = { application ->
                        conversationUseCase.updateCurrentSessionByBio(application)
                        navController.navigate(PageRouteNames.ConversationDetailsPage) {
                            popUpTo(PageRouteNames.ConversationDetailsPage) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(PageRouteNames.ScreenEditPage) {
                val screenUseCase = LocalUseCaseOfScreen.current
                ScreenEditPage(
                    screenInfo = screenUseCase.currentViewScreenInfo.value.screenInfo,
                    onBackClick = navController::navigateUp,
                    onPublicStateChanged = { newIsPublic ->
                        screenUseCase.changeScreenPublicState(newIsPublic)
                    }
                )
            }

            composable(PageRouteNames.PrivacyPage) {
                val context = LocalContext.current
                var androidPermissionsUsageList by remember {
                    mutableStateOf(PlatformUseCase.providePlatformPermissions(context))
                }
                val activityLifecycleUseCase = LocalUseCaseOfActivityLifecycle.current
                LaunchedEffect(activityLifecycleUseCase.activityResumeState.value) {
                    androidPermissionsUsageList =
                        PlatformUseCase.providePlatformPermissions(context)
                }
                val privacy = SystemUseCase.providePrivacy(context)
                PrivacyPage(
                    onBackClick = navController::navigateUp,
                    privacy = privacy,
                    platformPermissionsUsageList = androidPermissionsUsageList,
                    onRequest = { permission, type ->
                        if (type == 0) {
                            PlatformUseCase.navigateToExternalSystemAppDetails(context)
                        } else {
                            PlatformUseCase.requestPermission(
                                context,
                                permission.androidDefinitionNames
                            )
                        }
                    }
                )
            }
        }
        RestrictedContentDialog(
            isShow = isShowRestrictedContentDialog,
            onConfirmClick = restrictedContentConfirmCallback,
            onDismissRequest = {
                isShowRestrictedContentDialog = false
            }
        )
    }
}