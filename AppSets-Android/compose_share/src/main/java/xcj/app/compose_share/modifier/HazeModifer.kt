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
import xcj.app.starter.android.util.PurpleLogger

private const val TAG = "HazeModifier"

@Composable
fun rememberHazeStateIfAvailable(): HazeState? {
    if (LocalInspectionMode.current || ProjectConstants.IS_IN_ANDROID_STUDIO_PREVIEW) {
        PurpleLogger.current.d(
            TAG,
            "rememberHazeStateIfAvailable, in inspection mode or in preview!"
        )
        return null
    }
    PurpleLogger.current.d(TAG, "rememberHazeStateIfAvailable make state")
    val hazeState = rememberHazeState()
    return hazeState
}


fun Modifier.hazeSourceIfAvailable(
    state: HazeState?,
    zIndex: Float = 0f,
    key: Any? = null,
): Modifier {
    if (state == null) {
        PurpleLogger.current.d(TAG, "hazeSourceIfAvailable, state is null!")
        return this
    }
    PurpleLogger.current.d(TAG, "hazeSourceIfAvailable, get state")
    return hazeSource(state, zIndex, key)
}

fun Modifier.hazeEffectIfAvailable(
    state: HazeState?,
    style: HazeStyle = HazeStyle.Unspecified,
    block: (HazeEffectScope.() -> Unit)? = null,
): Modifier {
    return hazeEffectIfAvailableWithTag(
        effectTag = "hazeEffect",
        state = state,
        style = style,
        block = block
    )
}

fun Modifier.hazeEffectIfAvailableWithTag(
    effectTag: String? = null,
    state: HazeState? = null,
    style: HazeStyle = HazeStyle.Unspecified,
    block: (HazeEffectScope.() -> Unit)? = null,
): Modifier {
    if (state == null) {
        PurpleLogger.current.d(TAG, "hazeEffectIfAvailableWithTag, $effectTag, state is null!")
        return this
    }
    PurpleLogger.current.d(TAG, "hazeEffectIfAvailableWithTag, $effectTag, get state")
    return hazeEffect(state, style, block)
}