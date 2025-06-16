@file:OptIn(ExperimentalFoundationApi::class)

package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import xcj.app.appsets.ui.compose.content_selection.DragValue
import kotlin.math.roundToInt

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SwipeContainer(
    squareSize: Dp = 48.dp,
    onDragValueChanged: (DragValue) -> Unit,
    dragContent: @Composable () -> Unit
) {
    val width = squareSize * 2
    val state: AnchoredDraggableState<DragValue> =
        rememberSwipeContainerState(squareSize, onDragValueChanged)
    Box(
        modifier = Modifier
            .width(width)
            .anchoredDraggable(
                state = state,
                orientation = Orientation.Horizontal
            )
            .background(MaterialTheme.colorScheme.outline, CircleShape)
    ) {
        Box(
            Modifier
                .offset {
                    IntOffset(
                        x = state
                            .requireOffset()
                            .roundToInt(), y = 0
                    )
                }
                .size(squareSize)
                .background(MaterialTheme.colorScheme.tertiaryContainer, CircleShape)
        ) {
            dragContent()
        }
    }
}

@Composable
fun rememberSwipeContainerState(
    squareSize: Dp,
    onDragValueChanged: (DragValue) -> Unit
): AnchoredDraggableState<DragValue> {
    val density = LocalDensity.current
    val anchors = with(density) {
        DraggableAnchors {
            DragValue.Start at 0f
            DragValue.End at squareSize.toPx()
        }
    }
    val state = remember {
        AnchoredDraggableState<DragValue>(
            initialValue = DragValue.Start,
            positionalThreshold = {
                it
            },
            velocityThreshold = {
                anchors.maxPosition()
            },
            snapAnimationSpec = tween(450),
            decayAnimationSpec = exponentialDecay(),
            confirmValueChange = { dragValue ->
                onDragValueChanged(dragValue)
                true
            }
        )
    }

    SideEffect {
        state.updateAnchors(anchors)
    }

    return state
}