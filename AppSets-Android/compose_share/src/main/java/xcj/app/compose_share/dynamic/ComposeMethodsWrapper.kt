package xcj.app.compose_share.dynamic

import androidx.compose.runtime.Composable

data class ComposeMethodsWrapper(
    val iComposeMethods: IComposeMethodsAware,
    val content: @Composable () -> Unit
)