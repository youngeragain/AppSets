package xcj.app.compose_share.modifier

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import dev.chrisbanes.haze.HazeEffectScope
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeStyle
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import xcj.app.starter.android.ProjectConstants

@Composable
fun rememberHazeStateIfAvailable(): HazeState? {
    if (LocalInspectionMode.current || ProjectConstants.IS_IN_ANDROID_STUDIO_PREVIEW) {
        return null
    }
    val hazeState = rememberHazeState()
    return hazeState
}


fun Modifier.hazeSourceIfAvailable(
    state: HazeState?,
    zIndex: Float = 0f,
    key: Any? = null,
): Modifier {
    return if (state == null) {
        this
    } else {
        hazeSource(state, zIndex, key)
    }
}

fun Modifier.hazeEffectIfAvailable(
    state: HazeState?,
    style: HazeStyle = HazeStyle.Unspecified,
    block: (HazeEffectScope.() -> Unit)? = null,
): Modifier {
    if (state == null) {
        return this
    }
    return hazeEffect(state, style, block)
}