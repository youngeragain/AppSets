package xcj.app.appsets.ui.compose

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.runtime.staticCompositionLocalOf
import xcj.app.appsets.usecase.ActivityLifecycleUseCase
import xcj.app.appsets.usecase.AppCreationUseCase
import xcj.app.appsets.usecase.AppsUseCase
import xcj.app.appsets.usecase.ConversationUseCase
import xcj.app.appsets.usecase.GroupInfoUseCase
import xcj.app.appsets.usecase.MediaAudioRecorderUseCase
import xcj.app.appsets.usecase.MediaLocalExoUseCase
import xcj.app.appsets.usecase.MediaRemoteExoUseCase
import xcj.app.appsets.usecase.NavigationUseCase
import xcj.app.appsets.usecase.NowSpaceContentUseCase
import xcj.app.appsets.usecase.QRCodeUseCase
import xcj.app.appsets.usecase.ScreenPostUseCase
import xcj.app.appsets.usecase.ScreenUseCase
import xcj.app.appsets.usecase.SearchUseCase
import xcj.app.appsets.usecase.SystemUseCase
import xcj.app.appsets.usecase.UserInfoUseCase

val LocalBackPressedDispatcher =
    staticCompositionLocalOf<OnBackPressedDispatcher> { error("No Back Dispatcher provided") }

val LocalUseCaseOfActivityLifecycle =
    staticCompositionLocalOf<ActivityLifecycleUseCase> { error("No ActivityLifecycleUseCase provided") }

val LocalUseCaseOfNavigation =
    staticCompositionLocalOf<NavigationUseCase> { error("No NavigationUseCase provided") }

val LocalUseCaseOfSystem =
    staticCompositionLocalOf<SystemUseCase> { error("No SystemUseCase provided") }

val LocalUseCaseOfScreenPost =
    staticCompositionLocalOf<ScreenPostUseCase> { error("No ScreenPostUseCase provided") }

val LocalUseCaseOfSearch =
    staticCompositionLocalOf<SearchUseCase> { error("No SearchUseCase provided") }

val LocalUseCaseOfQRCode =
    staticCompositionLocalOf<QRCodeUseCase> { error("No QRCodeUseCase provided") }

val LocalUseCaseOfAppCreation =
    staticCompositionLocalOf<AppCreationUseCase> { error("No AppCreationUseCase provided") }

val LocalUseCaseOfGroupInfo =
    staticCompositionLocalOf<GroupInfoUseCase> { error("No GroupInfoUseCase provided") }

val LocalUseCaseOfScreen =
    staticCompositionLocalOf<ScreenUseCase> { error("No ScreenUseCase provided") }

val LocalUseCaseOfMediaLocalExo =
    staticCompositionLocalOf<MediaLocalExoUseCase> { error("No MediaLocalExoUseCase provided") }

val LocalUseCaseOfMediaRemoteExo =
    staticCompositionLocalOf<MediaRemoteExoUseCase> { error("No MediaRemoteExoUseCase provided") }

val LocalUseCaseOfMediaAudioRecorder =
    staticCompositionLocalOf<MediaAudioRecorderUseCase> { error("No MediaAudioRecorderUseCase provided") }

val LocalUseCaseOfConversation =
    staticCompositionLocalOf<ConversationUseCase> { error("No ConversationUseCase provided") }

val LocalUseCaseOfApps =
    staticCompositionLocalOf<AppsUseCase> { error("No AppsUseCase provided") }

val LocalUseCaseOfUserInfo =
    staticCompositionLocalOf<UserInfoUseCase> { error("No UserInfoUseCase provided") }

val LocalUseCaseOfNowSpaceContent =
    staticCompositionLocalOf<NowSpaceContentUseCase> { error("No NowSpaceContentUseCase provided") }

//提供所有需要登录拦截页面名
val LocalPageRouteNameNeedLoggedProvider = staticCompositionLocalOf {
    mutableListOf(
        PageRouteNames.ConversationOverviewPage,
        PageRouteNames.ConversationDetailsPage,
        PageRouteNames.UserProfilePage,
        PageRouteNames.CreateScreenPage,
        PageRouteNames.CreateAppPage,
    )
}