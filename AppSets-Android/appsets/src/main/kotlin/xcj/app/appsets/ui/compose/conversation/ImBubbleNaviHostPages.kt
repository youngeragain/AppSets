package xcj.app.appsets.ui.compose.conversation

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.os.BundleCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import kotlinx.coroutines.launch
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.im.InputSelector
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfGroupInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaAudioRecorder
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreen
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.LocalUseCaseOfUserInfo
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.apps.AppDetailsPage
import xcj.app.appsets.ui.compose.apps.DownloadBottomSheetContent
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionTypes
import xcj.app.appsets.ui.compose.content_selection.defaultAllSelectionTypeParam
import xcj.app.appsets.ui.compose.content_selection.defaultImageSelectionTypeParam
import xcj.app.appsets.ui.compose.group.GroupInfoPage
import xcj.app.appsets.ui.compose.main.DesignNaviHost
import xcj.app.appsets.ui.compose.main.handleApplicationDownload
import xcj.app.appsets.ui.compose.main.handleImMessageContentClick
import xcj.app.appsets.ui.compose.main.handleScreenMediaClick
import xcj.app.appsets.ui.compose.main.navigateToCreateAppPage
import xcj.app.appsets.ui.compose.main.navigateToUserInfoPage
import xcj.app.appsets.ui.compose.main.onBioClick
import xcj.app.appsets.ui.compose.main.showContentSelectionDialog
import xcj.app.appsets.ui.compose.main.showPictureViewDialog
import xcj.app.appsets.ui.compose.outside.RestrictedContentDialog
import xcj.app.appsets.ui.compose.outside.ScreenDetailsPage
import xcj.app.appsets.ui.compose.outside.ScreenEditPage
import xcj.app.appsets.ui.compose.privacy.PrivacyPage
import xcj.app.appsets.ui.compose.user.UserProfilePage
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.appsets.util.BundleDefaults
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.usecase.PlatformUseCase

@Composable
fun ImBubbleNaviHostPages(navController: NavHostController) {
    val isShowRestrictedContentDialogState = remember {
        mutableStateOf(false)
    }
    val restrictedContentConfirmCallbackState: MutableState<(() -> Unit)?> = remember {
        mutableStateOf(null)
    }

    var isShowRestrictedContentDialog by isShowRestrictedContentDialogState
    val restrictedContentConfirmCallback by restrictedContentConfirmCallbackState

    Box {
        DesignNaviHost(
            navController = navController,
            startDestination = PageRouteNames.ConversationDetailsPage,
        ) {
            composable(PageRouteNames.ConversationDetailsPage) {
                val context = LocalContext.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val anyStateProvider = LocalVisibilityComposeStateProvider.current
                val mediaAudioRecorderUseCase = LocalUseCaseOfMediaAudioRecorder.current
                val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
                val sessionState by conversationUseCase.currentSessionState
                val recorderState by mediaAudioRecorderUseCase.recorderState
                ConversationDetailsPage(
                    sessionState = sessionState,
                    recorderState = recorderState,
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
                        handleImMessageContentClick(
                            context,
                            imMessage,
                            conversationUseCase,
                            mediaRemoteExoUseCase,
                            anyStateProvider
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
                                ConversationDetailsMoreInfoSheetContent(
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
                val anyStateProvider = LocalVisibilityComposeStateProvider.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val mediaAudioRecorderUseCase = LocalUseCaseOfMediaAudioRecorder.current
                val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
                val recorderState by mediaAudioRecorderUseCase.recorderState
                val isShowActions by conversationUseCase.isShowActions
                val sessionState by conversationUseCase.currentSessionState
                ConversationOverviewPage(
                    sessionState = sessionState,
                    isShowActions = isShowActions,
                    recorderState = recorderState,
                    onBioClick = { bio ->
                        onBioClick(context, navController, bio)
                    },
                    onImMessageContentClick = { imMessage ->
                        handleImMessageContentClick(
                            context,
                            imMessage,
                            conversationUseCase,
                            mediaRemoteExoUseCase,
                            anyStateProvider
                        )
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
                            requestSelectionTypeParams = defaultAllSelectionTypeParam { selectionType ->
                                if (selectionType == ContentSelectionTypes.IMAGE) {
                                    100
                                } else {
                                    1
                                }
                            }
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
                                ConversationDetailsMoreInfoSheetContent(
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
                val screenUseCase = LocalUseCaseOfScreen.current
                val anyStateProvider = LocalVisibilityComposeStateProvider.current
                val coroutineScope = rememberCoroutineScope()
                val screenInfoForCard by screenUseCase.currentScreenInfoForCard
                ScreenDetailsPage(
                    screenInfoForCard = screenInfoForCard,
                    onBackClick = navController::navigateUp,
                    onBioClick = { bio ->
                        onBioClick(context, navController, bio)
                    },
                    onEditClick = {
                        navController.navigate(PageRouteNames.ScreenEditPage)
                    },
                    onCollectClick = { category ->
                        coroutineScope.launch {
                            screenUseCase.userClickCollectScreen(context, category)
                        }
                    },
                    onLikesClick = {
                        coroutineScope.launch {
                            screenUseCase.userClickLikeScreen(context)
                        }
                    },
                    onReviewConfirm = { reviewString ->
                        coroutineScope.launch {
                            screenUseCase.onReviewConfirm(context, reviewString)
                        }
                    },
                    onScreenMediaClick = { url, urls ->
                        handleScreenMediaClick(
                            context,
                            isShowRestrictedContentDialogState,
                            restrictedContentConfirmCallbackState,
                            anyStateProvider,
                            url,
                            urls
                        )
                    },
                    onPageShowPrevious = {
                        coroutineScope.launch {
                            screenUseCase.updatePageShowPrevious()
                        }
                    },
                    onPageShowNext = {
                        screenUseCase.updatePageShowNext()
                    }
                )
            }

            composable(PageRouteNames.UserProfilePage) {
                val context = LocalContext.current
                val userInfoUseCase = LocalUseCaseOfUserInfo.current
                val anyStateProvider = LocalVisibilityComposeStateProvider.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val screenUseCase = LocalUseCaseOfScreen.current
                val coroutineScope = rememberCoroutineScope()
                val userProfilePageState by userInfoUseCase.currentUserInfoState
                val userApplications by userInfoUseCase.applicationsState
                val userFollowers by userInfoUseCase.followerUsersState
                val userFollowed by userInfoUseCase.followedUsersState
                val isLoginUserFollowedThisUser by userInfoUseCase.loggedUserFollowedState
                val userScreens = screenUseCase.userScreensContainer.screens
                UserProfilePage(
                    onBackClick = navController::navigateUp,
                    userProfilePageState = userProfilePageState,
                    userApplications = userApplications,
                    userFollowers = userFollowers,
                    userFollowed = userFollowed,
                    isLoginUserFollowedThisUser = isLoginUserFollowedThisUser,
                    userScreens = userScreens,
                    onAddFriendClick = { userInfo ->
                        systemUseCase.requestAddFriend(
                            context,
                            userInfo.uid,
                            ContextCompat.getString(
                                context,
                                xcj.app.appsets.R.string.hello_i_want_to_make_friends_with_you
                            ),
                            ContextCompat.getString(context, xcj.app.appsets.R.string.nothing)
                        )
                    },
                    onFlipFollowClick = { userInfo ->
                        systemUseCase.flipFollowToUserState(userInfo, userInfoUseCase)
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
                        handleScreenMediaClick(
                            context,
                            isShowRestrictedContentDialogState,
                            restrictedContentConfirmCallbackState,
                            anyStateProvider,
                            url,
                            urls
                        )
                    },
                    onLoadMoreScreens = { uid, force ->
                        coroutineScope.launch {
                            screenUseCase.loadMore(uid, force)
                        }
                    },
                    onSelectUserAvatarClick = { requestKey ->
                        showContentSelectionDialog(
                            context,
                            anyStateProvider,
                            navController,
                            PageRouteNames.UserProfilePage,
                            requestKey,
                            requestSelectionTypeParams = defaultImageSelectionTypeParam()
                        )
                    },
                    onModifyProfileConfirmClick = {
                        coroutineScope.launch {
                            userInfoUseCase.modifyUserInfo(context)
                        }
                    }
                )
            }

            composable(PageRouteNames.GroupInfoPage) {
                val context = LocalContext.current
                val groupInfoUseCase = LocalUseCaseOfGroupInfo.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val groupInfoState by groupInfoUseCase.groupInfoPageState
                GroupInfoPage(
                    groupInfoPageState = groupInfoState,
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
                            ContextCompat.getString(
                                context,
                                xcj.app.appsets.R.string.this_group_looks_interesting_can_i_join
                            ),
                            ContextCompat.getString(context, xcj.app.appsets.R.string.nothing)
                        )
                    }
                )
            }

            composable(PageRouteNames.AppDetailsPage) {
                val application =
                    BundleCompat.getParcelable(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.APP_INFO,
                        Application::class.java
                    )
                val context = LocalContext.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val anyStateProvider = LocalVisibilityComposeStateProvider.current
                val coroutineScope = rememberCoroutineScope()
                AppDetailsPage(
                    application = application,
                    onBackClick = navController::navigateUp,
                    onGetApplicationClick = { application, appPlatform ->
                        anyStateProvider.bottomSheetState()
                            .show {
                                DownloadBottomSheetContent(
                                    application = application,
                                    appPlatform = appPlatform,
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
                    onAppScreenshotClick = { screenshot, screenshotList ->
                        val currentUri = screenshot.url ?: return@AppDetailsPage
                        val uriList = screenshotList.mapNotNull { screenshot -> screenshot.url }
                        showPictureViewDialog(
                            anyStateProvider,
                            context,
                            currentUri,
                            uriList
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
                val coroutineScope = rememberCoroutineScope()
                val currentScreenInfoForCard by screenUseCase.currentScreenInfoForCard
                ScreenEditPage(
                    screenInfo = currentScreenInfoForCard.screenInfo,
                    onBackClick = navController::navigateUp,
                    onPublicStateChanged = { newIsPublic ->
                        coroutineScope.launch {
                            screenUseCase.changeScreenPublicState(newIsPublic)
                        }
                    }
                )
            }

            composable(PageRouteNames.PrivacyPage) {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycle = lifecycleOwner.lifecycle
                val lifecycleState by lifecycle.currentStateAsState()
                val privacy = remember {
                    SystemUseCase.providePrivacy(context)
                }
                var androidPermissionsUsageList by remember {
                    mutableStateOf(PlatformUseCase.providePlatformPermissions(context))
                }
                LaunchedEffect(lifecycleState) {
                    androidPermissionsUsageList =
                        PlatformUseCase.providePlatformPermissions(context)
                }
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