@file:OptIn(ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.apps

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.ui.compose.custom_component.AnyImage
import xcj.app.appsets.ui.compose.custom_component.ShowNavBar
import xcj.app.appsets.ui.compose.theme.extShapes
import xcj.app.appsets.ui.model.page_state.AppCenterPageState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsCenterPage(
    appCenterPageState: AppCenterPageState,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit
) {
    ShowNavBar()
    val hapticFeedback = LocalHapticFeedback.current
    val allApplications by rememberUpdatedState(appCenterPageState.apps.flatMap { it.applications })
    val iconAnimationStates = remember {
        buildList {
            val allCount = allApplications.size
            repeat(allCount) { index ->
                add(AnimationState(0f))
            }
        }
    }
    LaunchedEffect(true) {
        if (appCenterPageState is AppCenterPageState.LoadSuccess) {
            val allCount = iconAnimationStates.size
            val center = (allCount) / 2 + 1
            iconAnimationStates.forEachIndexed { index, animation ->
                val delay = if (index <= center) {
                    ((1f - index.toFloat() / center.toFloat()) * 150).toInt()
                } else {
                    (((index - center).toFloat() / center.toFloat()) * 150).toInt()
                }
                launch {
                    animation.animateTo(1f, tween(450, delay))
                }
            }
        } else {
            iconAnimationStates.forEach {
                launch {
                    it.animateTo(1f, snap())
                }
            }
        }
    }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(90.dp),
        modifier = Modifier,
        state = rememberLazyGridState(),
        contentPadding = PaddingValues(
            top = WindowInsets.statusBars.asPaddingValues().calculateTopPadding(),
            bottom = 68.dp
        )
    ) {
        itemsIndexed(items = allApplications) { index, application ->
            val animateFraction by iconAnimationStates[index]
            SingleApplicationComponent(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .graphicsLayer {
                        scaleX = animateFraction
                        scaleY = animateFraction
                        alpha = animateFraction
                    }
                    .animateItem(),
                application = application,
                onApplicationClick = {
                    if (appCenterPageState !is AppCenterPageState.LoadSuccess) {
                        return@SingleApplicationComponent
                    }
                    onBioClick(application)
                },
                onApplicationLongClick = {
                    if (appCenterPageState !is AppCenterPageState.LoadSuccess) {
                        return@SingleApplicationComponent
                    }
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    onApplicationLongPress(application)
                }
            )
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