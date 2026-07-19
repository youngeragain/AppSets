package xcj.app.compose_share.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import dev.chrisbanes.haze.HazeState
import xcj.app.compose_share.usecase.ComposeDynamicUseCase

val LocalVisibilityComposeStateProvider =
    staticCompositionLocalOf<VisibilityComposeStateProvider> { error("No AnyStateProvider provided") }

val LocalUseCaseOfComposeDynamic =
    staticCompositionLocalOf<ComposeDynamicUseCase> { error("No ComposeDynamicUseCase provided") }

val LocalHazedStateMap =
    staticCompositionLocalOf<MutableMap<String, HazeState?>> { hashMapOf() }

const val HAZE_KEY_OF_MAIN = "MainHazeState"
const val HAZE_KEY_OF_PAGE = "PageHazeState"

val mainHazeState: HazeState?
    @Composable
    get() = LocalHazedStateMap.current[HAZE_KEY_OF_MAIN]

val pageHazeState: HazeState?
    @Composable
    get() = LocalHazedStateMap.current[HAZE_KEY_OF_PAGE]