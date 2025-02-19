package xcj.app.compose_share.components

import androidx.compose.runtime.staticCompositionLocalOf
import xcj.app.compose_share.usecase.ComposeDynamicUseCase

val LocalAnyStateProvider =
    staticCompositionLocalOf<AnyStateProvider> { error("No AnyStateProvider provided") }

val LocalUseCaseOfComposeDynamic =
    staticCompositionLocalOf<ComposeDynamicUseCase> { error("No ComposeDynamicUseCase provided") }