@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.outside

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.PageRouteNames

private const val TAG = "ScreensList"

@Composable
fun ScreensList(
    modifier: Modifier,
    currentDestinationRoute: String,
    screens: List<ScreenInfo>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    pictureInteractionFlowCollector: (Interaction, ScreenMediaFileUrl) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
) {
    val configuration = LocalConfiguration.current
    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        PortraitScreenList(
            modifier = modifier,
            scrollableState = scrollableState,
            currentDestinationRoute = currentDestinationRoute,
            screens = screens,
            onBioClick = onBioClick,
            pictureInteractionFlowCollector = pictureInteractionFlowCollector,
            onScreenMediaClick = onScreenMediaClick,
            headerContent = headerContent
        )
    } else {
        LandscapeScreenList(
            modifier = modifier,
            scrollableState = scrollableState,
            currentDestinationRoute = currentDestinationRoute,
            screens = screens,
            onBioClick = onBioClick,
            pictureInteractionFlowCollector = pictureInteractionFlowCollector,
            onScreenMediaClick = onScreenMediaClick,
            headerContent = headerContent
        )
    }
}

@Composable
private fun LandscapeScreenList(
    modifier: Modifier,
    currentDestinationRoute: String,
    screens: List<ScreenInfo>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    pictureInteractionFlowCollector: (Interaction, ScreenMediaFileUrl) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(3),
        contentPadding = PaddingValues(horizontal = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalItemSpacing = 6.dp,
        state = scrollableState as LazyStaggeredGridState
    )
    {
        if (currentDestinationRoute == PageRouteNames.OutSidePage) {
            items(3) {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            }
        }
        item {
            headerContent?.invoke()
        }
        itemsIndexed(screens, { index, screenInfo -> screenInfo.bioId }) { _, screenInfo ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.extraLarge
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onBioClick(screenInfo)
                    }
                    .padding(12.dp)
            ) {
                Screen(
                    currentPageRoute = currentDestinationRoute,
                    screenInfo = screenInfo,
                    onBioClick = onBioClick,
                    pictureInteractionFlowCollector = pictureInteractionFlowCollector,
                    onScreenMediaClick = onScreenMediaClick
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(82.dp))
        }
    }
}

@Composable
private fun PortraitScreenList(
    modifier: Modifier,
    currentDestinationRoute: String,
    screens: List<ScreenInfo>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    pictureInteractionFlowCollector: (Interaction, ScreenMediaFileUrl) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        state = scrollableState as LazyListState
    ) {
        if (currentDestinationRoute != PageRouteNames.UserProfilePage) {
            item {
                Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))
            }
        }
        if (headerContent != null) {
            item {
                headerContent()
            }
        }
        itemsIndexed(
            items = screens,
            key = { index, screenInfo ->
                screenInfo.bioId
            }
        ) { _, screenInfo ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        MaterialTheme.shapes.extraLarge
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        MaterialTheme.shapes.extraLarge
                    )
                    .clip(MaterialTheme.shapes.extraLarge)
                    .clickable {
                        onBioClick(screenInfo)
                    }
                    .padding(12.dp)
            ) {
                Screen(
                    currentPageRoute = currentDestinationRoute,
                    screenInfo = screenInfo,
                    onBioClick = onBioClick,
                    pictureInteractionFlowCollector = pictureInteractionFlowCollector,
                    onScreenMediaClick = onScreenMediaClick
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(82.dp))
        }
    }
}