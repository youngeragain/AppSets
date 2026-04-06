package xcj.app.compose_share.foundation_extension

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.calculateTargetValue
import androidx.compose.animation.core.exponentialDecay
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.launch
import kotlin.math.sign

//Thank you for sharing the implementation effect with @sinasamaki
//https://www.sinasamaki.com/overscroll-animations-in-jetpack-compose/

val CustomEasing: Easing = CubicBezierEasing(0.5f, 0.5f, 1.0f, 0.25f)

@Composable
fun Modifier.customOverscrollBase(
    orientation: Orientation,
    onNewOverscrollAmount: (Float) -> Unit,
    animationSpec: SpringSpec<Float> = spring(stiffness = Spring.StiffnessLow)
): Modifier {
    // implementation goes here
    val overscrollAmountAnimatable = remember { Animatable(0f) }
    var length by remember { mutableFloatStateOf(1f) }
    LaunchedEffect(Unit) {
        snapshotFlow { overscrollAmountAnimatable.value }.collect {
            onNewOverscrollAmount(CustomEasing.transform(it / (length * 1.5f)) * length)
        }
    }
    val scope = rememberCoroutineScope()
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            // we will override the
            // functions here (onPostSroll, onPreFling, etc.)
            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                scope.launch {
                    overscrollAmountAnimatable.snapTo(targetValue = calculateOverscroll(available))
                }
                return Offset.Zero
            }

            override suspend fun onPostFling(consumed: Velocity, available: Velocity): Velocity {
                val availableVelocity = when (orientation) {
                    Orientation.Vertical -> available.y
                    Orientation.Horizontal -> available.x
                }

                overscrollAmountAnimatable.animateTo(
                    targetValue = 0f,
                    initialVelocity = availableVelocity,
                    animationSpec = animationSpec
                )

                return available
            }

            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (overscrollAmountAnimatable.value != 0f && source != NestedScrollSource.SideEffect) {
                    scope.launch {
                        overscrollAmountAnimatable.snapTo(calculateOverscroll(available))
                    }
                    return available
                }

                return super.onPreScroll(available, source)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val availableVelocity = when (orientation) {
                    Orientation.Vertical -> available.y
                    Orientation.Horizontal -> available.x
                }

                if (overscrollAmountAnimatable.value != 0f && availableVelocity != 0f) {
                    val previousSign = overscrollAmountAnimatable.value.sign
                    var consumedVelocity = availableVelocity
                    val predictedEndValue = exponentialDecay<Float>().calculateTargetValue(
                        initialValue = overscrollAmountAnimatable.value,
                        initialVelocity = availableVelocity,
                    )
                    if (predictedEndValue.sign == previousSign) {
                        overscrollAmountAnimatable.animateTo(
                            targetValue = 0f,
                            initialVelocity = availableVelocity,
                            animationSpec = animationSpec,
                        )
                    } else {
                        try {
                            overscrollAmountAnimatable.animateDecay(
                                initialVelocity = availableVelocity,
                                animationSpec = exponentialDecay()
                            ) {
                                if (value.sign != previousSign) {
                                    consumedVelocity -= velocity
                                    scope.launch {
                                        overscrollAmountAnimatable.snapTo(0f)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                        }
                    }

                    return when (orientation) {
                        Orientation.Vertical -> Velocity(0f, consumedVelocity)
                        Orientation.Horizontal -> Velocity(consumedVelocity, 0f)
                    }
                }

                return super.onPreFling(available)
            }

            private fun calculateOverscroll(available: Offset): Float {
                val previous = overscrollAmountAnimatable.value
                val newValue = previous + when (orientation) {
                    Orientation.Vertical -> available.y
                    Orientation.Horizontal -> available.x
                }
                return when {
                    previous > 0 -> newValue.coerceAtLeast(0f)
                    previous < 0 -> newValue.coerceAtMost(0f)
                    else -> newValue
                }
            }
        }
    }
    return this
        .onSizeChanged {
            length = when (orientation) {
                Orientation.Vertical -> it.height.toFloat()
                Orientation.Horizontal -> it.width.toFloat()
            }
        }
        .nestedScroll(nestedScrollConnection)
}

@Composable
fun Modifier.customOverscroll(
    listState: LazyListState,
    onNewOverscrollAmount: (Float) -> Unit,
    animationSpec: SpringSpec<Float> = spring(stiffness = Spring.StiffnessLow)
) = customOverscrollBase(
    orientation = remember { listState.layoutInfo.orientation },
    onNewOverscrollAmount = onNewOverscrollAmount,
    animationSpec = animationSpec
)

@Composable
fun Modifier.customOverscroll(
    pagerState: PagerState,
    onNewOverscrollAmount: (Float) -> Unit,
    animationSpec: SpringSpec<Float> = spring(stiffness = Spring.StiffnessLow)
) = customOverscrollBase(
    orientation = remember { pagerState.layoutInfo.orientation },
    onNewOverscrollAmount = onNewOverscrollAmount,
    animationSpec = animationSpec
)

@Composable
fun Modifier.customHorizontalOverscroll(
    onNewOverscrollAmount: (Float) -> Unit,
    animationSpec: SpringSpec<Float> = spring(stiffness = Spring.StiffnessLow)
) = customOverscrollBase(
    orientation = Orientation.Horizontal,
    onNewOverscrollAmount = onNewOverscrollAmount,
    animationSpec = animationSpec
)

@Composable
fun Modifier.customVerticalOverscroll(
    onNewOverscrollAmount: (Float) -> Unit,
    animationSpec: SpringSpec<Float> = spring(stiffness = Spring.StiffnessLow)
) = customOverscrollBase(
    orientation = Orientation.Vertical,
    onNewOverscrollAmount = onNewOverscrollAmount,
    animationSpec = animationSpec
)