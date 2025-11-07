package xcj.app.appsets.ui.compose.outside

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.custom_component.ShowNavBar
import xcj.app.appsets.ui.compose.search.StatusBarAreaGradient

@Composable
fun OutSidePage(
    screens: List<ScreenInfo>,
    onBioClick: (Bio) -> Unit,
    onLoadMore: () -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    ShowNavBar()
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        ScreensList(
            modifier = Modifier,
            screens = screens,
            onBioClick = onBioClick,
            onScreenMediaClick = onScreenMediaClick,
            onLoadMore = onLoadMore
        )

        StatusBarAreaGradient(
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}