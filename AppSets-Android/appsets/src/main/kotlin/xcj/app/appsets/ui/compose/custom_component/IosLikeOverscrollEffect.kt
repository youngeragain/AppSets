package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.OverscrollEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch


class IosLikeOverscrollEffect(val scope: CoroutineScope) : OverscrollEffect {
    // 实际显示的偏移量，增加水平和垂直两个方向
    private val offsetX = Animatable(0f)
    private val offsetY = Animatable(0f)

    // 1. 处理滚动逻辑
    override fun applyToScroll(
        delta: Offset,
        source: NestedScrollSource,
        performScroll: (Offset) -> Offset,
    ): Offset {
        // 先尝试让列表正常滚动
        val consumedByScroll = performScroll(delta)
        val leftover = delta - consumedByScroll

        // 如果有剩余（到达边界）且是手动拖拽
        if (source == NestedScrollSource.UserInput) {
            if (leftover.x != 0f || leftover.y != 0f) {
                scope.launch {
                    // 应用阻尼效果（0.3f 模拟 iOS 的拉伸阻力）
                    if (leftover.x != 0f) {
                        offsetX.snapTo(offsetX.value + leftover.x * 0.3f)
                    }
                    if (leftover.y != 0f) {
                        offsetY.snapTo(offsetY.value + leftover.y * 0.3f)
                    }
                }
                // 消耗掉全部增量，防止系统默认的拉伸效果（如果有）触发
                return delta
            }
        }
        return consumedByScroll
    }

    // 2. 处理惯性停止后的回弹
    override suspend fun applyToFling(
        velocity: Velocity,
        performFling: suspend (Velocity) -> Velocity,
    ) {
        // 关键：必须执行 performFling，否则列表在松手后不会有惯性滚动
        performFling(velocity)

        // 惯性滚动结束或松手后，动画回弹到 0
        coroutineScope {
            launch {
                offsetX.animateTo(0f, spring(Spring.DampingRatioLowBouncy))
            }
            launch {
                offsetY.animateTo(0f, spring(Spring.DampingRatioLowBouncy))
            }
        }
    }

    // 3. 将效果应用到布局
    override val effectModifier: Modifier = Modifier.graphicsLayer {
        // 同时支持水平和垂直位移
        translationX = offsetX.value
        translationY = offsetY.value
    }

    override val isInProgress: Boolean
        get() = offsetX.value != 0f || offsetY.value != 0f
}
