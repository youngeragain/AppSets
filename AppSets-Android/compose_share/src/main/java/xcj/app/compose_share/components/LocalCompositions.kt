package xcj.app.compose_share.components

import androidx.compose.runtime.staticCompositionLocalOf
import xcj.app.compose_share.usecase.ComposeDynamicUseCase

val LocalVisibilityComposeStateProvider =
    staticCompositionLocalOf<VisibilityComposeStateProvider> { error("No AnyStateProvider provided") }

val LocalUseCaseOfComposeDynamic =
    staticCompositionLocalOf<ComposeDynamicUseCase> { error("No ComposeDynamicUseCase provided") }