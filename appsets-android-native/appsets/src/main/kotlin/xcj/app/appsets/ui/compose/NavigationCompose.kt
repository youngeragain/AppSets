package xcj.app.appsets.ui.compose

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.os.bundleOf
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import coil.load
import com.google.gson.Gson
import com.stfalcon.imageviewer.StfalconImageViewer
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.constants.Constants
import xcj.app.appsets.im.CommonURLJson
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.im.Session
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.apps.AppDetailsPage
import xcj.app.appsets.ui.compose.apps.AppsCenterPage
import xcj.app.appsets.ui.compose.apps.CreateAppPage
import xcj.app.appsets.ui.compose.conversation.ConversationDetailsMorePage
import xcj.app.appsets.ui.compose.conversation.ConversationDetailsPage
import xcj.app.appsets.ui.compose.conversation.ConversationOverviewPage
import xcj.app.appsets.ui.compose.group.CreateGroupPage
import xcj.app.appsets.ui.compose.group.GroupInfoPage
import xcj.app.appsets.ui.compose.outside.AddScreenPostPage
import xcj.app.appsets.ui.compose.outside.OutSidePage
import xcj.app.appsets.ui.compose.outside.ScreenDetailsPage
import xcj.app.appsets.ui.compose.outside.ScreenEditPage
import xcj.app.appsets.ui.compose.search.SearchPage
import xcj.app.appsets.ui.compose.settings.AboutPage
import xcj.app.appsets.ui.compose.settings.SettingsPage
import xcj.app.appsets.ui.compose.start.AppDefinition
import xcj.app.appsets.ui.compose.start.ItemDefinition
import xcj.app.appsets.ui.compose.start.SpotLightState
import xcj.app.appsets.ui.compose.start.StartAllAppsPage
import xcj.app.appsets.ui.compose.start.StartPage
import xcj.app.appsets.ui.compose.user.UserProfilePage
import xcj.app.appsets.usecase.models.Application
import xcj.app.compose_share.compose.dynamic.DynamicPage

@SuppressLint("ResourceAsColor")
@UnstableApi
@Composable
fun NavigationCompose(navController: NavHostController) {
    NavHost(navController = navController, PageRouteNameProvider.StartPage) {
        composable(PageRouteNameProvider.StartPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                StartPage(
                    onSearchBarClick = {
                        navController.navigate(PageRouteNameProvider.SearchPage)
                    },
                    onSettingsUserNameClick = {
                        navigateToUserInfoByUid(
                            navController,
                            viewModel,
                            LocalAccountManager._userInfo.value.uid
                        )
                    },
                    onSettingsClick = {
                        navController.navigate(PageRouteNameProvider.SettingsPage)
                    },
                    onSettingsLoginClick = {
                        viewModel.toLoginPageOrSinOut(context)
                    },
                    onSearchBarAddButtonClick = {

                    },
                    onShareClick = {
                        viewModel.toShareActivity(context)
                    },
                    onSnapShotStateClick = { any, payload ->
                        when (any) {
                            is SpotLightState.AudioPlayer -> {}
                            is SpotLightState.PinnedApps -> {
                                if (payload == "more_action") {
                                    navController.navigate(PageRouteNameProvider.StartAllAppsPage)
                                } else if (payload is AppDefinition) {
                                    any.onClick?.invoke(any, context, payload)
                                }
                            }

                            is SpotLightState.RecommendedItems -> {
                                if (payload == "more_action") {
                                    if (viewModel.appSetsUseCase.newVersionState.value?.forceUpdate == true) {
                                        "需要更新应用".toast()
                                    } else {
                                        navController.navigate(PageRouteNameProvider.OutSidePage)
                                    }
                                } else if (payload is ItemDefinition) {
                                    any.onClick?.invoke(any, context, payload)
                                }
                            }

                            is SpotLightState.QuestionOfTheDay -> {
                                //any.onClick?.invoke(any, context, null)
                                StfalconImageViewer.Builder<String>(
                                    context,
                                    listOf(any.img.toString())
                                ) { view, imageUrl ->
                                    view.load(imageUrl)
                                }.withHiddenStatusBar(false).show()
                            }

                            is SpotLightState.WordOfTheDay -> {
                            }

                            is SpotLightState.TodayInHistory -> {
                                // any.wordOfTheDay?.onClick?.invoke(any.wordOfTheDay, context, null)
                                StfalconImageViewer.Builder<String>(
                                    context,
                                    listOf(any.bgImg.toString())
                                ) { view, imageUrl ->
                                    view.load(imageUrl)
                                }.withHiddenStatusBar(false).show()
                            }

                            is SpotLightState.PopularSearches -> {}
                            is SpotLightState.HotWordsWrapper -> {}
                        }
                    })
            }
        }
        composable(PageRouteNameProvider.StartAllAppsPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                StartAllAppsPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    onBackClick = {
                        navController.navigateUp()
                    }
                )
            }
        }
        composable(PageRouteNameProvider.SearchPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                SearchPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    searchStringState = viewModel.searchUseCase?.searchStringState,
                    onBackClick = {
                        viewModel.searchUseCase?.updateCurrentSearchStr(null)
                        navController.navigateUp()
                    },
                    onKeywordsInput = { inputContent ->
                        viewModel.searchUseCase!!.updateCurrentSearchStr(inputContent)
                    },

                    onAppClick = { application ->
                        navController.findDestination(PageRouteNameProvider.AppDetailsPage)?.id?.let { id ->
                            val navDirections: NavDirections = object : NavDirections {
                                override val actionId: Int = id
                                override val arguments: Bundle =
                                    bundleOf(Constants.BK_APP_INFO to application)
                            }
                            navController.navigate(navDirections)
                        }
                    },
                    onUserInfoClick = { userInfo ->
                        navigateToUserInfoByUid(navController, viewModel, userInfo?.uid)
                    },
                    onGroupInfoClick = { groupInfo ->
                        viewModel.groupInfoUseCase?.updateGroupInfo(groupInfo)
                        navController.navigate(PageRouteNameProvider.GroupInfoPage)
                    },
                    onScreenContentClick = { userScreenInfo ->
                        viewModel.screensUseCase?.updateCurrentViewScreen(userScreenInfo)
                        navController.navigate(PageRouteNameProvider.ScreenDetailsPage)
                    },
                    onPictureClick = { pictureUrl, allPictureUrls ->
                        showPictureViewDialog(context, pictureUrl, allPictureUrls)
                    },
                    onScreenVideoPlayClick = { mediaFileUrl ->
                        navigateToVideoPlaybackActivity(context, mediaFileUrl)
                    }
                )
            }
        }
        composable(PageRouteNameProvider.OutSidePage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                OutSidePage(
                    onAddButtonClick = {
                        navController.navigate(PageRouteNameProvider.AddScreenPostPage)
                    },
                    onRefreshButtonClick = {
                        viewModel.screensUseCase?.loadIndexScreens(true)
                    },
                    onMediaFallClick = {
                        viewModel.toMediaFallActivity(context)
                    },
                    onScreenAvatarClick = { userScreenInfo ->
                        navigateToUserInfoByUid(navController, viewModel, userScreenInfo.uid)
                    },
                    onScreenContentClick = { userScreenInfo ->
                        if (!userScreenInfo.uid.isNullOrEmpty()) {
                            viewModel.screensUseCase?.updateCurrentViewScreen(userScreenInfo)
                            navController.navigate(PageRouteNameProvider.ScreenDetailsPage)
                        }
                    },
                    onPictureClick = { pictureUrl, allPictureUrls ->
                        showPictureViewDialog(context, pictureUrl, allPictureUrls)
                    },
                    onScreenVideoPlayClick = { mediaFileUrl ->
                        navigateToVideoPlaybackActivity(context, mediaFileUrl)
                    }
                )
            }
        }
        composable(PageRouteNameProvider.ScreenDetailsPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                //TODO 添加上一级页面信息
                val fromDestinationRoute = it.arguments?.getString("from_destination_route") ?: ""
                ScreenDetailsPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    screenInfoState = viewModel.screensUseCase?.currentViewScreenState!!,
                    screenViewCountState = viewModel.screensUseCase?.currentViewScreenViewCount!!,
                    screenLikeCountState = viewModel.screensUseCase?.currentViewScreenLikedCount!!,
                    screenIsCollectByUserState = viewModel.screensUseCase?.currentViewScreenIsCollectByUser!!,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onEditClick = {
                        navController.navigate(PageRouteNameProvider.ScreenEditPage)
                    },
                    onCollectClick = { category ->
                        viewModel.screensUseCase?.userClickCollectScreen(category)
                    },
                    onLikesClick = {
                        viewModel.screensUseCase?.userClickLikeScreen()
                    },
                    onUserAvatarClick = { userInfo ->
                        navigateToUserInfoByUid(navController, viewModel, userInfo?.uid)
                    },
                    onPictureClick = { pictureUrl, allPictureUrls ->
                        showPictureViewDialog(context, pictureUrl, allPictureUrls)
                    },
                    onScreenVideoPlayClick = { mediaFileUrl ->
                        navigateToVideoPlaybackActivity(context, mediaFileUrl)
                    })
            }
        }
        composable(PageRouteNameProvider.ConversationOverviewPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                ConversationOverviewPage(
                    onAddFriendClick = {
                        navController.navigate(PageRouteNameProvider.SearchPage)
                    },
                    onJoinGroupClick = {
                        navController.navigate(PageRouteNameProvider.SearchPage)
                    },
                    onCreateGroupClick = {
                        navController.navigate(PageRouteNameProvider.CreateGroup)
                    },
                    onConversionSessionClick = { session, shouldNavigateNewPage ->
                        viewModel.conversationUseCase?.updateCurrentSessionBySession(session)
                        if (shouldNavigateNewPage)
                            navController.navigate(PageRouteNameProvider.ConversationDetailsPage)
                    },
                    onSystemImMessageClick = { session, imMessage ->
                        Log.e("ConversationOverviewPage", "system im message click")
                    },
                    onAvatarClick = { type, id ->
                        when (type) {
                            "user" -> {
                                navigateToUserInfoByUid(navController, viewModel, id)
                            }
                            "group" -> {
                                viewModel.groupInfoUseCase?.updateGroupInfoByGroupId(id)
                                navController.navigate(PageRouteNameProvider.GroupInfoPage)
                            }
                        }
                    },
                    onUserRequestClick = { result, session: Session, imMessage: ImMessage.System ->
                        viewModel.systemUseCase?.handleUserRequestResult(
                            context,
                            result,
                            session,
                            imMessage
                        )

                    }
                )
            }
        }
        composable(PageRouteNameProvider.UserProfilePage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                UserProfilePage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    userInfoState = viewModel.userInfoUseCase.currentUserInfoState,
                    userApplicationsState = viewModel.userInfoUseCase.applicationsForThisUserState,
                    userFollowersState = viewModel.userInfoUseCase.followerUserListState,
                    userFollowedState = viewModel.userInfoUseCase.followedUserListState,
                    myFollowedThisUserState = viewModel.userInfoUseCase.myFollowedState,
                    userScreensState = viewModel.screensUseCase!!.userScreensContainer!!.screensState,
                    onAddFriendClick = { userInfo ->
                        viewModel.systemUseCase?.requestAddFriend(
                            userInfo.uid,
                            "你好，我想和你交个朋友",
                            "nothing"
                        )
                    },
                    onFollowStateClick = { userInfo ->
                        viewModel.systemUseCase?.flipFollowToUserState(userInfo) {
                            viewModel.userInfoUseCase.updateUserFollowState()
                        }
                    },

                    onTalkToUserClick = { userInfo ->
                        viewModel.conversationUseCase?.updateCurrentSessionByUserInfo(userInfo)
                        navController.navigate(PageRouteNameProvider.ConversationDetailsPage)
                    },
                    onPictureClick = { pictureUrl, allPictureUrls ->
                        showPictureViewDialog(context, pictureUrl, allPictureUrls)
                    },
                    onScreenContentClick = { userScreenInfo ->
                        viewModel.screensUseCase?.updateCurrentViewScreen(userScreenInfo)
                        navController.navigate(PageRouteNameProvider.ScreenDetailsPage)
                    },
                    onScreenVideoPlayClick = { mediaFileUrl ->
                        navigateToVideoPlaybackActivity(context, mediaFileUrl)
                    },

                    onLoadMoreScreens = {
                        viewModel.screensUseCase!!.loadMore()
                    },
                    onAppClick = { application ->
                        navController.findDestination(PageRouteNameProvider.AppDetailsPage)?.id?.let { id ->
                            val navDirections: NavDirections = object : NavDirections {
                                override val actionId: Int = id
                                override val arguments: Bundle =
                                    bundleOf(Constants.BK_APP_INFO to application)
                            }
                            navController.navigate(navDirections)
                        }
                    },
                    onUserInfoClick = { userInfo ->
                        //TODO bug
                        /*viewModel.userInfoUseCase.updateUserInfo(userInfo)
                        viewModel.screensUseCase?.loadScreensByUid(userInfo!!.uid)
                        navController.navigate(PageRouteNameProvider.UserProfilePage)*/
                    }
                )
            }
        }
        composable(PageRouteNameProvider.GroupInfoPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                GroupInfoPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onUserAvatarClick = { userInfo ->
                        navigateToUserInfoByUid(navController, viewModel, userInfo?.uid)
                    },
                    onTalkToGroupClick = { groupInfo ->
                        viewModel.conversationUseCase?.updateCurrentSessionByGroupInfo(groupInfo)
                        navController.navigate(PageRouteNameProvider.ConversationDetailsPage)
                    },
                    onJoinGroupRequestClick = { groupInfo ->
                        viewModel.systemUseCase?.requestJoinGroup(
                            groupInfo.groupId,
                            "这个群组看起来很有趣的样子呢，我可以加入吗?",
                            "nothing"
                        )
                    }
                )
            }
        }
        composable(PageRouteNameProvider.AppSetsCenterPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                AppsCenterPage(
                    headerApplication = viewModel.appSetsUseCase.headerApplicationState.value,
                    applications = viewModel.appSetsUseCase.indexApplications,
                    onRequestLoadApplication = {
                        viewModel.appSetsUseCase.loadIndexApps()
                    },
                    onAppClick = { application ->
                        navController.findDestination(PageRouteNameProvider.AppDetailsPage)?.id?.let { id ->
                            val navDirections: NavDirections = object : NavDirections {
                                override val actionId: Int = id
                                override val arguments: Bundle =
                                    bundleOf(Constants.BK_APP_INFO to application)
                            }
                            navController.navigate(navDirections)
                        }
                    },
                    onSearchBarClick = {
                        navController.navigate(PageRouteNameProvider.SearchPage)
                    },
                    onSearchBarAddButtonClick = {
                        navController.navigate(PageRouteNameProvider.CreateAppPage)
                    },
                    onSettingsUserNameClick = {
                        navigateToUserInfoByUid(
                            navController,
                            viewModel,
                            LocalAccountManager._userInfo.value.uid
                        )
                    },
                    onShareClick = {
                        viewModel.toShareActivity(context)
                    },
                    onSettingsClick = {
                        navController.navigate(PageRouteNameProvider.SettingsPage)
                    },
                    onSettingsLoginClick = {
                        viewModel.toLoginPageOrSinOut(context)
                    }
                )
            }
        }
        composable(PageRouteNameProvider.CreateAppPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                val application =
                    it.arguments?.getParcelable(Constants.BK_APP_INFO) as? Application
                val platform = it.arguments?.getString("platform_id")?.let { platformId ->
                    application?.platforms?.firstOrNull { platform -> platform.id == platformId }
                }
                val versionInfo = it.arguments?.getString("version_id")?.let { versionId ->
                    platform?.versionInfos?.firstOrNull { versionInfo -> versionInfo.id == versionId }
                }

                //标志创建的是整个application还是platform或者version_info或者screenshot_info或者download_info
                val createStep =
                    it.arguments?.getString("create_step", "application") ?: "application"
                CreateAppPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    createStep = createStep,
                    application = application,
                    platform = platform,
                    versionInfo = versionInfo,
                    applicationForCreate = viewModel.createApplicationUseCase!!.getApplication(),
                    createApplicationState = viewModel.createApplicationUseCase!!.createApplicationState,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onChoosePictureClick = { useAge, uriHolderState ->
                        viewModel.createApplicationUseCase?.setCurrentUseAgeAndUriHolderState(
                            useAge,
                            uriHolderState
                        )
                        viewModel.showSelectContentDialog(
                            context,
                            PageRouteNameProvider.CreateAppPage,
                            useAge
                        )
                    },
                    onConfirmClick = {
                        if (createStep != "application") {
                            "当前无法修改".toast()
                            return@CreateAppPage
                        }
                        viewModel.createApplicationUseCase?.finishCreateApp(context)
                    },
                    onDispose = {
                        viewModel.createApplicationUseCase!!.clear()
                    }
                )
            }
        }
        composable(PageRouteNameProvider.SettingsPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                SettingsPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onAboutClick = {
                        navController.navigate(PageRouteNameProvider.AboutPage)
                    },
                    onAddInClick = {
                        navController.navigate(PageRouteNameProvider.DynamicPage)
                    }
                )
            }
        }
        composable(PageRouteNameProvider.ConversationDetailsPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                ConversationDetailsPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onAvatarClick = { imMessage ->
                        navigateToUserInfoByUid(navController, viewModel, imMessage.msgFromInfo.uid)
                    },
                    onMoreClick = { imObj ->
                        if (imObj == null)
                            return@ConversationDetailsPage
                        navController.navigate(PageRouteNameProvider.ConversationDetailsMorePage)
                    }
                )
            }
        }
        composable(PageRouteNameProvider.AddScreenPostPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                AddScreenPostPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    onBackAction = { shouldRefresh ->
                        if (shouldRefresh)
                            viewModel.screensUseCase?.loadIndexScreens(true)
                        navController.navigateUp()
                    },
                    onConfirmClick = {
                        viewModel.screenPostUseCase?.post(context)
                    },
                    onAutoGenerateClick = {
                        viewModel.screenPostUseCase?.autoGenerateContent(context)
                    },
                    onAddMediaContent = { type ->
                        viewModel.showSelectContentDialog(
                            context,
                            PageRouteNameProvider.AddScreenPostPage,
                            type
                        )
                    },

                    onRemoveMediaContent = { type, scalableItemState ->
                        viewModel.screenPostUseCase?.onRemoveMediaContent(type, scalableItemState)
                    },
                    onVideoPlayClick = { mediaUriWrapper ->
                        navigateToVideoPlaybackActivity(
                            context,
                            ScreenMediaFileUrl(
                                mediaUriWrapper.uri.path ?: "",
                                null, mediaUriWrapper.name, "application/video", 0
                            )
                        )
                    }
                )
            }
        }
        composable(PageRouteNameProvider.AppDetailsPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                val application =
                    it.arguments?.getParcelable(Constants.BK_APP_INFO) as? Application
                AppDetailsPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    application = application,
                    onBackAction = {
                        navController.navigateUp()
                    },
                    onShowApplicationCreatorClick = { uid ->
                        navigateToUserInfoByUid(navController, viewModel, uid)
                    },
                    onAddPlatformInfoClick = { platform ->
                        navController.findDestination(PageRouteNameProvider.CreateAppPage)?.id?.let { id ->
                            val navDirections: NavDirections = object : NavDirections {
                                override val actionId: Int = id
                                override val arguments: Bundle =
                                    bundleOf(
                                        Constants.BK_APP_INFO to application,
                                        "platform_id" to platform?.id,
                                        "create_step" to "platform"
                                    ).apply {
                                        platform?.id?.let { platformId ->
                                            putString("platform_id", platformId)
                                        }
                                    }
                            }
                            navController.navigate(navDirections)
                        }
                    },
                    onAddVersionInfoClick = { platform ->
                        navController.findDestination(PageRouteNameProvider.CreateAppPage)?.id?.let { id ->
                            val navDirections: NavDirections = object : NavDirections {
                                override val actionId: Int = id
                                override val arguments: Bundle =
                                    bundleOf(
                                        Constants.BK_APP_INFO to application,
                                        "platform_id" to platform.id,
                                        "create_step" to "version"
                                    )
                            }
                            navController.navigate(navDirections)
                        }
                    },
                    onAddScreenshotInfoClick = { platform, version ->
                        navController.findDestination(PageRouteNameProvider.CreateAppPage)?.id?.let { id ->
                            val navDirections: NavDirections = object : NavDirections {
                                override val actionId: Int = id
                                override val arguments: Bundle =
                                    bundleOf(
                                        Constants.BK_APP_INFO to application,
                                        "platform_id" to platform.id,
                                        "version_id" to version.id,
                                        "create_step" to "screenshot"
                                    )
                            }
                            navController.navigate(navDirections)
                        }
                    },
                    onAddDownloadInfoClick = { platform, version ->
                        navController.findDestination(PageRouteNameProvider.CreateAppPage)?.id?.let { id ->
                            val navDirections: NavDirections = object : NavDirections {
                                override val actionId: Int = id
                                override val arguments: Bundle =
                                    bundleOf(
                                        Constants.BK_APP_INFO to application,
                                        "platform_id" to platform.id,
                                        "version_id" to version.id,
                                        "create_step" to "download"
                                    )
                            }
                            navController.navigate(navDirections)
                        }
                    }
                )
            }
        }
        composable(PageRouteNameProvider.CreateGroup) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                CreateGroupPage(
                    groupIconState = viewModel.systemUseCase?.groupIconState!!,
                    onBackAction = {
                        viewModel.systemUseCase?.clean()
                        navController.navigateUp()
                    },
                    onConfirmAction = { groupName, groupMembersCount, isPublic, groupIntroduction ->
                        viewModel.systemUseCase?.createGroup(
                            context, groupName, groupMembersCount,
                            isPublic, groupIntroduction
                        )
                    },
                    onSelectGroupIconClick = {
                        viewModel.showSelectContentDialog(
                            context,
                            PageRouteNameProvider.CreateGroup
                        )
                    }
                )
            }
        }
        composable(PageRouteNameProvider.ConversationDetailsMorePage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                ConversationDetailsMorePage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    imObj = viewModel.conversationUseCase?.currentSession?.imObj,
                    onBackClick = {
                        navController.navigateUp()
                    },
                    onRequestAddFriend = {},
                    onRequestDeleteFriend = {},
                    onShowUserInfoClick = { uid ->
                        navigateToUserInfoByUid(navController, viewModel, uid)
                    },
                    onRequestJoinGroup = {},
                    onRequestLeaveGroup = {},
                    onRequestDeleteGroup = {},
                    onShowGroupInfoClick = { groupId ->
                        viewModel.groupInfoUseCase?.updateGroupInfoByGroupId(groupId)
                        navController.navigate(PageRouteNameProvider.GroupInfoPage)
                    }
                )
            }
        }
        composable(PageRouteNameProvider.ScreenEditPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LoginInterceptorCompose(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                navBackStackEntry = it,
                onBackAction = {
                    navController.navigateUp()
                },
                onLoginClick = {
                    viewModel.toLoginPageOrSinOut(context)
                }
            ) {
                ScreenEditPage(
                    tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                    screenInfoState = viewModel.screensUseCase?.currentViewScreenState!!,
                    onBackAction = {
                        navController.navigateUp()
                    },
                    onPublicStateChanged = { newIsPublic ->
                        viewModel.screensUseCase?.changeScreenPublicState(newIsPublic)
                    }
                )
            }
        }
        composable(PageRouteNameProvider.AboutPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            AboutPage(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                updateHistory = viewModel.appSetsUseCase.updateHistory,
                onBackAction = {
                    navController.navigateUp()
                },
                onHistoryExpandStateChanged = {
                    viewModel.appSetsUseCase.getUpdateHistory()
                },
                onDispose = {
                    viewModel.appSetsUseCase.cleanUpdateHistory()
                }
            )
        }
        composable(PageRouteNameProvider.DynamicPage) {
            val context = LocalContext.current
            val viewModel: MainViewModel = viewModel(context as AppCompatActivity)
            LaunchedEffect(key1 = true, block = {
                viewModel.composeDynamicUseCase?.doLoad()
            })
            DynamicPage(
                tabVisibilityState = viewModel.bottomMenuUseCase.tabVisibilityState,
                onBackAction = {
                    navController.navigateUp()
                },
                onAddClick = {
                    viewModel.composeDynamicUseCase?.onAddClick(context)
                },
                onDeleteClick = {
                    viewModel.composeDynamicUseCase?.onDeleteClick(it)
                },
                onDispose = {
                    viewModel.composeDynamicUseCase?.onParentComposeDispose()
                },
                composeMethods = viewModel.composeDynamicUseCase?.composeMethodsState,
            )
        }
    }
}

fun navigateToUserInfoByUid(navController: NavController, viewModel: MainViewModel, uid: String?) {
    if (uid.isNullOrEmpty())
        return
    viewModel.userInfoUseCase.updateUserInfoByUid(uid)
    viewModel.screensUseCase?.loadScreensByUid(uid)
    navController.navigate(PageRouteNameProvider.UserProfilePage)
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun navigateToVideoPlaybackActivity(context: Context, mediaFileUrl: ScreenMediaFileUrl) {
    val intent = Intent(context, ExoPlayerActivity::class.java).apply {
        val videoJson = Gson().toJson(
            CommonURLJson.VideoURLJson(
                mediaFileUrl.mediaFileUrl,
                mediaFileUrl.mediaDescription
            )
        )
        putExtra("video_json", videoJson)
    }
    context.startActivity(intent)
}

fun showPictureViewDialog(
    context: Context,
    pictureUrl: ScreenMediaFileUrl,
    allPictureUrls: List<ScreenMediaFileUrl>
) {
    StfalconImageViewer.Builder(
        context,
        allPictureUrls
    ) { view, imageUrl ->
        view.load(imageUrl.mediaFileUrl)
    }.withHiddenStatusBar(false)
        .withStartPosition(allPictureUrls.indexOf(pictureUrl))
        .show()
}



