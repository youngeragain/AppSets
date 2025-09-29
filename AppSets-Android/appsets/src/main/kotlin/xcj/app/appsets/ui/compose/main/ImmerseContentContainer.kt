package xcj.app.appsets.ui.compose.main

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import xcj.app.compose_share.components.LocalVisibilityComposeStateProvider
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.immerseContentState

@Composable
fun ImmerseContentContainer(
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val visibilityComposeStateProvider = LocalVisibilityComposeStateProvider.current
    val immerseContentState = visibilityComposeStateProvider.immerseContentState()
    Box(
        modifier
            .fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = immerseContentState.isShow,
            enter = fadeIn(animationSpec = tween()) + scaleIn(
                initialScale = 1.12f,
                animationSpec = tween()
            ),
            exit = fadeOut() + scaleOut(
                targetScale = 1.12f
            ),
        ) {
            immerseContentState.getContent(context)?.Content()
        }
    }
}