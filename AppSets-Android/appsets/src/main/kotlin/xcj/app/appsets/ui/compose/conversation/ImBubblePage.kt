package xcj.app.appsets.ui.compose.conversation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import dev.chrisbanes.haze.rememberHazeState
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
import xcj.app.appsets.ui.compose.main.ImmerseContentContainer
import xcj.app.appsets.ui.compose.main.MainNaviHostPagesContainer
import xcj.app.appsets.ui.viewmodel.IMBubbleViewModel
import xcj.app.compose_share.components.BottomSheetContainer
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider

@Composable
fun ImBubblePage() {
    val viewModel = viewModel<IMBubbleViewModel>()
    val navController = rememberNavController()
    val hazeState = rememberHazeState()
    CompositionLocalProvider(
        LocalUseCaseOfSystem provides viewModel.systemUseCase,
        LocalUseCaseOfNavigation provides viewModel.navigationUseCase,
        LocalUseCaseOfGroupInfo provides viewModel.groupInfoUseCase,
        LocalUseCaseOfScreen provides viewModel.screensUseCase,
        LocalUseCaseOfMediaRemoteExo provides viewModel.mediaRemoteExoUseCase,
        LocalUseCaseOfMediaAudioRecorder provides viewModel.mediaAudioRecorderUseCase,
        LocalUseCaseOfConversation provides viewModel.conversationUseCase,
        LocalUseCaseOfUserInfo provides viewModel.userInfoUseCase,
        LocalUseCaseOfNowSpaceContent provides viewModel.nowSpaceContentUseCase,
        LocalVisibilityComposeStateProvider provides viewModel,
    ) {
        Surface {
            Box(modifier = Modifier.fillMaxSize()) {
                MainNaviHostPagesContainer(
                    navController = navController,
                    startPageRoute = PageRouteNames.ConversationOverviewPage,
                    hazeState = hazeState,
                    hostContextName = IMBubbleActivity.TAG
                )

                ImmerseContentContainer()

                BottomSheetContainer()

            }
        }
    }
}
