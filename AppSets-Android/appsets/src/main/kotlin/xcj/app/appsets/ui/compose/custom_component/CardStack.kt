package xcj.app.appsets.ui.compose.custom_component

import androidx.annotation.IntRange
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.msusman.compose.cardstack.Direction
import com.msusman.compose.cardstack.Duration
import com.msusman.compose.cardstack.SwipeDirection
import com.msusman.compose.cardstack.SwipeMethod
import com.msusman.compose.cardstack.internal.visible
import com.msusman.compose.cardstack.utils.isNegative
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


@Composable
fun <T> CardStack0(
    modifier: Modifier = Modifier,
    stackState0: StackState0<T>,
    items: List<T>,
    stackDirection: Direction = Direction.Bottom,
    @IntRange(from = 1) visibleCount: Int = 3,
    cardElevation: Dp = 10.dp,
    scaleRatio: Float = 0.95f,
    displacementThreshold: Dp = 60.dp,
    animationDuration: Duration = Duration.NORMAL,
    @IntRange(from = 0, to = 360) rotationMaxDegree: Int = 20,
    swipeDirection: SwipeDirection = SwipeDirection.FREEDOM,
    swipeMethod: SwipeMethod = SwipeMethod.AUTOMATIC_AND_MANUAL,
    shadowElevation: Dp,
    shadowShape: Shape,
    onSwiped: (Int) -> Unit = { _ -> },
    content: @Composable (T) -> Unit,
) {
    require(visibleCount in 1..items.size) {
        "visibilityCount must be greater than 0 and less than items size"
    }
    require(cardElevation >= 0.dp) {
        "cardElevation must not be negative"
    }
    require(scaleRatio in 0.0f..1.0f) {
        "scaleRatio must be between 0.0 and 1.0 (inclusive)"
    }
    require(rotationMaxDegree in 0..360) {
        "rotationMaxDegree must be between 0 and 360 (inclusive)"
    }
    val density = LocalDensity.current
    val onCardSwiped: (Int) -> Unit = {
        onSwiped.invoke(it)
    }
    var rewind by remember { mutableIntStateOf(0) }
    val onRewind: () -> Unit = {
        rewind += 1
    }
    //Initialize stack state
    stackState0.initilize(
        items = items,
        direction = stackDirection,
        visibleCount = visibleCount,
        stackElevationPx = with(density) { cardElevation.toPx() },
        scaleInterval = scaleRatio,
        displacementThresholdpx = with(density) { displacementThreshold.toPx() },
        animationDuration = animationDuration,
        rotationMaxDegree = rotationMaxDegree,
        swipeDirection = swipeDirection,
        swipeMethod = swipeMethod,
        onSwiped = onCardSwiped,
        onRewind = onRewind,
    )

    //calculate stack padding based on number of card visible
    val scalePadding =
        (1..visibleCount).sumOf { cardElevation.times((1 - scaleRatio).pow(it)).value.toDouble() }.dp
    val stackPadding = cardElevation.times(visibleCount)
    val (stackPaddingHorizontal, stackPaddingVertical) = when (stackDirection) {
        Direction.None -> 0.dp to 0.dp
        Direction.Top -> 0.dp to stackPadding.minus(scalePadding)
        Direction.Bottom -> 0.dp to stackPadding.minus(scalePadding)
        Direction.Left -> stackPadding.minus(scalePadding) to 0.dp
        Direction.Right -> stackPadding.minus(scalePadding) to 0.dp
        Direction.TopAndLeft -> stackPadding to stackPadding
        Direction.TopAndRight -> stackPadding to stackPadding
        Direction.BottomAndLeft -> stackPadding to stackPadding
        Direction.BottomAndRight -> stackPadding to stackPadding
    }
    val cardQueueChanged by stackState0.cardQueueChanged

    LaunchedEffect(cardQueueChanged) {
        stackState0.initCardQueue(cardQueueChanged > 0)
    }

    Box(
        modifier = modifier
            .padding(
                horizontal = stackPaddingHorizontal,
                vertical = stackPaddingVertical
            )
            .pointerInput(key1 = Unit) {
                if (stackState0.canDrag) {
                    detectDragGestures(
                        onDrag = stackState0::onDrag,
                        onDragEnd = stackState0::onDragEnd
                    )
                }
            }
    ) {

        stackState0.rawItems.forEachIndexed { index, item ->
            val cardState0 = stackState0.cardQueue.getOrNull(index)
            if (cardState0 != null) {
                CardContainer0(
                    cardState0 = cardState0,
                    shadowElevation = if (stackState0.rawItems.size > 1) {
                        shadowElevation
                    } else {
                        0.dp
                    },
                    shadowShape = shadowShape,
                    cardQueueChanged = cardQueueChanged
                ) {
                    content(cardState0.data)
                }
            }
        }
    }
}

@Composable
fun CardContainer0(
    cardState0: CardSate0<*>,
    shadowShape: Shape,
    shadowElevation: Dp,
    cardQueueChanged: Int,
    content: @Composable () -> Unit,
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(cardState0.zIndex)
            .visible(cardState0.shouldVisible)
            .graphicsLayer {
                translationX = cardState0.offsetX.value
                translationY = cardState0.offsetY.value
                scaleX = cardState0.scaleX.value
                scaleY = cardState0.scaleY.value
                rotationZ = cardState0.rotation.value
            }
            .shadow(shadowElevation, shadowShape),
    ) {
        content()
    }
}

@Composable
fun <T> rememberStackState0(canDrag: Boolean = true, infinite: Boolean = true): StackState0<T> {
    val scope = rememberCoroutineScope()
    val screenWidth: Float =
        with(LocalDensity.current) { LocalConfiguration.current.screenWidthDp.dp.toPx() }
    val screenHeight: Float =
        with(LocalDensity.current) { LocalConfiguration.current.screenHeightDp.dp.toPx() }
    return remember {
        StackState0(
            scope = scope,
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            canDrag = canDrag,
            infinite = infinite
        )
    }
}


class StackState0<T>(
    private val scope: CoroutineScope,
    private val screenWidth: Float,
    private val screenHeight: Float,
    val canDrag: Boolean = true,
    val infinite: Boolean = true,
) {
    private var direction: Direction = Direction.None
    private var visibleCount: Int = 0
    private var stackElevationPx: Float = 0.0f
    private var scaleInterval: Float = 0.0f
    private var displacementThresholdpx: Float = 0.0f
    private var animationDuration: Duration = Duration.NORMAL
    private var rotationMaxDegree: Int = 0
    private var swipeDirection: SwipeDirection = SwipeDirection.FREEDOM
    private var swipeMethod: SwipeMethod = SwipeMethod.AUTOMATIC_AND_MANUAL
    var rawItems: List<T> = emptyList()
    private var items: List<T> = emptyList()
    val cardQueueChanged: MutableState<Int> = mutableIntStateOf(0)
    var cardQueue: MutableList<CardSate0<T>> = mutableListOf()
    private var dragOffsetX = 0f
    private var dragOffsetY = 0f
    private var onSwiped: (Int) -> Unit = { }
    private var onRewind: () -> Unit = { }
    private var topCardIndex = 0

    fun initilize(
        items: List<T>,
        direction: Direction,
        visibleCount: Int,
        stackElevationPx: Float,
        scaleInterval: Float,
        displacementThresholdpx: Float,
        animationDuration: Duration,
        rotationMaxDegree: Int,
        swipeDirection: SwipeDirection,
        swipeMethod: SwipeMethod,
        onSwiped: (Int) -> Unit,
        onRewind: () -> Unit,
    ) {
        this.rawItems = items
        this.items = items
        this.direction = direction
        this.visibleCount = visibleCount
        this.stackElevationPx = stackElevationPx
        this.scaleInterval = scaleInterval
        this.displacementThresholdpx = displacementThresholdpx
        this.animationDuration = animationDuration
        this.rotationMaxDegree = rotationMaxDegree
        this.swipeDirection = swipeDirection
        this.swipeMethod = swipeMethod
        this.onSwiped = onSwiped
        this.onRewind = onRewind

        initCardQueue(false)
    }

    private fun getSwipeDirection(): Direction {
        val isOffsetXNegative = dragOffsetX.isNegative()  //true if user dragged to left
        val isOffsetYNegative = dragOffsetY.isNegative()  //true if user dragged to upward
        val xThreshHoldReached = abs(dragOffsetX) > displacementThresholdpx
        val yThreshHoldReached = abs(dragOffsetY) > displacementThresholdpx
        return when {
            xThreshHoldReached && yThreshHoldReached -> {
                when {
                    isOffsetXNegative && isOffsetYNegative -> Direction.TopAndLeft
                    isOffsetXNegative -> Direction.BottomAndLeft
                    isOffsetYNegative -> Direction.TopAndRight
                    else -> Direction.BottomAndRight
                }
            }

            xThreshHoldReached -> if (isOffsetXNegative) Direction.Left else Direction.Right
            yThreshHoldReached -> if (isOffsetYNegative) Direction.Top else Direction.Bottom
            else -> Direction.None

        }
    }


    fun onDrag(change: PointerInputChange, dragAmount: Offset) {
        if (!canDrag) {
            return
        }
        if (items.size <= 1) {
            return
        }
        if (cardQueue.firstOrNull()?.isAnimating() == true) {
            return
        }
        if (swipeMethod == SwipeMethod.AUTOMATIC ||
            swipeMethod == SwipeMethod.AUTOMATIC_AND_MANUAL
        ) {
            when (swipeDirection) {
                SwipeDirection.FREEDOM -> {
                    dragOffsetX += dragAmount.x
                    dragOffsetY += dragAmount.y
                }

                SwipeDirection.HORIZONTAL -> dragOffsetX += dragAmount.x
                SwipeDirection.VERTICAL -> dragOffsetY += dragAmount.y
            }
            cardQueue.firstOrNull()?.snapToTranslation(Offset(dragOffsetX, dragOffsetY))
            val rotationZ = calculateRotation()
            cardQueue.firstOrNull()?.snapToRotation(rotationZ)
            change.consume()
        }
    }

    private fun calculateRotation(): Float {
        val resultantOffset = sqrt(dragOffsetX.pow(2) + dragOffsetY.pow(2))
        val calculatedRotationZ = (rotationMaxDegree * resultantOffset) / displacementThresholdpx
        val finalRotationZ = calculatedRotationZ.coerceAtMost(rotationMaxDegree.toFloat())
        return if (dragOffsetX.isNegative() || dragOffsetY.isNegative()) finalRotationZ.unaryMinus() else finalRotationZ
    }


    fun onDragEnd() {
        if (!canDrag) {
            return
        }
        if (items.size <= 1) {
            return
        }
        val swipeDirection: Direction = getSwipeDirection()
        if (swipeMethod.isAutomaticSwipeAllowed()) {
            swipeInternal(swipeDirection)
        }
    }

    fun initCardQueue(rotate: Boolean) {
        if (items.isEmpty()) {
            return
        }
        if (rotate && items.size > 1) {
            val list = items.toMutableList()
            val first = list.first()
            list.add(first)
            items = list
        }
        val size = if (!canDrag) {
            visibleCount
        } else {
            items.size
        }
        val cards = List(size) { index ->
            createCardState0(index)
        }.toMutableList()

        cardQueue = cards
    }

    private fun createCardState0(index: Int): CardSate0<T> {
        val (tx, ty) = Transformations0.calculateTranslation(
            index = index,
            direction = direction,
            visibleCount = visibleCount,
            stackElevationPx = stackElevationPx,
        )
        val (sx, sy) = Transformations0.calculateScale(
            index = index,
            visibleCount = visibleCount,
            direction = direction,
            scaleInterval = scaleInterval,
        )
        val item = items[index]
        return CardSate0(
            screenWidth = screenWidth,
            screenHeight = screenHeight,
            scope = scope,
            data = item,
            offsetX = Animatable(tx),
            offsetY = Animatable(ty),
            scaleX = Animatable(sx),
            scaleY = Animatable(sy),
            rotation = Animatable(0.0f),
            zIndex = 1000.0f - index,
            animationDuration = animationDuration
        )
    }

    fun swipe(swipeDirection: Direction) {
        if (swipeMethod.isManualSwipeAllowed()) {
            swipeInternal(swipeDirection)
        }
    }

    private fun swipeInternal(swipeDirection: Direction) {
        if (!canDrag) {
            return
        }
        if (cardQueue.firstOrNull()?.isAnimating() == true) return
        cardQueue.firstOrNull()?.swipeToward(swipeDirection)
        if (swipeDirection != Direction.None) {
            cardQueue.removeFirstOrNull()
            cardQueue.forEachIndexed { index, cardSate ->
                val translateOffset = Transformations0.calculateTranslation(
                    index = index,
                    direction = direction,
                    visibleCount = visibleCount,
                    stackElevationPx = stackElevationPx,
                )
                val scaleOffset = Transformations0.calculateScale(
                    index = index,
                    visibleCount = visibleCount,
                    direction = direction,
                    scaleInterval = scaleInterval,
                )
                cardQueue[index].translateTo(translateOffset)
                cardQueue[index].scaleTo(scaleOffset)
            }
            scope.launch {
                delay(animationDuration.duration.toLong() + 100)
                topCardIndex++
                if (infinite && topCardIndex == items.size) {
                    topCardIndex = 0
                    initCardQueue(true)
                    cardQueueChanged.value = cardQueueChanged.value + 1
                }
                onSwiped(topCardIndex)
            }
        }
        dragOffsetX = 0f
        dragOffsetY = 0f
    }

    fun rewind() {
        onRewind.invoke()
    }
}

data class CardSate0<T>(
    private val screenWidth: Float,
    private val screenHeight: Float,
    private val scope: CoroutineScope,
    val data: T,
    val offsetX: Animatable<Float, AnimationVector1D>,
    val offsetY: Animatable<Float, AnimationVector1D>,
    val scaleX: Animatable<Float, AnimationVector1D>,
    val scaleY: Animatable<Float, AnimationVector1D>,
    val rotation: Animatable<Float, AnimationVector1D>,
    val zIndex: Float,
    val animationDuration: Duration,
) {
    val shouldVisible get() = true
    private val center = Offset(0f, 0f)
    private val centerAnimationSpec: AnimationSpec<Float> = SpringSpec()

    private val animationSpec: AnimationSpec<Float> =
        tween(durationMillis = animationDuration.duration)

    private fun returnCenter() = with(scope) {
        launch {
            offsetX.animateTo(center.x, centerAnimationSpec)
        }
        launch {
            offsetY.animateTo(center.y, centerAnimationSpec)
        }
        launch {
            rotation.animateTo(0.0f, centerAnimationSpec)
        }
    }

    fun swipeToward(swipeDirection: Direction) = with(scope) {
        when (swipeDirection) {
            Direction.Left -> {
                launch {
                    offsetX.animateTo(-screenWidth * 1.5f, animationSpec)
                }
            }

            Direction.Right -> {
                launch { offsetX.animateTo(screenWidth * 1.5f, animationSpec) }
            }

            Direction.Top -> {
                launch { offsetY.animateTo(-screenHeight * 1.5f, animationSpec) }
            }

            Direction.Bottom -> {
                launch { offsetY.animateTo(screenHeight * 1.5f, animationSpec) }
            }

            Direction.TopAndLeft -> {
                launch { offsetX.animateTo(-screenWidth * 1.5f, animationSpec) }
                launch { offsetY.animateTo(-screenHeight * 1.5f, animationSpec) }
            }

            Direction.TopAndRight -> {
                launch { offsetX.animateTo(screenWidth * 1.5f, animationSpec) }
                launch { offsetY.animateTo(-screenHeight * 1.5f, animationSpec) }
            }

            Direction.BottomAndLeft -> {
                launch { offsetX.animateTo(-screenWidth * 1.5f, animationSpec) }
                launch { offsetY.animateTo(screenHeight * 1.5f, animationSpec) }
            }

            Direction.BottomAndRight -> {
                launch { offsetX.animateTo(screenWidth * 1.5f, animationSpec) }
                launch { offsetY.animateTo(screenHeight * 1.5f, animationSpec) }
            }

            Direction.None -> {
                returnCenter()
            }

        }
    }

    fun translateTo(translateOffset: Offset) {
        with(scope) {
            launch { offsetX.animateTo(translateOffset.x, animationSpec) }
            launch { offsetY.animateTo(translateOffset.y, animationSpec) }
        }
    }

    fun scaleTo(scaleOffset: Offset) {
        with(scope) {
            launch { scaleX.animateTo(scaleOffset.x, animationSpec) }
            launch { scaleY.animateTo(scaleOffset.y, animationSpec) }
        }
    }

    fun isAnimating(): Boolean {
        return listOf(offsetX, offsetY, scaleX, scaleY, rotation).any { it.isRunning }
    }

    fun snapToRotation(rot: Float) {
        with(scope) {
            launch { rotation.snapTo(rot) }
        }
    }

    fun snapToTranslation(snapOffset: Offset) {
        with(scope) {
            launch { offsetX.snapTo(snapOffset.x) }
            launch { offsetY.snapTo(snapOffset.y) }
        }
    }

}

object Transformations0 {

    fun calculateTranslation(
        index: Int,
        direction: Direction,
        visibleCount: Int,
        stackElevationPx: Float,
    ): Offset {
        var translationX = 0f
        var translationY = 0f
        val translationIndex = if (index <= visibleCount) index else visibleCount
        when (direction) {
            Direction.Top -> {
                translationY = -stackElevationPx * translationIndex
            }

            Direction.Bottom -> {
                translationY = stackElevationPx * translationIndex
            }

            Direction.Left -> {
                translationX = -stackElevationPx * translationIndex
                translationY = 0f
            }

            Direction.Right -> {
                translationX = stackElevationPx * translationIndex
            }

            Direction.TopAndLeft -> {
                translationX = -stackElevationPx * translationIndex
                translationY = -stackElevationPx * translationIndex
            }

            Direction.TopAndRight -> {
                translationX = stackElevationPx * translationIndex
                translationY = -stackElevationPx * translationIndex
            }

            Direction.BottomAndLeft -> {
                translationX = -stackElevationPx * translationIndex
                translationY = stackElevationPx * translationIndex
            }

            Direction.BottomAndRight -> {
                translationX = stackElevationPx * translationIndex
                translationY = stackElevationPx * translationIndex
            }

            else -> {
                translationX = 0f
                translationY = 0f
            }

        }
        return Offset(translationX, translationY)

    }

    fun calculateScale(
        index: Int,
        visibleCount: Int,
        direction: Direction,
        scaleInterval: Float,
    ): Offset {
        val scaleIndex = if (index <= visibleCount) index else visibleCount
        return when (direction) {
            Direction.Top, Direction.Bottom -> Offset(scaleInterval.pow(scaleIndex), 1f)
            Direction.Left, Direction.Right -> Offset(1f, scaleInterval.pow(scaleIndex))
            else -> Offset(1f, 1f)
        }
    }
}