@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.gson.Gson
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.InputSelector
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.HTMLMessage
import xcj.app.appsets.im.message.ImageMessage
import xcj.app.appsets.im.message.MusicMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.message.VideoMessage
import xcj.app.appsets.im.message.VoiceMessage
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.PlatForm
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.VersionInfo
import xcj.app.appsets.ui.base.BaseIMViewModel
import xcj.app.appsets.ui.compose.LocalUseCaseOfActivityLifecycle
import xcj.app.appsets.ui.compose.LocalUseCaseOfAppCreation
import xcj.app.appsets.ui.compose.LocalUseCaseOfApps
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfGroupInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaAudioRecorder
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.compose.LocalUseCaseOfQRCode
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreen
import xcj.app.appsets.ui.compose.LocalUseCaseOfScreenPost
import xcj.app.appsets.ui.compose.LocalUseCaseOfSearch
import xcj.app.appsets.ui.compose.LocalUseCaseOfSystem
import xcj.app.appsets.ui.compose.LocalUseCaseOfUserInfo
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.apps.AppDetailsPage
import xcj.app.appsets.ui.compose.apps.AppsCenterPage
import xcj.app.appsets.ui.compose.apps.CreateAppPage
import xcj.app.appsets.ui.compose.apps.DownloadBottomSheetComponent
import xcj.app.appsets.ui.compose.apps.tools.AppToolsDetailsPage
import xcj.app.appsets.ui.compose.apps.tools.AppToolsPage
import xcj.app.appsets.ui.compose.apps.tools.TOOL_TYPE
import xcj.app.appsets.ui.compose.apps.tools.TOOL_TYPE_AppSets_Compose_plugin
import xcj.app.appsets.ui.compose.apps.tools.TOOL_TYPE_AppSets_Launcher
import xcj.app.appsets.ui.compose.apps.tools.TOOL_TYPE_AppSets_Proxy
import xcj.app.appsets.ui.compose.apps.tools.TOOL_TYPE_AppSets_Share
import xcj.app.appsets.ui.compose.camera.CameraComposeActivity
import xcj.app.appsets.ui.compose.content_selection.ContentSelectDialog
import xcj.app.appsets.ui.compose.conversation.ConversationDetailsMoreInfo
import xcj.app.appsets.ui.compose.conversation.ConversationDetailsPage
import xcj.app.appsets.ui.compose.conversation.ConversationOverviewPage
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.group.CreateGroupPage
import xcj.app.appsets.ui.compose.group.GroupInfoPage
import xcj.app.appsets.ui.compose.login.LoginInterceptorPage
import xcj.app.appsets.ui.compose.login.LoginPage
import xcj.app.appsets.ui.compose.login.SignUpPage
import xcj.app.appsets.ui.compose.media.video.single.MediaPlaybackActivity
import xcj.app.appsets.ui.compose.outside.CreateScreenPage
import xcj.app.appsets.ui.compose.outside.OutSidePage
import xcj.app.appsets.ui.compose.outside.RestrictedContentDialog
import xcj.app.appsets.ui.compose.outside.ScreenDetailsPage
import xcj.app.appsets.ui.compose.outside.ScreenEditPage
import xcj.app.appsets.ui.compose.privacy.PrivacyPage
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.search.SearchPage
import xcj.app.appsets.ui.compose.settings.AboutPage
import xcj.app.appsets.ui.compose.settings.SettingsPage
import xcj.app.appsets.ui.compose.user.UserProfilePage
import xcj.app.appsets.ui.compose.web.WebComponent
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.usecase.AppsUseCase
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.compose_share.components.AnyStateProvider
import xcj.app.compose_share.components.LocalAnyStateProvider
import xcj.app.compose_share.components.ProgressedComposeContainerState
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.immerseContentState
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.usecase.PlatformUseCase
import xcj.app.starter.android.util.LocalMessager
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.ComposeEvent
import xcj.app.starter.test.LocalPurpleEventPublisher
import xcj.app.starter.test.NaviHostParams

private const val TAG = "MainNaviHostPages"

@Composable
fun MainNaviHostPages(navController: NavHostController) {

    var isShowRestrictedContentDialog by remember {
        mutableStateOf(false)
    }
    var restrictedContentConfirmCallback: (() -> Unit)? by remember {
        mutableStateOf(null)
    }

    Box {
        DesignNaviHost(
            navController = navController,
            startDestination = PageRouteNames.AppsCenterPage,
        ) {

            publishComposeNaviHostFormedEvent(navController, this)

            composable(PageRouteNames.AppsCenterPage) {
                val context = LocalContext.current
                val appsUseCase = LocalUseCaseOfApps.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                AppsCenterPage(
                    appCenterState = appsUseCase.appCenterState.value,
                    onBioClick = { bio ->
                        onBioClick(context, navController, bio)
                    },
                    onApplicationLongPress = { application ->
                        conversationUseCase.updateCurrentSessionByBio(application)
                        navController.navigate(PageRouteNames.ConversationDetailsPage) {
                            popUpTo(PageRouteNames.ConversationDetailsPage) {
                                inclusive = true
                            }
                        }
                    }
                )
            }

            composable(PageRouteNames.AppDetailsPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {

                    val application =
                        it.arguments?.getParcelable(Constants.APP_INFO) as? Application
                    val context = LocalContext.current
                    val conversationUseCase = LocalUseCaseOfConversation.current
                    val anyStateProvider = LocalAnyStateProvider.current

                    AppDetailsPage(
                        application = application,
                        onBackClick = navController::navigateUp,
                        onGetApplicationClick = { application ->
                            anyStateProvider.bottomSheetState()
                                .show {
                                    DownloadBottomSheetComponent(application = application)
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
            }

            composable(PageRouteNames.CreateAppPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val application =
                        it.arguments?.getParcelable(Constants.APP_INFO) as? Application
                    val platform =
                        it.arguments?.getString(Constants.PLATFORM_ID)?.let { platformId ->
                            application?.platforms?.firstOrNull { platform -> platform.id == platformId }
                        }
                    val versionInfo =
                        it.arguments?.getString(Constants.VERSION_ID)?.let { versionId ->
                            platform?.versionInfos?.firstOrNull { versionInfo -> versionInfo.id == versionId }
                        }

                    //标志创建的是整个application还是platform或者version_info或者screenshot_info或者download_info
                    val createStep = it.arguments?.getString(
                        Constants.CREATE_STEP, ApplicationForCreate.CREATE_STEP_APPLICATION
                    ) ?: ApplicationForCreate.CREATE_STEP_APPLICATION

                    val context = LocalContext.current
                    val appCreationUseCase = LocalUseCaseOfAppCreation.current
                    val anyStateProvider = LocalAnyStateProvider.current

                    LaunchedEffect(key1 = true, block = {
                        appCreationUseCase.inflateApplication(application)
                    })

                    CreateAppPage(
                        onBackClick = navController::navigateUp,
                        createStep = createStep,
                        platform = platform,
                        versionInfo = versionInfo,
                        createApplicationState = appCreationUseCase.createApplicationState.value,
                        onApplicationForCreateFiledChanged = appCreationUseCase::onApplicationForCreateFiledChanged,
                        onChoosePictureClick = { any, filedName, uriHolder ->
                            val requestKey = "CREATE_APP_PICTURE_REQUEST"
                            appCreationUseCase.setChooseContentTempValues(
                                any, filedName
                            )
                            showContentSelectionDialog(
                                context,
                                anyStateProvider,
                                navController,
                                PageRouteNames.CreateAppPage,
                                requestKey,
                            )
                        },
                        onConfirmClick = {
                            if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION) {
                                context.getString(xcj.app.appsets.R.string.currently_cannot_be_modified)
                                    .toast()
                                return@CreateAppPage
                            }
                            appCreationUseCase.finishCreateApp(context)
                        }
                    )
                }
            }

            composable(PageRouteNames.OutSidePage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val screensUseCase = LocalUseCaseOfScreen.current
                    val anyStateProvider = LocalAnyStateProvider.current
                    OutSidePage(
                        screens = screensUseCase.systemScreensContainer.screens,
                        onBioClick = { bio ->
                            onBioClick(context, navController, bio)
                        },
                        onLoadMore = {
                            screensUseCase.loadMore(null, false)
                        },
                        onPictureClick = { url, urls ->
                            restrictedContentConfirmCallback = {
                                showPictureViewDialog(
                                    anyStateProvider,
                                    context,
                                    url.mediaFileUrl,
                                    urls.map { fileUrl -> fileUrl.mediaFileUrl })
                            }
                            if (url.isRestrictedContent) {
                                isShowRestrictedContentDialog = true
                            } else {
                                restrictedContentConfirmCallback?.invoke()
                            }
                        },
                        onScreenVideoPlayClick = { mediaFileUrl ->
                            restrictedContentConfirmCallback = {
                                navigateToVideoPlaybackActivity(context, mediaFileUrl)
                            }
                            if (mediaFileUrl.isRestrictedContent) {
                                isShowRestrictedContentDialog = true
                            } else {
                                restrictedContentConfirmCallback?.invoke()
                            }
                        }
                    )
                }
            }

            composable(PageRouteNames.ScreenDetailsPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
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
                        onPictureClick = { url, urls ->
                            restrictedContentConfirmCallback = {
                                showPictureViewDialog(
                                    anyStateProvider,
                                    context,
                                    url.mediaFileUrl,
                                    urls.map { fileUrl -> fileUrl.mediaFileUrl })
                            }
                            if (url.isRestrictedContent) {
                                isShowRestrictedContentDialog = true
                            } else {
                                restrictedContentConfirmCallback?.invoke()
                            }
                        },
                        onScreenVideoPlayClick = { mediaFileUrl ->
                            restrictedContentConfirmCallback = {
                                navigateToVideoPlaybackActivity(context, mediaFileUrl)
                            }
                            if (mediaFileUrl.isRestrictedContent) {
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
            }

            composable(PageRouteNames.CreateScreenPage) {

                val quickStepContents =
                    it.arguments?.getParcelableArrayList<QuickStepContent>(Constants.QUICK_STEP_CONTENT)

                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val screenUseCase = LocalUseCaseOfScreen.current
                    val screenPostUseCase = LocalUseCaseOfScreenPost.current
                    val systemUseCase = LocalUseCaseOfSystem.current
                    val anyStateProvider = LocalAnyStateProvider.current
                    CreateScreenPage(
                        quickStepContents = quickStepContents,
                        onBackClick = { shouldRefresh ->
                            if (shouldRefresh) {
                                screenUseCase.loadOutSideScreens()
                            }
                            navController.navigateUp()
                        },
                        onConfirmClick = {
                            screenPostUseCase.createScreen(context)
                        },
                        onIsPublicClick = screenPostUseCase::onIsPublicClick,
                        onGenerateClick = {
                            screenPostUseCase.generateContent(context)
                        },
                        onInputContent = screenPostUseCase::onInputContent,
                        onInputTopics = screenPostUseCase::onInputTopics,
                        onInputPeoples = screenPostUseCase::onInputPeoples,
                        onAddMediaFallClick = screenPostUseCase::onAddMediaFallClick,

                        onAddMediaContentClick = { requestKey, requestType ->
                            showContentSelectionDialog(
                                context,
                                anyStateProvider,
                                navController,
                                PageRouteNames.CreateScreenPage,
                                requestKey,
                                requestTypes = listOf(requestType)
                            )
                        },
                        onRemoveMediaContent = { type, scalableItemState ->
                            screenPostUseCase.onRemoveMediaContent(
                                type,
                                scalableItemState
                            )
                        },
                        onVideoPlayClick = { uriProvider ->
                            navigateToVideoPlaybackActivity(context, uriProvider)
                        }
                    )
                }
            }

            composable(PageRouteNames.ScreenEditPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val screenUseCase = LocalUseCaseOfScreen.current
                    ScreenEditPage(
                        screenInfo = screenUseCase.currentViewScreenInfo.value.screenInfo,
                        onBackClick = navController::navigateUp,
                        onPublicStateChanged = { newIsPublic ->
                            screenUseCase.changeScreenPublicState(newIsPublic)
                        }
                    )
                }
            }

            composable(PageRouteNames.ConversationOverviewPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val conversationUseCase = LocalUseCaseOfConversation.current
                    val anyStateProvider = LocalAnyStateProvider.current
                    val systemUseCase = LocalUseCaseOfSystem.current
                    val mediaAudioRecorderUseCase = LocalUseCaseOfMediaAudioRecorder.current
                    val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
                    val recorderState = mediaAudioRecorderUseCase.recorderState
                    ConversationOverviewPage(
                        sessionState = conversationUseCase.currentSessionState.value,
                        isShowAddActions = conversationUseCase.isShowAddActions.value,
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
                        onAddFriendClick = {
                            navController.navigate(PageRouteNames.SearchPage)
                        },
                        onJoinGroupClick = {
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
                            mediaAudioRecorderUseCase.stopRecord("UI click")
                            if (!showSend) {
                                mediaAudioRecorderUseCase.cleanUp("user stop")
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
            }

            composable(PageRouteNames.ConversationDetailsPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val conversationUseCase = LocalUseCaseOfConversation.current
                    val anyStateProvider = LocalAnyStateProvider.current
                    val mediaAudioRecorderUseCase = LocalUseCaseOfMediaAudioRecorder.current
                    val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
                    ConversationDetailsPage(
                        sessionState = conversationUseCase.currentSessionState.value,
                        recorderState = mediaAudioRecorderUseCase.recorderState.value,
                        onBackClick = navController::navigateUp,
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
                        onInputMoreAction = { requestType ->
                            showContentSelectionDialog(
                                context,
                                anyStateProvider,
                                navController,
                                PageRouteNames.ConversationDetailsPage,
                                requestType
                            )
                        },
                        onVoiceAction = {
                            mediaAudioRecorderUseCase.startRecord(context, navController)
                        },
                        onVoiceStopClick = { showSend ->
                            mediaAudioRecorderUseCase.stopRecord("UI click")
                            if (!showSend) {
                                mediaAudioRecorderUseCase.cleanUp("user stop")
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
            }

            composable(PageRouteNames.LoginPage) {
                val context = LocalContext.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val qrCodeUseCase = LocalUseCaseOfQRCode.current
                val anyStateProvider = LocalAnyStateProvider.current
                LaunchedEffect(true) {
                    systemUseCase.prepareLoginState()
                }
                LoginPage(
                    loginSignUpState = systemUseCase.loginSignUpState.value,
                    onBackClick = navController::navigateUp,
                    qrCodeInfo = qrCodeUseCase.generatedQRCodeInfo.value,
                    onSignUpButtonClick = {
                        navController.navigate(PageRouteNames.SignUpPage)
                    },
                    onQRCodeLoginButtonClick = {
                        qrCodeUseCase.requestGenerateQRCode()
                    },
                    onScanQRCodeButtonClick = {
                        navigateToCameraActivity(context, navController)
                    },
                    onLoginConfirmButtonClick = { account, password ->
                        systemUseCase.login(
                            context,
                            account,
                            password,
                            anyStateProvider
                        )
                    }
                )
            }

            composable(PageRouteNames.SignUpPage) {
                val context = LocalContext.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val anyStateProvider = LocalAnyStateProvider.current

                LaunchedEffect(true) {
                    systemUseCase.prepareSignUpState()
                }
                SignUpPage(
                    loginState = systemUseCase.loginSignUpState.value,
                    onBackClick = navController::navigateUp,
                    onSelectUserAvatarClick = { requestKey ->
                        showContentSelectionDialog(
                            context,
                            anyStateProvider,
                            navController,
                            PageRouteNames.SignUpPage,
                            requestKey,
                        )
                    },
                    onConfirmClick = {
                        systemUseCase.signUp(
                            context
                        )
                    }
                )
            }

            composable(PageRouteNames.AppToolsPage) {
                val context = LocalContext.current
                AppToolsPage(
                    onBackClick = navController::navigateUp,
                    onToolClick = { type ->
                        when (type) {
                            TOOL_TYPE_AppSets_Compose_plugin -> {
                                navController.navigate(xcj.app.compose_share.ui.purple_module.ComposeEventHandler.ROUTE_COMPOSE_DYNAMIC)
                            }

                            TOOL_TYPE_AppSets_Share -> {
                                navigateToAppSetsShareActivity(context, null)
                            }

                            TOOL_TYPE_AppSets_Launcher -> {
                                navigateToAppSetsLauncherActivity(context)
                            }

                            TOOL_TYPE_AppSets_Proxy -> {
                                navigateToAppSetsVpnActivity(context)
                            }

                            else -> {
                                navigateWithBundle(
                                    navController,
                                    PageRouteNames.AppToolsDetailsPage,
                                    bundleCreator = {
                                        bundleOf().apply {
                                            putString(TOOL_TYPE, type)
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }

            composable(PageRouteNames.AppToolsDetailsPage) {
                val type = it.arguments?.getString(TOOL_TYPE)
                val quickStepContents =
                    it.arguments?.getParcelableArrayList<QuickStepContent>(Constants.QUICK_STEP_CONTENT)
                AppToolsDetailsPage(
                    type = type,
                    quickStepContents = quickStepContents,
                    onBackClick = navController::navigateUp
                )
            }

            composable(PageRouteNames.MediaPage) {
                /* MusicMediaPage(
                     onSnapShotStateClick = { any, payload ->

                     }
                 )*/
            }

            composable(PageRouteNames.SearchPage) {
                val context = LocalContext.current
                val searchUseCase = LocalUseCaseOfSearch.current
                val anyStateProvider = LocalAnyStateProvider.current
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    SearchPage(
                        onBackClick = navController::navigateUp,
                        onInputContent = { inputContent ->
                            searchUseCase.updateKeywords(inputContent)
                        },
                        onBioClick = { bio ->
                            onBioClick(context, navController, bio)
                        },
                        onPictureClick = { url, urls ->
                            restrictedContentConfirmCallback = {
                                showPictureViewDialog(
                                    anyStateProvider,
                                    context,
                                    url.mediaFileUrl,
                                    urls.map { fileUrl -> fileUrl.mediaFileUrl })
                            }
                            if (url.isRestrictedContent) {
                                isShowRestrictedContentDialog = true
                            } else {
                                restrictedContentConfirmCallback?.invoke()
                            }
                        },
                        onScreenVideoPlayClick = { mediaFileUrl ->
                            restrictedContentConfirmCallback = {
                                navigateToVideoPlaybackActivity(context, mediaFileUrl)
                            }
                            if (mediaFileUrl.isRestrictedContent) {
                                isShowRestrictedContentDialog = true
                            } else {
                                restrictedContentConfirmCallback?.invoke()
                            }
                        }
                    )
                }
            }

            composable(PageRouteNames.GroupInfoPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
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
            }

            composable(PageRouteNames.CreateGroupPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val systemUseCase = LocalUseCaseOfSystem.current
                    val anyStateProvider = LocalAnyStateProvider.current
                    CreateGroupPage(
                        createGroupState = systemUseCase.createGroupState.value,
                        onBackClick = navController::navigateUp,
                        onConfirmAction = {
                            systemUseCase.createGroup(context)
                        },
                        onSelectGroupIconClick = { requestKey ->
                            showContentSelectionDialog(
                                context,
                                anyStateProvider,
                                navController,
                                PageRouteNames.CreateGroupPage,
                                requestKey,
                            )
                        }
                    )
                }
            }

            composable(PageRouteNames.SettingsPage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    SettingsPage(
                        onBackClick = navController::navigateUp,
                        onAboutClick = {
                            navController.navigate(PageRouteNames.AboutPage)
                        },
                        onPrivacyAndPermissionClick = {
                            navController.navigate(PageRouteNames.PrivacyPage)
                        }
                    )
                }
            }

            composable(PageRouteNames.AboutPage) {
                val systemUseCase = LocalUseCaseOfSystem.current
                AboutPage(
                    updateHistory = systemUseCase.updateHistory,
                    onBackClick = navController::navigateUp,
                    onHistoryExpandStateChanged = {
                        systemUseCase.getUpdateHistory()
                    },
                    onDispose = {
                        systemUseCase.cleanUpdateHistory()
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
                    onRequest = {
                        PlatformUseCase.navigateToExternalSystemAppDetails(context)
                    }
                )
            }

            composable(PageRouteNames.UserProfilePage) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
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
                        onPictureClick = { url, urls ->
                            restrictedContentConfirmCallback = {
                                showPictureViewDialog(
                                    anyStateProvider,
                                    context,
                                    url.mediaFileUrl,
                                    urls.map { fileUrl -> fileUrl.mediaFileUrl })
                            }
                            if (url.isRestrictedContent) {
                                isShowRestrictedContentDialog = true
                            } else {
                                restrictedContentConfirmCallback?.invoke()
                            }
                        },
                        onScreenVideoPlayClick = { mediaFileUrl ->
                            restrictedContentConfirmCallback = {
                                navigateToVideoPlaybackActivity(context, mediaFileUrl)
                            }
                            if (mediaFileUrl.isRestrictedContent) {
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

fun publishComposeNaviHostFormedEvent(navController: NavHostController, builder: NavGraphBuilder) {
    PurpleLogger.current.d(TAG, "publishComposeNaviHostFormedEvent")
    val naviHostParams = NaviHostParams(navController, builder)
    val composeEvent = ComposeEvent(ComposeEvent.EVENT_NAVI_HOST_FORMED, naviHostParams)
    LocalPurpleEventPublisher.current.publishEvent(composeEvent)
}

/**
 * Interceptor handler when NaviHost's Destination onChange
 * eg: for ImmerseContentState
 */
@Composable
fun NaviHostBackHandlerInterceptor(navController: NavHostController) {
    val anyStateProvider = LocalAnyStateProvider.current
    val immerseContentState = anyStateProvider.immerseContentState()
    PredictiveBackHandler(immerseContentState.isShow) {
        PurpleLogger.current.d(
            TAG,
            "NaviHostBackHandlerInterceptor onBack, make immerseContentState.showState to false"
        )
        if (immerseContentState is ProgressedComposeContainerState) {
            it.collect(immerseContentState)
            immerseContentState.hide()
        }
    }
}

@Composable
fun DesignNaviHost(
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            scaleIn(initialScale = 0.93f, animationSpec = tween()) + fadeIn(tween())
        },
        exitTransition = {
            scaleOut(targetScale = 1.07f, animationSpec = tween()) + fadeOut(tween())
        },
        builder = builder,
        contentAlignment = Alignment.TopCenter
    )
    NaviHostBackHandlerInterceptor(navController)
}

fun onBioClick(
    context: Context,
    navController: NavHostController,
    bio: Bio
) {
    if (context !is DesignComponentActivity) {
        return
    }
    val baseViewModel = context.requireViewModel()
    if (baseViewModel == null) {
        return
    }
    if (baseViewModel !is BaseIMViewModel) {
        return
    }
    when (bio) {
        is UserInfo,
        is MessageFromInfo -> {
            navigateToUserInfoPage(context, navController, bio.id)
        }

        is GroupInfo -> {
            baseViewModel.groupInfoUseCase.updateGroupInfo(context, bio)
            navController.navigate(PageRouteNames.GroupInfoPage)
        }

        is Application -> {
            if (baseViewModel is MainViewModel) {
                navigateToAppDetailsPage(navController, baseViewModel.appsUseCase, bio)
            }
        }

        is ScreenInfo -> {
            baseViewModel.screensUseCase.updateCurrentViewScreen(
                navController.currentDestination?.route,
                bio
            )
            navController.navigate(PageRouteNames.ScreenDetailsPage)
        }
    }
}

@SuppressLint("RestrictedApi")
fun navigateToAppDetailsPage(
    navController: NavHostController,
    appsUseCase: AppsUseCase,
    application: Application
) {
    navigateWithBundle(
        navController,
        PageRouteNames.AppDetailsPage,
        bundleCreator = {
            bundleOf().apply {
                val overrideApplication =
                    (appsUseCase.findApplicationById(application) ?: application)
                putParcelable(Constants.APP_INFO, overrideApplication)
            }
        }
    )
}

@SuppressLint("RestrictedApi")
fun navigateWithBundle(
    navController: NavHostController,
    route: String,
    bundleCreator: () -> Bundle
) {
    val destinationId = navController.findDestination(route)?.id
    if (destinationId == null) {
        PurpleLogger.current.d(
            TAG,
            "navigateWithBundle, route:$route, destinationId is null, return"
        )
        return
    }
    val navDirections: NavDirections = object : NavDirections {
        override val actionId: Int = destinationId
        override val arguments: Bundle = bundleCreator()
    }
    navController.navigate(navDirections)
}

@SuppressLint("RestrictedApi")
fun navigateToCreateAppPage(
    navController: NavController,
    application: Application?,
    platform: PlatForm?,
    version: VersionInfo?,
    createStep: String
): Boolean {
    val destinationId = navController.findDestination(PageRouteNames.CreateAppPage)?.id
    if (destinationId == null) {
        PurpleLogger.current.d(TAG, "navigateToCreateAppPage, destinationId is null, return")
        return false
    }
    val navDirections: NavDirections = object : NavDirections {
        override val actionId: Int = destinationId
        override val arguments: Bundle = bundleOf(
            Constants.CREATE_STEP to createStep
        ).apply {
            if (application != null) {
                putParcelable(Constants.APP_INFO, application)
            }
            if (platform != null) {
                putString(Constants.PLATFORM_ID, platform.id)
            }
            if (version != null) {
                putString(Constants.VERSION_ID, version.id)
            }
        }
    }
    navController.navigate(navDirections)
    return true
}

fun navigateToUserInfoPage(
    context: Context,
    navController: NavController,
    uid: String?
) {
    if (uid.isNullOrEmpty()) {
        return
    }
    if (context !is DesignComponentActivity) {
        return
    }
    val baseViewModel = context.requireViewModel()
    if (baseViewModel == null) {
        return
    }
    if (baseViewModel !is BaseIMViewModel) {
        return
    }
    baseViewModel.userInfoUseCase.updateCurrentUserInfoByUid(uid)
    navController.navigate(PageRouteNames.UserProfilePage)
}

fun <D> showPictureViewDialog(
    anyStateProvider: AnyStateProvider,
    context: Context,
    data: D,
    dataList: List<D>
) {
    val immerseContentState = anyStateProvider.immerseContentState()
    immerseContentState.show {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val scope = rememberCoroutineScope()
            val pagerState = rememberPagerState(
                initialPage = dataList.indexOf(data),
                pageCount = { dataList.size }
            )
            VerticalPager(
                modifier = Modifier,
                state = pagerState
            ) { pageIndex ->
                AnyImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(rememberZoomableState()),
                    any = dataList[pageIndex],
                    contentScale = ContentScale.FillWidth
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .systemBarsPadding()
            ) {
                Card(
                    shape = CircleShape
                ) {
                    Text(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        text = "${pagerState.currentPage + 1}/${dataList.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(
                        bottom = WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding() + 12.dp
                    )
            ) {
                val rotation = remember {
                    mutableFloatStateOf(270f)
                }
                val rotationAnimate = animateFloatAsState(
                    targetValue = rotation.floatValue,
                    label = "degree_animate",
                    animationSpec = tween(450)
                )

                LaunchedEffect(true) {
                    scope.launch {
                        rotation.floatValue = 0f
                    }
                }
                Icon(
                    modifier = Modifier
                        .shadow(20.dp, CircleShape)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                        .clip(CircleShape)
                        .clickable {
                            immerseContentState.hide()
                        }
                        .rotate(rotationAnimate.value)
                        .padding(12.dp),
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_close_24),
                    contentDescription = "close"
                )
                Spacer(
                    modifier = Modifier.width(
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                )
            }

        }
    }
}

fun showContentSelectionDialog(
    context: Context,
    anyStateProvider: AnyStateProvider,
    navController: NavController,
    contextName: String,
    requestKey: String,
    requestTypes: List<String>? = null
) {
    val platformPermissionsUsageOfFile =
        PlatformUseCase.providePlatformPermissions(context).firstOrNull {
            it.name == context.getString(xcj.app.appsets.R.string.file)
        }
    if (platformPermissionsUsageOfFile == null) {
        return
    }
    if (!platformPermissionsUsageOfFile.granted) {
        navController.navigate(PageRouteNames.PrivacyPage)
        return
    }
    val bottomSheetContainerState = anyStateProvider.bottomSheetState()
    bottomSheetContainerState.show {
        ContentSelectDialog(
            contextName = contextName,
            requestKey = requestKey,
            requestTypes = requestTypes,
            onContentSelect = {
                bottomSheetContainerState.hide()
                LocalMessager.post(ModuleConstant.MESSAGE_KEY_ON_CONTENT_SELECT_RESULT, it)
            }
        )
    }
}

fun showWebBrowserDialog(context: Context, anyStateProvider: AnyStateProvider, data: Any) {
    if (data !is String) {
        return
    }
    val bottomSheetState = anyStateProvider.bottomSheetState()
    bottomSheetState.show {
        WebComponent(null, url = data)
    }
}

fun navigateToVideoPlaybackActivity(context: Context, playbackContent: Any) {
    val commonURIJson = when (playbackContent) {
        is ScreenMediaFileUrl -> {
            CommonURIJson(
                playbackContent.mediaFileUrl,
                playbackContent.mediaDescription,
                playbackContent.mediaFileUrl
            )
        }

        is VideoMessage -> {
            CommonURIJson(
                playbackContent.id,
                playbackContent.metadata.description,
                playbackContent.metadata.url ?: ""
            )
        }

        is MediaStoreDataUri -> {
            CommonURIJson(
                playbackContent.uri?.path ?: "",
                playbackContent.displayName ?: "",
                playbackContent.uri.toString(),
                true
            )
        }

        else -> return
    }
    val intent = Intent(context, MediaPlaybackActivity::class.java).apply {
        val videoJson = Gson().toJson(commonURIJson)
        putExtra(MediaPlaybackActivity.KEY_VIDEO_JSON_DATA, videoJson)
    }
    context.startActivity(intent)
}

fun navigateToCameraActivity(context: Context, navController: NavController) {
    if (context !is Activity) {
        return
    }
    val platformPermissionsUsageOfFile =
        PlatformUseCase.providePlatformPermissions(context).firstOrNull {
            it.name == context.getString(xcj.app.appsets.R.string.camera)
        }
    if (platformPermissionsUsageOfFile == null) {
        return
    }
    if (!platformPermissionsUsageOfFile.granted) {
        navController.navigate(PageRouteNames.PrivacyPage)
        return
    }
    val intent = Intent(context, CameraComposeActivity::class.java)
    context.startActivityForResult(intent, CameraComposeActivity.REQUEST_CODE)
}

fun navigateToAppSetsLauncherActivity(context: Context) {
    val intent = Intent()
    val componentName =
        ComponentName(
            "xcj.app.container",
            "xcj.app.launcher.ui.compose.standard_home.StandardWindowHome"
        )
    intent.setComponent(componentName)
    runCatching {
        context.startActivity(intent)
        if (context is Activity) {
            context.overridePendingTransition(
                android.R.anim.fade_in, android.R.anim.fade_out
            )
        }
    }.onFailure {
        PurpleLogger.current.d(TAG, "navigateToAppSetsLauncherActivity, failed!, e:${it.message}")
    }
}

fun navigateToAppSetsVpnActivity(context: Context) {
    val intent = Intent()
    val componentName =
        ComponentName(
            "xcj.app.container",
            "xcj.app.proxy.ui.compose.vpn.AppSetsVpnActivity"
        )
    intent.setComponent(componentName)
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        PurpleLogger.current.d(TAG, "navigateToAppSetsVpnActivity, failed!, e:${it.message}")
    }
}

fun navigateToAppSetsShareActivity(context: Context, intentN: Intent?) {
    val intent = Intent()
    val componentName =
        ComponentName(
            "xcj.app.container",
            "xcj.app.share.ui.compose.AppSetsShareActivity"
        )
    intent.setComponent(componentName)
    intentN?.let {
        intent.type = it.type
        intent.action = it.action
        intent.putExtras(it)
    }
    runCatching {
        context.startActivity(intent)
    }.onFailure {
        PurpleLogger.current.d(TAG, "navigateToAppSetsVpnActivity, failed!, e:${it.message}")
    }
}