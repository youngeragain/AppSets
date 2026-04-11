@file:OptIn(ExperimentalMaterial3Api::class)

package xcj.app.appsets.ui.compose.outside

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.ScreenInfo
import xcj.app.appsets.server.model.ScreenMediaFileUrl
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.LoadMoreHandler
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.compose_share.components.statusBarWithTopActionBarPaddingValues
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

private const val TAG = "ScreensList"

@Composable
fun ScreensList(
    modifier: Modifier,
    pageRouteName: String,
    screens: List<ScreenInfo>,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
    onLoadMore: () -> Unit
) {
    VerticalOverscrollBox {
        val configuration = LocalConfiguration.current

        if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            val scrollableState = rememberLazyListState()

            LoadMoreHandler(scrollableState = scrollableState) {
                onLoadMore()
            }

            PortraitScreenList(
                modifier = modifier,
                pageRouteName = pageRouteName,
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
                pageRouteName = pageRouteName,
                scrollableState = scrollableState,
                screens = screens,
                onBioClick = onBioClick,
                onScreenMediaClick = onScreenMediaClick
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Composable
private fun LandscapeScreenList(
    modifier: Modifier,
    pageRouteName: String,
    screens: List<ScreenInfo>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    val randomKeyPrefix = remember { Uuid.random().toString() }
    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Fixed(3),
        contentPadding = statusBarWithTopActionBarPaddingValues(
            bottom = 150.dp,
            containsTopBarHeight = pageRouteName == PageRouteNames.UserProfilePage
        ),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalItemSpacing = 6.dp,
        state = scrollableState as LazyStaggeredGridState
    )
    {
        itemsIndexed(
            items = screens,
            key = { index, screenInfo ->
                randomKeyPrefix + screenInfo.bioId
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
                    pageRouteName = pageRouteName,
                    screenInfo = screenInfo,
                    onBioClick = onBioClick,
                    onScreenMediaClick = onScreenMediaClick
                )
            }
        }
        item {
            Spacer(modifier = Modifier.height(150.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalUuidApi::class)
@Composable
private fun PortraitScreenList(
    modifier: Modifier,
    pageRouteName: String,
    screens: List<ScreenInfo>,
    scrollableState: ScrollableState,
    onBioClick: (Bio) -> Unit,
    onScreenMediaClick: (ScreenMediaFileUrl, List<ScreenMediaFileUrl>) -> Unit,
) {
    val randomKeyPrefix = remember { Uuid.random().toString() }
    LazyColumn(
        modifier = modifier,
        state = scrollableState as LazyListState,
        contentPadding = statusBarWithTopActionBarPaddingValues(
            bottom = 150.dp,
            containsTopBarHeight = pageRouteName == PageRouteNames.UserProfilePage
        ),
        verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
    )
    {
        itemsIndexed(
            items = screens,
            key = { index, screenInfo ->
                randomKeyPrefix + screenInfo.bioId
            }
        ) { index, screenInfo ->
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
                    pageRouteName = pageRouteName,
                    screenInfo = screenInfo,
                    onBioClick = onBioClick,
                    onScreenMediaClick = onScreenMediaClick
                )
            }
        }
    }
}