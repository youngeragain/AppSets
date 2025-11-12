package xcj.app.appsets.ui.compose.user

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.outside.ScreensList

@Composable
fun UserScreens(
    screens: List<ScreenInfo>,
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
        ScreensList(
            modifier = Modifier,
            pageRouteName = PageRouteNames.UserProfilePage,
            screens = screens,
            onBioClick = onBioClick,
            onScreenMediaClick = onScreenMediaClick,
            onLoadMore = onLoadMore
        )
    }
}