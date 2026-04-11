package xcj.app.appsets.ui.compose.custom_component.preview_tooling

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import xcj.app.appsets.ui.compose.LocalNavControllers
import xcj.app.appsets.ui.compose.LocalQuickStepContentHandlerRegistry
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
import xcj.app.appsets.ui.compose.main.KEY_MAIN_NAVI_CONTROLLER
import xcj.app.appsets.ui.compose.quickstep.QuickStepContentHandlerRegistry
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.compose_share.components.LocalUseCaseOfComposeDynamic
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider

@Composable
fun DesignPreviewCompositionLocalProvider(content: @Composable () -> Unit) {
    val viewModel = remember {
        MainViewModel()
    }
    val navController = rememberNavController()
    val quickStepContentHandlerRegistry = remember {
        QuickStepContentHandlerRegistry()
    }
    CompositionLocalProvider(
        LocalUseCaseOfNavigation provides viewModel.navigationUseCase,
        LocalUseCaseOfComposeDynamic provides viewModel.composeDynamicUseCase,
        LocalUseCaseOfSystem provides viewModel.systemUseCase,
        LocalUseCaseOfScreenPost provides viewModel.screenPostUseCase,
        LocalUseCaseOfSearch provides viewModel.searchUseCase,
        LocalUseCaseOfQRCode provides viewModel.qrCodeUseCase,
        LocalUseCaseOfAppCreation provides viewModel.appCreationUseCase,
        LocalUseCaseOfGroupInfo provides viewModel.groupInfoUseCase,
        LocalUseCaseOfScreen provides viewModel.screensUseCase,
        LocalUseCaseOfMediaRemoteExo provides viewModel.mediaRemoteExoUseCase,
        LocalUseCaseOfMediaAudioRecorder provides viewModel.mediaAudioRecorderUseCase,
        LocalUseCaseOfConversation provides viewModel.conversationUseCase,
        LocalUseCaseOfApps provides viewModel.appsUseCase,
        LocalUseCaseOfUserInfo provides viewModel.userInfoUseCase,
        LocalUseCaseOfNowSpaceContent provides viewModel.nowSpaceContentUseCase,
        LocalNavControllers provides mapOf(KEY_MAIN_NAVI_CONTROLLER to navController),
        LocalVisibilityComposeStateProvider provides viewModel,
        LocalQuickStepContentHandlerRegistry provides quickStepContentHandlerRegistry,
        content = content,
    )
}