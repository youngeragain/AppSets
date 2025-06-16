package xcj.app.appsets.ui.compose.user

import android.content.res.Configuration
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.custom_component.LoadMoreHandler
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.outside.ScreensList
import xcj.app.appsets.ui.model.ScreenState

@Composable
fun UserScreens(
    screens: List<ScreenState>,
    onBioClick: (Bio) -> Unit,
    onLoadMore: () -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit
) {
    if (screens.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = String.format(stringResource(id = xcj.app.appsets.R.string.no_a), "Screen"),
                fontSize = 12.sp
            )
        }
    } else {
        val scrollableState =
            if (LocalConfiguration.current.orientation == Configuration.ORIENTATION_PORTRAIT) {
                rememberLazyListState()
            } else {
                rememberLazyStaggeredGridState()
            }
        LoadMoreHandler(scrollableState = scrollableState) {
            onLoadMore()
        }
        val interactionFlow: (Interaction, ScreenMediaFileUrl) -> Unit = remember {
            { _, _ -> }
        }

        ScreensList(
            modifier = Modifier,
            scrollableState = scrollableState,
            currentDestinationRoute = PageRouteNames.UserProfilePage,
            screens = screens,
            onBioClick = onBioClick,
            pictureInteractionFlowCollector = interactionFlow,
            onScreenMediaClick = onScreenMediaClick,
        )
    }
}