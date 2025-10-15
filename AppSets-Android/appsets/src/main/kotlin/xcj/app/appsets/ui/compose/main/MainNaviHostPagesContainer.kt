@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.os.BundleCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.google.gson.Gson
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch
import me.saket.telephoto.zoomable.rememberZoomableState
import me.saket.telephoto.zoomable.zoomable
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.IMMessageDesignType
import xcj.app.appsets.im.InputSelector
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.HTMLMessage
import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.im.message.ImageMessage
import xcj.app.appsets.im.message.MusicMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.message.VideoMessage
import xcj.app.appsets.im.message.VoiceMessage
import xcj.app.appsets.im.message.requireUri
import xcj.app.appsets.im.model.CommonURIJson
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.server.model.AppPlatform
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.DownloadInfo
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.VersionInfo
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.ui.base.BaseIMViewModel
import xcj.app.appsets.ui.compose.LocalUseCaseOfAppCreation
import xcj.app.appsets.ui.compose.LocalUseCaseOfApps
import xcj.app.appsets.ui.compose.LocalUseCaseOfConversation
import xcj.app.appsets.ui.compose.LocalUseCaseOfGroupInfo
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaAudioRecorder
import xcj.app.appsets.ui.compose.LocalUseCaseOfMediaRemoteExo
import xcj.app.appsets.ui.compose.LocalUseCaseOfNavigation
import xcj.app.appsets.ui.compose.LocalUseCaseOfNowSpaceContent
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
import xcj.app.appsets.ui.compose.apps.DownloadBottomSheetContent
import xcj.app.appsets.ui.compose.apps.tools.AppTool
import xcj.app.appsets.ui.compose.apps.tools.ToolContentTransformPage
import xcj.app.appsets.ui.compose.apps.tools.ToolFileCreatePage
import xcj.app.appsets.ui.compose.apps.tools.ToolFileManagerPage
import xcj.app.appsets.ui.compose.apps.tools.ToolGraphicPage
import xcj.app.appsets.ui.compose.apps.tools.ToolIntentCallerPage
import xcj.app.appsets.ui.compose.apps.tools.ToolStartPage
import xcj.app.appsets.ui.compose.apps.tools.ToolWeatherPage
import xcj.app.appsets.ui.compose.camera.DesignCameraActivity
import xcj.app.appsets.ui.compose.compose_extensions.composableIf
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionPromptSheetContent
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionRequest
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionTypes
import xcj.app.appsets.ui.compose.content_selection.defaultAllSelectionTypeParam
import xcj.app.appsets.ui.compose.content_selection.defaultImageSelectionTypeParam
import xcj.app.appsets.ui.compose.conversation.ConversationDetailsMoreInfoSheetContent
import xcj.app.appsets.ui.compose.conversation.ConversationDetailsPage
import xcj.app.appsets.ui.compose.conversation.ConversationOverviewPage
import xcj.app.appsets.ui.compose.conversation.IMBubbleActivity
import xcj.app.appsets.ui.compose.conversation.ai_model.AIGCMarketPage
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
import xcj.app.appsets.ui.compose.outside.RestrictedContentHandleState
import xcj.app.appsets.ui.compose.outside.ScreenDetailsPage
import xcj.app.appsets.ui.compose.outside.ScreenEditPage
import xcj.app.appsets.ui.compose.outside.rememberRestrictedContentHandleState
import xcj.app.appsets.ui.compose.privacy.PrivacyPage
import xcj.app.appsets.ui.compose.quickstep.QuickStepContent
import xcj.app.appsets.ui.compose.search.SearchPage
import xcj.app.appsets.ui.compose.settings.AboutPage
import xcj.app.appsets.ui.compose.settings.SettingsPage
import xcj.app.appsets.ui.compose.user.UserProfilePage
import xcj.app.appsets.ui.compose.web.WebSheetContent
import xcj.app.appsets.ui.model.ApplicationForCreate
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.usecase.AppsUseCase
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.MediaRemoteExoUseCase
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.appsets.util.BundleDefaults
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.model.MediaStoreDataUri
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.components.ProgressiveVisibilityComposeState
import xcj.app.compose_share.components.VisibilityComposeStateProvider
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.immerseContentState
import xcj.app.io.components.LocalFileIO
import xcj.app.starter.android.ktx.startWithHttpSchema
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage
import xcj.app.starter.android.usecase.PlatformUseCase
import xcj.app.starter.android.util.LocalMessenger
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.ComposeEvent
import xcj.app.starter.test.LocalPurpleEventPublisher
import xcj.app.starter.test.NaviHostParams
import java.util.UUID

private const val TAG = "MainNaviHostPagesContainer"

@Composable
fun MainNaviHostPagesContainer(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startPageRoute: String,
    hazeState: HazeState,
    hostContextName: String = MainActivity.TAG
) {

    val restrictedContentHandleState = rememberRestrictedContentHandleState()

    Box(modifier = modifier) {
        DesignNaviHost(
            modifier = Modifier.hazeSource(hazeState),
            navController = navController,
            startDestination = startPageRoute,
        ) {

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.AppsCenterPage
            ) {
                val context = LocalContext.current
                val appsUseCase = LocalUseCaseOfApps.current
                val conversationUseCase = LocalUseCaseOfConversation.current
                val appCenterPageState by appsUseCase.appCenterPageState
                val coroutineScope = rememberCoroutineScope()
                AppsCenterPage(
                    appCenterPageState = appCenterPageState,
                    onBioClick = { bio ->
                        coroutineScope.launch {
                            onBioClick(context, navController, bio)
                        }
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

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG || hostContextName == IMBubbleActivity.TAG
                },
                route = PageRouteNames.AppDetailsPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {

                    val application =
                        BundleCompat.getParcelable(
                            it.arguments ?: BundleDefaults.empty,
                            Constants.APP_INFO,
                            Application::class.java
                        )
                    val context = LocalContext.current
                    val conversationUseCase = LocalUseCaseOfConversation.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val coroutineScope = rememberCoroutineScope()
                    AppDetailsPage(
                        application = application,
                        onBackClick = navController::navigateUp,
                        onGetApplicationClick = { application, appPlatform ->
                            val bottomSheetState = visibilityComposeStateProvider.bottomSheetState()
                            bottomSheetState.show {
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
                            coroutineScope.launch {
                                navigateToUserInfoPage(
                                    context,
                                    navController,
                                    application.createUid
                                )
                            }
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
                                visibilityComposeStateProvider,
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
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.CreateAppPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val application = BundleCompat.getParcelable(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.APP_INFO,
                        Application::class.java
                    )

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
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val coroutineScope = rememberCoroutineScope()
                    val createApplicationPageState by appCreationUseCase.createApplicationPageState

                    LaunchedEffect(key1 = true, block = {
                        appCreationUseCase.inflateApplication(application)
                    })

                    CreateAppPage(
                        createStep = createStep,
                        platform = platform,
                        versionInfo = versionInfo,
                        createApplicationPageState = createApplicationPageState,
                        onBackClick = navController::navigateUp,
                        onApplicationForCreateFiledChanged = appCreationUseCase::onApplicationForCreateFiledChanged,
                        onChoosePictureClick = { any, filedName, uriHolder ->
                            val requestKey = "CREATE_APP_PICTURE_REQUEST"
                            appCreationUseCase.setChooseContentTempValues(
                                any, filedName
                            )
                            coroutineScope.launch {
                                showContentSelectionDialog(
                                    context,
                                    visibilityComposeStateProvider,
                                    navController,
                                    PageRouteNames.CreateAppPage,
                                    requestKey,
                                    requestSelectionTypeParams = defaultImageSelectionTypeParam()
                                )
                            }
                        },
                        onConfirmClick = {
                            if (createStep != ApplicationForCreate.CREATE_STEP_APPLICATION) {
                                ContextCompat.getString(
                                    context,
                                    xcj.app.appsets.R.string.currently_cannot_be_modified
                                )
                                    .toast()
                                return@CreateAppPage
                            }
                            coroutineScope.launch {
                                appCreationUseCase.finishCreateApp(context)
                            }
                        }
                    )
                }
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.OutSidePage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val screensUseCase = LocalUseCaseOfScreen.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val coroutineScope = rememberCoroutineScope()
                    OutSidePage(
                        screens = screensUseCase.systemScreensContainer.screens,
                        onBioClick = { bio ->
                            coroutineScope.launch {
                                onBioClick(context, navController, bio)
                            }
                        },
                        onLoadMore = {
                            coroutineScope.launch {
                                screensUseCase.loadMore(null, false)
                            }
                        },
                        onScreenMediaClick = { url, urls ->
                            handleScreenMediaClick(
                                context,
                                restrictedContentHandleState,
                                visibilityComposeStateProvider,
                                url,
                                urls
                            )
                        }
                    )
                }
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ScreenDetailsPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val screenUseCase = LocalUseCaseOfScreen.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val coroutineScope = rememberCoroutineScope()
                    val screenInfoForCard by screenUseCase.currentScreenInfoForCard
                    ScreenDetailsPage(
                        screenInfoForCard = screenInfoForCard,
                        onBackClick = navController::navigateUp,
                        onBioClick = { bio ->
                            coroutineScope.launch {
                                onBioClick(context, navController, bio)
                            }
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
                                restrictedContentHandleState,
                                visibilityComposeStateProvider,
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
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.CreateScreenPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val screenUseCase = LocalUseCaseOfScreen.current
                    val screenPostUseCase = LocalUseCaseOfScreenPost.current
                    val systemUseCase = LocalUseCaseOfSystem.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val quickStepContents = BundleCompat.getParcelableArrayList(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.QUICK_STEP_CONTENT,
                        QuickStepContent::class.java
                    )
                    val coroutineScope = rememberCoroutineScope()
                    CreateScreenPage(
                        quickStepContents = quickStepContents,
                        onBackClick = { shouldRefresh ->
                            if (shouldRefresh) {
                                coroutineScope.launch {
                                    screenUseCase.loadOutSideScreens()
                                }
                            }
                            navController.navigateUp()
                        },
                        onConfirmClick = {
                            coroutineScope.launch {
                                screenPostUseCase.createScreen(context)
                            }
                        },
                        onIsPublicClick = screenPostUseCase::onIsPublicClick,
                        onGenerateClick = {
                            coroutineScope.launch {
                                screenPostUseCase.generateContent(context)
                            }
                        },
                        onInputContent = screenPostUseCase::onInputContent,
                        onInputTopics = screenPostUseCase::onInputTopics,
                        onInputPeoples = screenPostUseCase::onInputPeoples,
                        onAddMediaFallClick = screenPostUseCase::onAddMediaFallClick,

                        onAddMediaContentClick = { requestKey, requestType, requestTypeMaxCount ->
                            coroutineScope.launch {
                                showContentSelectionDialog(
                                    context,
                                    visibilityComposeStateProvider,
                                    navController,
                                    PageRouteNames.CreateScreenPage,
                                    requestKey,
                                    requestSelectionTypeParams = listOf(
                                        ContentSelectionRequest.SelectionTypeParam(
                                            selectionType = requestType,
                                            maxCount = { selectionType ->
                                                requestTypeMaxCount
                                            }
                                        )
                                    ),
                                    defaultSelectionType = requestType
                                )
                            }

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

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG || hostContextName == IMBubbleActivity.TAG
                },
                route = PageRouteNames.ScreenEditPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val screenUseCase = LocalUseCaseOfScreen.current
                    val coroutineScope = rememberCoroutineScope()
                    val screenInfoForCard by screenUseCase.currentScreenInfoForCard
                    ScreenEditPage(
                        screenInfo = screenInfoForCard.screenInfo,
                        onBackClick = navController::navigateUp,
                        onPublicStateChanged = { newIsPublic ->
                            coroutineScope.launch {
                                screenUseCase.changeScreenPublicState(newIsPublic)
                            }
                        }
                    )
                }
            }
            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ConversationAIGCMarketPage
            ) {
                AIGCMarketPage(
                    onBackClick = navController::navigateUp
                )
            }
            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ConversationOverviewPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val conversationUseCase = LocalUseCaseOfConversation.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val systemUseCase = LocalUseCaseOfSystem.current
                    val mediaAudioRecorderUseCase = LocalUseCaseOfMediaAudioRecorder.current
                    val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
                    val nowSpaceContentUseCase = LocalUseCaseOfNowSpaceContent.current
                    val currentSessionState by conversationUseCase.currentSessionState
                    val recorderState by mediaAudioRecorderUseCase.recorderState
                    val isShowActions by conversationUseCase.isShowActions
                    val coroutineScope = rememberCoroutineScope()
                    ConversationOverviewPage(
                        sessionState = currentSessionState,
                        isShowActions = isShowActions,
                        recorderState = recorderState,
                        onBioClick = { bio ->
                            coroutineScope.launch {
                                onBioClick(context, navController, bio)
                            }
                        },
                        onImMessageContentClick = { imMessage ->
                            handleImMessageContentClick(
                                context,
                                imMessage,
                                conversationUseCase,
                                mediaRemoteExoUseCase,
                                visibilityComposeStateProvider
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
                            coroutineScope.launch {
                                showContentSelectionDialog(
                                    context,
                                    visibilityComposeStateProvider,
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
                            }
                        },
                        onVoiceAction = {
                            coroutineScope.launch {
                                mediaAudioRecorderUseCase.startRecord(
                                    context,
                                    systemUseCase,
                                    nowSpaceContentUseCase
                                )
                            }
                        },
                        onVoiceStopClick = { showSend ->
                            mediaAudioRecorderUseCase.stopRecord("UI click")
                            if (!showSend) {
                                mediaAudioRecorderUseCase.cleanUp("user stop")
                                return@ConversationOverviewPage
                            }
                            mediaAudioRecorderUseCase.resetState()
                            val uriProvider = mediaAudioRecorderUseCase.getRecordFileUriProvider()
                            if (uriProvider == null) {
                                return@ConversationOverviewPage
                            }
                            conversationUseCase.sendMessage(
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
                            val bottomSheetState = visibilityComposeStateProvider.bottomSheetState()
                            bottomSheetState.show {
                                ConversationDetailsMoreInfoSheetContent(
                                    imObj = imObj,
                                    onBioClick = { bio ->
                                        coroutineScope.launch {
                                            onBioClick(context, navController, bio)
                                        }
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

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG || hostContextName == IMBubbleActivity.TAG
                },
                route = PageRouteNames.ConversationDetailsPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val conversationUseCase = LocalUseCaseOfConversation.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val mediaAudioRecorderUseCase = LocalUseCaseOfMediaAudioRecorder.current
                    val mediaRemoteExoUseCase = LocalUseCaseOfMediaRemoteExo.current
                    val systemUseCase = LocalUseCaseOfSystem.current
                    val nowSpaceContentUseCase = LocalUseCaseOfNowSpaceContent.current
                    val sessionState by conversationUseCase.currentSessionState
                    val recorderState by mediaAudioRecorderUseCase.recorderState
                    val coroutineScope = rememberCoroutineScope()
                    ConversationDetailsPage(
                        sessionState = sessionState,
                        recorderState = recorderState,
                        onBackClick = navController::navigateUp,
                        onBioClick = { bio ->
                            coroutineScope.launch {
                                onBioClick(context, navController, bio)
                            }
                        },
                        onImMessageContentClick = { imMessage ->
                            handleImMessageContentClick(
                                context,
                                imMessage,
                                conversationUseCase,
                                mediaRemoteExoUseCase,
                                visibilityComposeStateProvider
                            )
                        },
                        onInputMoreAction = { requestType ->
                            coroutineScope.launch {
                                showContentSelectionDialog(
                                    context,
                                    visibilityComposeStateProvider,
                                    navController,
                                    PageRouteNames.ConversationDetailsPage,
                                    requestType,
                                    requestSelectionTypeParams = defaultAllSelectionTypeParam { selectionType ->
                                        if (selectionType == ContentSelectionTypes.IMAGE) {
                                            100
                                        } else {
                                            1
                                        }
                                    }
                                )
                            }
                        },
                        onVoiceAction = {
                            coroutineScope.launch {
                                mediaAudioRecorderUseCase.startRecord(
                                    context,
                                    systemUseCase,
                                    nowSpaceContentUseCase
                                )
                            }
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
                            conversationUseCase.sendMessage(
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
                            val bottomSheetState = visibilityComposeStateProvider.bottomSheetState()
                            bottomSheetState.show {
                                ConversationDetailsMoreInfoSheetContent(
                                    imObj = imObj,
                                    onBioClick = { bio ->
                                        coroutineScope.launch {
                                            onBioClick(context, navController, bio)
                                        }
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

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.LoginPage
            ) {
                val context = LocalContext.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val qrCodeUseCase = LocalUseCaseOfQRCode.current
                val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                val navigationUseCase = LocalUseCaseOfNavigation.current
                val coroutineScope = rememberCoroutineScope()
                val loginSignUpPageState by systemUseCase.loginSignUpPageState
                val generatedQRCodeInfo by qrCodeUseCase.generatedQRCodeInfo

                LaunchedEffect(true) {
                    systemUseCase.prepareLoginState()
                }
                LoginPage(
                    loginSignUpPageState = loginSignUpPageState,
                    generatedQRCodeInfo = generatedQRCodeInfo,
                    onBackClick = navController::navigateUp,
                    onLoggingFinish = {
                        val lastNavDestination = navigationUseCase.lastRoute
                        if (lastNavDestination.isNullOrEmpty()) {
                            return@LoginPage
                        }
                        navController.navigateWithClearStack(lastNavDestination)
                    },
                    onSignUpButtonClick = {
                        navController.navigate(PageRouteNames.SignUpPage)
                    },
                    onQRCodeLoginButtonClick = {
                        coroutineScope.launch {
                            qrCodeUseCase.requestGenerateQRCode()
                        }
                    },
                    onScanQRCodeButtonClick = {
                        navigateToCameraActivity(context, navController)
                    },
                    onLoginConfirmButtonClick = { account, password ->
                        coroutineScope.launch {
                            systemUseCase.login(
                                context,
                                account,
                                password,
                                visibilityComposeStateProvider
                            )
                        }
                    }
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.SignUpPage
            ) {
                val context = LocalContext.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                val coroutineScope = rememberCoroutineScope()
                val loginSignUpPageState by systemUseCase.loginSignUpPageState
                LaunchedEffect(true) {
                    systemUseCase.prepareSignUpState()
                }
                SignUpPage(
                    loginState = loginSignUpPageState,
                    onBackClick = navController::navigateUp,
                    onSelectUserAvatarClick = { requestKey ->
                        coroutineScope.launch {
                            showContentSelectionDialog(
                                context,
                                visibilityComposeStateProvider,
                                navController,
                                PageRouteNames.SignUpPage,
                                requestKey,
                                requestSelectionTypeParams = defaultImageSelectionTypeParam()
                            )
                        }
                    },
                    onConfirmClick = {
                        coroutineScope.launch {
                            systemUseCase.signUp(context)
                        }
                    }
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ToolsStartPage
            ) {
                val context = LocalContext.current
                ToolStartPage(
                    onBackClick = navController::navigateUp,
                    onToolClick = { appTool ->
                        appTool.routeBuilder?.invoke(context, navController)
                    }
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ToolsDetailsPage + AppTool.TOOL_TYPE_AppSets_Transform
            ) {
                val quickStepContents =
                    BundleCompat.getParcelableArrayList(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.QUICK_STEP_CONTENT,
                        QuickStepContent::class.java
                    )

                ToolContentTransformPage(
                    quickStepContents = quickStepContents,
                    onBackClick = navController::navigateUp
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ToolsDetailsPage + AppTool.TOOL_TYPE_AppSets_Weather
            ) {
                val quickStepContents =
                    BundleCompat.getParcelableArrayList(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.QUICK_STEP_CONTENT,
                        QuickStepContent::class.java
                    )

                ToolWeatherPage(
                    quickStepContents = quickStepContents,
                    onBackClick = navController::navigateUp
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ToolsDetailsPage + AppTool.TOOL_TYPE_AppSets_Intent_Caller
            ) {
                val quickStepContents =
                    BundleCompat.getParcelableArrayList(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.QUICK_STEP_CONTENT,
                        QuickStepContent::class.java
                    )

                ToolIntentCallerPage(
                    quickStepContents = quickStepContents,
                    onBackClick = navController::navigateUp
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ToolsDetailsPage + AppTool.TOOL_TYPE_AppSets_File_Manager
            ) {
                val quickStepContents =
                    BundleCompat.getParcelableArrayList(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.QUICK_STEP_CONTENT,
                        QuickStepContent::class.java
                    )

                ToolFileManagerPage(
                    quickStepContents = quickStepContents,
                    onBackClick = navController::navigateUp,
                    onCreateFileClick = { abstractFile ->
                        navController.navigateWithBundle(PageRouteNames.ToolsDetailsPage + AppTool.TOOL_TYPE_AppSets_File_Creator) {
                            bundleOf().apply {
                                putParcelable(Constants.URI, abstractFile.asUri())
                            }
                        }
                    }
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ToolsDetailsPage + AppTool.TOOL_TYPE_AppSets_File_Creator
            ) {
                val quickStepContents =
                    BundleCompat.getParcelableArrayList(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.QUICK_STEP_CONTENT,
                        QuickStepContent::class.java
                    )
                val uri =
                    BundleCompat.getParcelable(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.URI,
                        Uri::class.java
                    )
                ToolFileCreatePage(
                    quickStepContents = quickStepContents,
                    uri = uri,
                    onBackClick = navController::navigateUp
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.ToolsDetailsPage + AppTool.TOOL_TYPE_AppSets_Graphic
            ) {
                val quickStepContents =
                    BundleCompat.getParcelableArrayList(
                        it.arguments ?: BundleDefaults.empty,
                        Constants.QUICK_STEP_CONTENT,
                        QuickStepContent::class.java
                    )

                ToolGraphicPage(
                    quickStepContents = quickStepContents,
                    onBackClick = navController::navigateUp
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.MediaPage
            ) {
                /* MusicMediaPage(
                     onSnapShotStateClick = { any, payload ->

                     }
                 )*/
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG || hostContextName == IMBubbleActivity.TAG
                },
                route = PageRouteNames.SearchPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val searchUseCase = LocalUseCaseOfSearch.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val coroutineScope = rememberCoroutineScope()
                    SearchPage(
                        onBioClick = { bio ->
                            coroutineScope.launch {
                                onBioClick(context, navController, bio)
                            }
                        },
                        onScreenMediaClick = { url, urls ->
                            handleScreenMediaClick(
                                context,
                                restrictedContentHandleState,
                                visibilityComposeStateProvider,
                                url,
                                urls
                            )
                        }
                    )
                }
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG || hostContextName == IMBubbleActivity.TAG
                },
                route = PageRouteNames.GroupInfoPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val groupInfoUseCase = LocalUseCaseOfGroupInfo.current
                    val conversationUseCase = LocalUseCaseOfConversation.current
                    val systemUseCase = LocalUseCaseOfSystem.current
                    val groupInfoState by groupInfoUseCase.groupInfoPageState
                    val coroutineScope = rememberCoroutineScope()
                    GroupInfoPage(
                        groupInfoPageState = groupInfoState,
                        onBackClick = navController::navigateUp,
                        onBioClick = { bio ->
                            coroutineScope.launch {
                                onBioClick(context, navController, bio)
                            }
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
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG || hostContextName == IMBubbleActivity.TAG
                },
                route = PageRouteNames.CreateGroupPage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val systemUseCase = LocalUseCaseOfSystem.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
                    val createGroupPageState by systemUseCase.createGroupPageState
                    val coroutineScope = rememberCoroutineScope()
                    CreateGroupPage(
                        createGroupPageState = createGroupPageState,
                        onBackClick = navController::navigateUp,
                        onConfirmAction = {
                            systemUseCase.createGroup(context)
                        },
                        onSelectGroupIconClick = { requestKey ->
                            coroutineScope.launch {
                                showContentSelectionDialog(
                                    context,
                                    visibilityComposeStateProvider,
                                    navController,
                                    PageRouteNames.CreateGroupPage,
                                    requestKey,
                                    requestSelectionTypeParams = defaultImageSelectionTypeParam()
                                )
                            }
                        }
                    )
                }
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.SettingsPage
            ) {
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

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG
                },
                route = PageRouteNames.AboutPage
            ) {
                val context = LocalContext.current
                val systemUseCase = LocalUseCaseOfSystem.current
                val coroutineScope = rememberCoroutineScope()

                /**
                 * 更新历史
                 */
                val updateHistory = remember {
                    mutableStateListOf<UpdateCheckResult>()
                }
                AboutPage(
                    updateHistory = updateHistory,
                    onBackClick = navController::navigateUp,
                    onHistoryExpandStateChanged = {
                        coroutineScope.launch {
                            systemUseCase.getUpdateHistory {
                                updateHistory.clear()
                                updateHistory.addAll(it)
                            }
                        }
                    },
                    onWebsiteClick = {
                        navigateToExternalWeb(context, AppSetsModuleSettings.WEBSITE_URL.toUri())
                    },
                    onDispose = {

                    }
                )
            }

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG || hostContextName == IMBubbleActivity.TAG
                },
                route = PageRouteNames.PrivacyPage
            ) {
                val context = LocalContext.current
                val lifecycleOwner = LocalLifecycleOwner.current
                val lifecycle = lifecycleOwner.lifecycle
                val lifecycleState by lifecycle.currentStateAsState()
                val privacy = remember {
                    SystemUseCase.providePrivacy(context)
                }
                var androidPermissionsUsageList by remember {
                    mutableStateOf(emptyList<PlatformPermissionsUsage>())
                }
                LaunchedEffect(lifecycleState) {
                    androidPermissionsUsageList =
                        PlatformPermissionsUsage.provideAll(context)
                }
                PrivacyPage(
                    privacy = privacy,
                    platformPermissionsUsageList = androidPermissionsUsageList,
                    onBackClick = navController::navigateUp,
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

            composableIf(
                test = {
                    hostContextName == MainActivity.TAG || hostContextName == IMBubbleActivity.TAG
                },
                route = PageRouteNames.UserProfilePage
            ) {
                LoginInterceptorPage(
                    navController = navController,
                    navBackStackEntry = it,
                    onBackClick = navController::navigateUp,
                ) {
                    val context = LocalContext.current
                    val userInfoUseCase = LocalUseCaseOfUserInfo.current
                    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
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
                        userProfilePageState = userProfilePageState,
                        userApplications = userApplications,
                        userFollowers = userFollowers,
                        userFollowed = userFollowed,
                        isLoginUserFollowedThisUser = isLoginUserFollowedThisUser,
                        userScreens = userScreens,
                        onBackClick = navController::navigateUp,
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
                            coroutineScope.launch {
                                onBioClick(context, navController, bio)
                            }
                        },
                        onScreenMediaClick = { url, urls ->
                            handleScreenMediaClick(
                                context,
                                restrictedContentHandleState,
                                visibilityComposeStateProvider,
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
                            coroutineScope.launch {
                                showContentSelectionDialog(
                                    context,
                                    visibilityComposeStateProvider,
                                    navController,
                                    PageRouteNames.UserProfilePage,
                                    requestKey,
                                    requestSelectionTypeParams = defaultImageSelectionTypeParam()
                                )
                            }
                        },
                        onModifyProfileConfirmClick = {
                            coroutineScope.launch {
                                userInfoUseCase.modifyUserInfo(context)
                            }
                        }
                    )
                }
            }
        }

        RestrictedContentDialog(
            restrictedContentHandleState = restrictedContentHandleState
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
    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
    val immerseContentState = visibilityComposeStateProvider.immerseContentState()
    PredictiveBackHandler(immerseContentState.isShow) {
        PurpleLogger.current.d(
            TAG,
            "NaviHostBackHandlerInterceptor onBack, make immerseContentState.showState to false"
        )
        if (immerseContentState is ProgressiveVisibilityComposeState) {
            it.collect(immerseContentState)
            immerseContentState.hide()
        }
    }
}

@Composable
fun DesignNaviHost(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String,
    builder: NavGraphBuilder.() -> Unit,
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = {
            scaleIn(
                initialScale = 1.07f,
                animationSpec = tween(350)
            ) + fadeIn(
                animationSpec = tween(350)
            )
        },
        exitTransition = {
            scaleOut(
                targetScale = 0.93f,
                animationSpec = tween(350)
            ) + fadeOut(
                animationSpec = tween(350)
            )
        },
        contentAlignment = Alignment.TopCenter,
        builder = {
            publishComposeNaviHostFormedEvent(navController, this)
            builder()
        },
    )
    NaviHostBackHandlerInterceptor(navController)
}

suspend fun onBioClick(
    context: Context,
    navController: NavHostController,
    bio: Bio,
) {
    if (context !is DesignComponentActivity) {
        return
    }
    val baseViewModel = context.requireViewModel<BaseIMViewModel>()
    if (baseViewModel == null) {
        return
    }
    when (bio) {
        is UserInfo,
        is MessageToInfo,
        is MessageFromInfo,
            -> {
            navigateToUserInfoPage(context, navController, bio.bioId)
        }

        is GroupInfo -> {
            baseViewModel.viewModelScope.launch {
                baseViewModel.groupInfoUseCase.updateGroupInfo(context, bio)
            }
            navController.navigate(PageRouteNames.GroupInfoPage)
        }

        is Application -> {
            if (baseViewModel is MainViewModel) {
                navigateToAppDetailsPage(navController, baseViewModel.appsUseCase, bio)
            }
        }

        is ScreenInfo -> {
            baseViewModel.viewModelScope.launch {
                baseViewModel.screensUseCase.updateCurrentViewScreen(
                    navController.currentDestination?.route,
                    bio
                )
            }
            navController.navigate(PageRouteNames.ScreenDetailsPage)
        }
    }
}

@SuppressLint("RestrictedApi")
private fun navigateToAppDetailsPage(
    navController: NavHostController,
    appsUseCase: AppsUseCase,
    application: Application,
) {
    navController.navigateWithBundle(
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
private fun navigateToCreateAppPage(
    navController: NavController,
    application: Application?,
    platform: AppPlatform?,
    version: VersionInfo?,
    createStep: String,
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

private suspend fun navigateToUserInfoPage(
    context: Context,
    navController: NavController,
    uid: String?,
) {
    if (uid.isNullOrEmpty()) {
        return
    }
    if (context !is DesignComponentActivity) {
        return
    }
    val baseViewModel = context.requireViewModel<BaseIMViewModel>()
    if (baseViewModel == null) {
        return
    }
    baseViewModel.userInfoUseCase.updateCurrentUserInfoByUid(uid)
    navController.navigate(PageRouteNames.UserProfilePage)
}

private fun showPictureViewDialog(
    visibilityComposeStateProvider: VisibilityComposeStateProvider,
    context: Context,
    data: Any,
    dataList: List<Any>,
) {
    val immerseContentState = visibilityComposeStateProvider.immerseContentState()
    immerseContentState.show {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val coroutineScope = rememberCoroutineScope()
            val rotation = remember {
                mutableFloatStateOf(90f)
            }
            val rotationState by animateFloatAsState(
                targetValue = rotation.floatValue,
                label = "degree_animate",
                animationSpec = tween()
            )
            LaunchedEffect(true) {
                coroutineScope.launch {
                    rotation.floatValue = 0f
                }
            }
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
                    model = dataList[pageIndex],
                    contentScale = ContentScale.FillWidth
                )
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(
                        bottom = WindowInsets.navigationBars
                            .asPaddingValues()
                            .calculateBottomPadding() + 12.dp
                    )
            ) {
                Spacer(
                    modifier = Modifier.width(
                        WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                )
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
                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    modifier = Modifier
                        .clickable {
                            immerseContentState.hide()
                        }
                        .rotate(rotationState)
                        .padding(12.dp),
                    painter = painterResource(id = xcj.app.compose_share.R.drawable.ic_round_close_24),
                    contentDescription = "close",
                    tint = MaterialTheme.colorScheme.onSurface
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

private fun showContentSelectionDialog(
    context: Context,
    visibilityComposeStateProvider: VisibilityComposeStateProvider,
    navController: NavController,
    contextName: String,
    requestKey: String,
    requestSelectionTypeParams: List<ContentSelectionRequest.SelectionTypeParam> = defaultAllSelectionTypeParam(),
    defaultSelectionType: String = requestSelectionTypeParams.first().selectionType,
) {
    val request = ContentSelectionRequest(
        context,
        contextName,
        requestKey,
        requestSelectionTypeParams,
        defaultSelectionType
    )
    val bottomSheetState = visibilityComposeStateProvider.bottomSheetState()
    bottomSheetState.show {
        ContentSelectionPromptSheetContent(
            request = request,
            onContentSelected = { contentSelectionResult ->
                bottomSheetState.hide()
                LocalMessenger.post(
                    ModuleConstant.MESSAGE_KEY_ON_CONTENT_SELECT_RESULT,
                    contentSelectionResult
                )
            },
            onDismiss = {
                bottomSheetState.hide()
            }
        )
    }
}

fun showWebBrowserDialog(
    context: Context,
    visibilityComposeStateProvider: VisibilityComposeStateProvider,
    data: Any,
) {
    if (data !is String) {
        return
    }
    val bottomSheetState = visibilityComposeStateProvider.bottomSheetState()
    bottomSheetState.show {
        WebSheetContent(null, url = data)
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
            val uriPair = playbackContent.requireUri() ?: return
            val uri = uriPair.first?.toString() ?: return
            CommonURIJson(
                playbackContent.id,
                playbackContent.metadata.description,
                uri
            )
        }

        is MediaStoreDataUri -> {
            CommonURIJson(
                playbackContent.id.toString(),
                playbackContent.displayName ?: "",
                playbackContent.provideUri().toString(),
                true
            )
        }

        is UriProvider -> {
            val bioId = UUID.randomUUID().toString()
            CommonURIJson(
                bioId,
                bioId,
                playbackContent.provideUri().toString(),
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
    val platformCameraPermission =
        PlatformPermissionsUsage.provideCamera(context)

    if (!platformCameraPermission.granted) {
        navController.navigate(PageRouteNames.PrivacyPage)
        return
    }
    val intent = Intent(context, DesignCameraActivity::class.java)
    context.startActivityForResult(intent, DesignCameraActivity.REQUEST_CODE)
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
                android.R.anim.fade_in,
                android.R.anim.fade_out
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

fun navigateToExternalWeb(context: Context, uri: Uri) {
    val downloadIntent = Intent(Intent.ACTION_VIEW, uri)
    runCatching {
        context.startActivity(downloadIntent)
    }.onFailure {
        PurpleLogger.current.d(TAG, "navigateToExternalWeb, failed!, e:${it.message}")
    }
}

private suspend fun handleApplicationDownload(
    context: Context,
    application: Application,
    downloadInfo: DownloadInfo,
) {
    var url = downloadInfo.url
    if (url.isNullOrEmpty()) {
        return
    }
    if (!url.startWithHttpSchema()) {
        val fileIO = LocalFileIO.current
        url = fileIO.generatePreSign(url, AppsUseCase.appsContentObjectUploadOptions)
    }
    val uri = url?.toUri()
    if (uri == null) {
        return
    }
    navigateToExternalWeb(context, uri)
}

private fun handleImMessageContentClick(
    context: Context,
    imMessage: IMMessage<*>,
    conversationUseCase: ConversationUseCase,
    mediaRemoteExoUseCase: MediaRemoteExoUseCase,
    visibilityComposeStateProvider: VisibilityComposeStateProvider,
) {
    when (imMessage) {
        is MusicMessage -> {
            val uri =
                imMessage.requireUri() ?: return
            val commonURIJson = CommonURIJson(
                imMessage.id,
                imMessage.metadata.description,
                uri.toString()
            )
            mediaRemoteExoUseCase.playOrPauseAudio(context, commonURIJson)
        }

        is VoiceMessage -> {
            val uri =
                imMessage.requireUri() ?: return
            val commonURIJson = CommonURIJson(
                imMessage.id,
                imMessage.metadata.description,
                uri.toString()
            )
            mediaRemoteExoUseCase.playOrPauseAudio(context, commonURIJson)
        }

        is VideoMessage -> {
            navigateToVideoPlaybackActivity(context, imMessage)
        }

        is ImageMessage -> {
            val currentUri = imMessage.requireUri()
                ?: return
            val uriList =
                conversationUseCase.findCurrentSessionMessagesByMessageType<ImageMessage>(
                    IMMessageDesignType.TYPE_IMAGE
                ).mapNotNull { imageMessage ->
                    imageMessage.requireUri()
                }
            showPictureViewDialog(
                visibilityComposeStateProvider,
                context,
                currentUri,
                uriList
            )
        }

        is HTMLMessage -> {
            showWebBrowserDialog(
                context,
                visibilityComposeStateProvider,
                imMessage.metadata.data
            )
        }
    }
}

private fun handleScreenMediaClick(
    context: Context,
    restrictedContentHandleState: RestrictedContentHandleState,
    visibilityComposeStateProvider: VisibilityComposeStateProvider,
    url: ScreenMediaFileUrl,
    urls: List<ScreenMediaFileUrl>,
) {
    val callback = {
        if (url.isVideoMedia) {
            navigateToVideoPlaybackActivity(context, url)
        } else {
            val currentUri = url.mediaFileUrl
            val uriList = urls.map { fileUrl -> fileUrl.mediaFileUrl }
            showPictureViewDialog(
                visibilityComposeStateProvider,
                context,
                currentUri,
                uriList
            )
        }

    }
    if (url.isRestrictedContent) {
        restrictedContentHandleState.setCallback(callback)
        restrictedContentHandleState.show()
    } else {
        callback.invoke()
    }
}