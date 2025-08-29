package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.foundation.gestures.ScrollableState
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun LoadMoreHandler(
    scrollableState: ScrollableState,
    buffer: Int = 3,
    onLoaderMore: suspend () -> Unit
) {
    val shouldLoadMoreState by remember {
        derivedStateOf {
            var needRequest = false
            when (scrollableState) {
                is LazyStaggeredGridState -> {
                    val layoutInfo = scrollableState.layoutInfo
                    needRequest = ((layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: 0) + 1) > layoutInfo.totalItemsCount - buffer
                }

                is LazyListState -> {
                    val layoutInfo = scrollableState.layoutInfo
                    needRequest = ((layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: 0) + 1) > layoutInfo.totalItemsCount - buffer
                }

                is LazyGridState -> {
                    val layoutInfo = scrollableState.layoutInfo
                    needRequest = ((layoutInfo.visibleItemsInfo.lastOrNull()?.index
                        ?: 0) + 1) > layoutInfo.totalItemsCount - buffer
                }
            }
            needRequest
        }
    }
    LaunchedEffect(
        key1 = shouldLoadMoreState,
        block = {
            snapshotFlow {
                shouldLoadMoreState
            }.distinctUntilChanged()
                .collect {
                    if (it) {
                        onLoaderMore()
                    }
                }
        }
    )
}