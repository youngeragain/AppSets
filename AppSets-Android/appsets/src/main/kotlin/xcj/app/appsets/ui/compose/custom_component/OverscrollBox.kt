package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import xcj.app.compose_share.foundation_extension.customHorizontalOverscroll
import xcj.app.compose_share.foundation_extension.customVerticalOverscroll
import kotlin.math.roundToInt

@Composable
fun VerticalOverscrollBox(
    modifier: Modifier = Modifier,
    onOverscrollOffset: ((Float) -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    var animatedOverscrollAmount by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .fillMaxSize()
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
    var animatedOverscrollAmount by remember { mutableFloatStateOf(0f) }
    Box(
        modifier = modifier
            .fillMaxSize()
            .customHorizontalOverscroll(
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