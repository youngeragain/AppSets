@file:OptIn(ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.apps

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.ui.compose.LocalNavControllers
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.ShowNavBar
import xcj.app.appsets.ui.compose.custom_component.VerticalOverscrollBox
import xcj.app.appsets.ui.compose.main.KEY_MAIN_NAVI_CONTROLLER
import xcj.app.appsets.ui.compose.theme.extShapes
import xcj.app.appsets.ui.model.page_state.AppCenterPageUIState
import xcj.app.compose_share.components.statusBarWithTopActionBarPaddingValues
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "AppsCenterPage"

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppsCenterPage(
    appCenterPageUIState: AppCenterPageUIState,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit
) {
    ShowNavBar()
    val allApplications by rememberUpdatedState(appCenterPageUIState.apps.flatMap { it.applications })
    SimpleApplicationList(
        apps = allApplications,
        pageRouteName = PageRouteNames.AppsCenterPage,
        onBioClick = onBioClick,
        onApplicationLongPress = onApplicationLongPress
    )
}

@OptIn(
    ExperimentalLayoutApi::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeMaterialsApi::class
)
@Composable
fun SimpleApplicationList(
    modifier: Modifier = Modifier,
    pageRouteName: String,
    apps: List<Application>,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit
) {
    val hapticFeedback = LocalHapticFeedback.current
    val navHostController = LocalNavControllers.current[KEY_MAIN_NAVI_CONTROLLER]
    var isShowDestination by remember {
        mutableStateOf(false)
    }
    var destinationIconSize by remember {
        mutableFloatStateOf(1f)
    }
    var overscrollOffset by remember {
        mutableFloatStateOf(0f)
    }
    var isSearchPageRouted by remember {
        mutableStateOf(false)
    }
    LaunchedEffect(true) {
        isSearchPageRouted = false
    }

    LaunchedEffect(overscrollOffset) {
        PurpleLogger.current.d(TAG, "overscrollOffset:$overscrollOffset")
        isShowDestination = overscrollOffset > 0f
        if (overscrollOffset > 10f && overscrollOffset < 300f) {
            destinationIconSize = (1f + overscrollOffset / 300f)
        }
        if (overscrollOffset >= 800f && !isSearchPageRouted) {
            isSearchPageRouted = true
            navHostController?.navigate(PageRouteNames.SearchPage)
        }
    }
    VerticalOverscrollBox(
        modifier = modifier,
        onOverscrollOffset = {
            overscrollOffset = it
        }
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(90.dp),
            modifier = Modifier,
            state = rememberLazyGridState(),
            contentPadding = statusBarWithTopActionBarPaddingValues(
                bottom = 150.dp,
                containsTopBarHeight = pageRouteName == PageRouteNames.UserProfilePage
            )
        ) {
            itemsIndexed(items = apps) { index, application ->
                SingleApplicationComponent(
                    modifier = Modifier
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .animateItem(),
                    application = application,
                    onApplicationClick = {
                        onBioClick(application)
                    },
                    onApplicationLongClick = {
                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                        onApplicationLongPress(application)
                    }
                )
            }
        }

        AnimatedVisibility(
            modifier = Modifier
                .fillMaxSize(),
            visible = isShowDestination
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            translationY = overscrollOffset
                        },
                )
                {
                    Box(
                        modifier
                            .align(Alignment.Center)
                            .clip(MaterialTheme.shapes.extraLarge)
                            .background(color = MaterialTheme.colorScheme.surface)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline,
                                MaterialTheme.shapes.extraLarge
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .animateContentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier
                                    .size(24.dp),
                                painter = painterResource(xcj.app.compose_share.R.drawable.ic_round_search_24),
                                contentDescription = stringResource(xcj.app.appsets.R.string.search),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            AnimatedVisibility(overscrollOffset > 200f) {
                                Box(modifier = Modifier.padding(horizontal = 12.dp)) {
                                    Text(
                                        text = stringResource(xcj.app.appsets.R.string.search),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SingleApplicationComponent(
    modifier: Modifier = Modifier,
    application: Application,
    onApplicationClick: () -> Unit,
    onApplicationLongClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnyImage(
            modifier = Modifier
                .size(68.dp)
                .clip(MaterialTheme.shapes.extShapes.large)
                .background(
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.extShapes.large
                )
                .border(
                    1.dp,
                    MaterialTheme.colorScheme.outline,
                    MaterialTheme.shapes.extShapes.large
                )
                .combinedClickable(
                    enabled = true,
                    onClick = onApplicationClick,
                    onLongClick = onApplicationLongClick
                ),
            model = application.bioUrl
        )
        Text(
            text = application.bioName ?: "",
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            modifier = Modifier
                .widthIn(max = 82.dp)
        )
    }
}