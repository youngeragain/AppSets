@file:OptIn(ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.apps

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.AnimationVector1D
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import xcj.app.appsets.ui.compose.custom_component.ShowNavBarWhenOnLaunch
import xcj.app.appsets.ui.compose.theme.AppSetsShapes
import xcj.app.appsets.ui.model.page_state.AppCenterPageState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppsCenterPage(
    appCenterPageState: AppCenterPageState,
    onBioClick: (Bio) -> Unit,
    onApplicationLongPress: (Application) -> Unit
) {
    ShowNavBarWhenOnLaunch()

    val coroutineScope = rememberCoroutineScope()

    var allApplications = appCenterPageState.apps.flatMap { it.applications }
    val iconAnimationStates = remember {
        val animations = mutableListOf<AnimationState<Float, AnimationVector1D>>()
        val allCount = allApplications.size
        repeat(allCount) { index ->
            animations.add(AnimationState(0f))
        }
        animations
    }
    LaunchedEffect(true) {
        if (appCenterPageState is AppCenterPageState.LoadSuccess) {
            val allCount = iconAnimationStates.size
            val center = (allCount) / 2 + 1
            iconAnimationStates.forEachIndexed { index, animation ->
                val delay = if (index <= center) {
                    ((1f - index.toFloat() / center.toFloat()) * 180).toInt()
                } else {
                    (((index - center).toFloat() / center.toFloat()) * 180).toInt()
                }
                coroutineScope.launch {
                    animation.animateTo(1f, tween(550, delay))
                }
            }
        } else {
            iconAnimationStates.forEach {
                coroutineScope.launch {
                    it.animateTo(1f, snap())
                }
            }
        }
    }
    val hapticFeedback = LocalHapticFeedback.current
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
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .graphicsLayer {
                        val animateFraction = iconAnimationStates[index].value
                        scaleX = animateFraction
                        scaleY = animateFraction
                        alpha = animateFraction
                    }
                    .animateItem(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnyImage(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(AppSetsShapes.large)
                        .background(MaterialTheme.colorScheme.outline, AppSetsShapes.large)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            AppSetsShapes.large
                        )
                        .combinedClickable(
                            enabled = true,
                            onClick = {
                                if (appCenterPageState !is AppCenterPageState.LoadSuccess) {
                                    return@combinedClickable
                                }
                                onBioClick(application)
                            },
                            onLongClick = {
                                if (appCenterPageState !is AppCenterPageState.LoadSuccess) {
                                    return@combinedClickable
                                }
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onApplicationLongPress(application)
                            }
                        ),
                    model = application.bioUrl
                )
                Text(
                    text = application.name ?: "",
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .widthIn(max = 82.dp)
                )
            }
        }
    }
}