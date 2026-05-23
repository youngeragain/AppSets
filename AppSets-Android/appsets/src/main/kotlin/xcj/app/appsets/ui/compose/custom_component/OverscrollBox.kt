package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import xcj.app.compose_share.foundation_extension.customHorizontalOverscroll
import xcj.app.compose_share.foundation_extension.customVerticalOverscroll
import kotlin.math.roundToInt

data class OverScrollBoxInfo(
    val orientation: Orientation? = null,
    val offset: MutableState<Float> = mutableFloatStateOf(0f)
)


interface OverScrollBoxOffsetContentProvider {
    fun getContent(): @Composable () -> Unit
}

val LocalOverScrollBoxOffsetInfo = staticCompositionLocalOf { OverScrollBoxInfo() }

val LocalOverScrollBoxOffsetContent =
    staticCompositionLocalOf<OverScrollBoxOffsetContentProvider?> { null }

@Composable
fun VerticalOverscrollBox(
    modifier: Modifier = Modifier,
    onOverscrollOffset: ((Float) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val overScrollBoxOffsetContent = LocalOverScrollBoxOffsetContent.current
    val overScrollBoxInfo = remember {
        OverScrollBoxInfo(Orientation.Vertical)
    }
    CompositionLocalProvider(
        LocalOverScrollBoxOffsetInfo provides overScrollBoxInfo
    ) {
        VerticalOverscrollBox2(
            modifier = modifier.fillMaxSize(),
            onOverscrollOffset = { offset ->
                overScrollBoxInfo.offset.value = offset
                onOverscrollOffset?.invoke(offset)
            },
            content = {
                Box(modifier = Modifier.fillMaxSize()) {
                    content()
                    overScrollBoxOffsetContent?.getContent()
                }
            }
        )
    }
}

@Composable
fun VerticalOverscrollBox2(
    modifier: Modifier = Modifier,
    onOverscrollOffset: ((Float) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var animatedOverscrollAmount by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .customVerticalOverscroll(
                onNewOverscrollAmount = {
                    animatedOverscrollAmount = it
                    onOverscrollOffset?.invoke(it)
                }
            )
            .offset { IntOffset(0, animatedOverscrollAmount.roundToInt()) }
    ) {
        content()
    }
}

@Composable
fun HorizontalOverscrollBox(
    modifier: Modifier = Modifier,
    onOverscrollOffset: ((Float) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val overScrollBoxOffsetContent = LocalOverScrollBoxOffsetContent.current
    val overScrollBoxInfo = remember {
        OverScrollBoxInfo(Orientation.Horizontal)
    }
    CompositionLocalProvider(
        LocalOverScrollBoxOffsetInfo provides overScrollBoxInfo
    ) {
        HorizontalOverscrollBox2(
            modifier = modifier.fillMaxSize(),
            onOverscrollOffset = { offset ->
                overScrollBoxInfo.offset.value = offset
                onOverscrollOffset?.invoke(offset)
            },
            content = {
                content()
                overScrollBoxOffsetContent?.getContent()
            }
        )
    }
}

@Composable
fun HorizontalOverscrollBox2(
    modifier: Modifier = Modifier,
    onOverscrollOffset: ((Float) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var animatedOverscrollAmount by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .customHorizontalOverscroll(
                onNewOverscrollAmount = {
                    animatedOverscrollAmount = it
                    onOverscrollOffset?.invoke(it)
                }
            )
            .offset { IntOffset(animatedOverscrollAmount.roundToInt(), 0) }
    ) {
        content()
    }
}