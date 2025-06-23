package xcj.app.appsets.ui.compose.custom_component

import androidx.compose.animation.core.AnimationState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateTo
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.sin

data class CardInfo(
    val rotationX: Float = 0f,
    val rotationY: Float = 0f,
    val rotationZ: Float = 0f
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FlipCardContainer(
    modifier: Modifier = Modifier,
    cardModifier: Modifier = Modifier,
    cardShape: Shape = CardDefaults.shape,
    cardColors: CardColors = CardDefaults.cardColors(),
    onCardClick: () -> Unit,
    frontContent: @Composable () -> Unit,
    backContent: @Composable () -> Unit
) {
    val animateState = remember { AnimationState(0f) }

    var status by remember {
        mutableStateOf(false)
    }
    var card1Info by remember {
        mutableStateOf(CardInfo())
    }
    val base = 180f
    val card2Info by remember {
        derivedStateOf {
            card1Info.copy(
                rotationX = base + card1Info.rotationX,
                rotationY = base + card1Info.rotationY,
                rotationZ = base + card1Info.rotationZ
            )
        }
    }
    var scale = remember {
        derivedStateOf {
            val animatedValue = animateState.value
            1 - sin(Math.toRadians(animatedValue.toDouble())).toFloat() * 0.38f
        }
    }

    val coroutineScope = rememberCoroutineScope()
    val interactionSource = remember { MutableInteractionSource() } // 记住 InteractionSource
    val context = LocalContext.current
    val hapticFeedback = LocalHapticFeedback.current
    Box(
        modifier
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onCardClick,
                onLongClick = {
                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                    coroutineScope.launch {
                        status = !status
                        val targetValue = if (status) {
                            base
                        } else {
                            0f
                        }
                        animateState.animateTo(
                            targetValue, animationSpec = spring(
                                dampingRatio = Spring.DampingRatioHighBouncy,
                                stiffness = Spring.StiffnessVeryLow
                            )
                        ) {
                            val v = animateState.value
                            card1Info = card1Info.copy(
                                rotationX = v,
                                rotationY = v,
                                rotationZ = v
                            )
                        }
                    }
                }
            )
    ) {
        Card(
            modifier = cardModifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    //rotationX = card2Info.rotationX
                    rotationY = card2Info.rotationY
                    //rotationZ = card2Info.rotationZ
                    alpha = if (card2Info.rotationX <= (base + 90f)) {
                        0f
                    } else {
                        1f
                    }
                },
            shape = cardShape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 12.dp),
            colors = cardColors
        ) {
            backContent()
        }
        Card(
            modifier = cardModifier
                .graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    //rotationX = card1Info.rotationX
                    rotationY = card1Info.rotationY
                    //rotationZ = card1Info.rotationZ
                    alpha = if (card1Info.rotationX >= 90f) {
                        0f
                    } else {
                        1f
                    }
                },
            shape = cardShape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = 0.dp),
            colors = cardColors
        ) {
            frontContent()
        }
    }

}