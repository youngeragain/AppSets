package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp

@Composable
fun ImageHoldShowContainer(url: String) {
    Box(
        modifier = Modifier
            .padding(horizontal = 12.dp)
            .fillMaxSize()
    ) {

        var pressInteraction by remember {
            mutableStateOf<PressInteraction?>(null)
        }
        var sizeOfItem by remember {
            mutableStateOf(IntSize.Zero)
        }

        val interactionSource = remember {
            MutableInteractionSource()
        }
        LaunchedEffect(key1 = interactionSource, block = {
            interactionSource.interactions.collect { interaction ->
                when (interaction) {
                    is PressInteraction.Press -> {
                        pressInteraction = interaction
                    }

                    is PressInteraction.Release, is PressInteraction.Cancel -> {
                        pressInteraction = null
                    }
                }
            }
        })

        val bigImageHeight = if (pressInteraction is PressInteraction.Press) {
            500.dp
        } else {
            0.dp
        }
        val size = animateDpAsState(
            targetValue = bigImageHeight,
            animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow),
            finishedListener = {
                if (it.value == 0f)
                    pressInteraction = null
            },
            label = "show_big_image_animate"
        )
        AnyImage(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .size(size.value)
                .clip(MaterialTheme.shapes.extraLarge),
            model = url,
            contentScale = ContentScale.FillWidth
        )
    }
}