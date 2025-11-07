@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.outside

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.itemsIndexed
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
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
import xcj.app.appsets.ui.compose.custom_component.LoadMoreHandler

private const val TAG = "ScreensList"

@Composable
fun ScreensList(
    modifier: Modifier,
    screens: List<ScreenInfo>,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onLoadMore: () -> Unit
) {
    val configuration = LocalConfiguration.current

    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
        val scrollableState = rememberLazyListState()

        LoadMoreHandler(scrollableState = scrollableState) {
            onLoadMore()
        }

        PortraitScreenList(
            modifier = modifier,
            scrollableState = scrollableState,
            screens = screens,
            onBioClick = onBioClick,
            onScreenMediaClick = onScreenMediaClick
        )
    } else {
        val scrollableState = rememberLazyStaggeredGridState()

        LoadMoreHandler(scrollableState = scrollableState) {
            onLoadMore()
        }

        LandscapeScreenList(
            modifier = modifier,
            scrollableState = scrollableState,
            screens = screens,
            onBioClick = onBioClick,
            onScreenMediaClick = onScreenMediaClick
        )
    }
}

@Composable
private fun LandscapeScreenList(
    modifier: Modifier,
    screens: List<ScreenInfo>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    headerContent: (@Composable () -> Unit)? = null,
) {
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Fixed(3),
        contentPadding = PaddingValues(
            start = 12.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            end = 12.dp,
            bottom = 150.dp
        ),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalItemSpacing = 6.dp,
        state = scrollableState as LazyStaggeredGridState
    )
    {
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
                    screenInfo = screenInfo,
                    onBioClick = onBioClick,
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
    screens: List<ScreenInfo>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 12.dp,
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            end = 12.dp,
            bottom = 150.dp
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
        state = scrollableState as LazyListState
    ) {
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
                    screenInfo = screenInfo,
                    onBioClick = onBioClick,
                    onScreenMediaClick = onScreenMediaClick
                )
            }
        }
    }
}