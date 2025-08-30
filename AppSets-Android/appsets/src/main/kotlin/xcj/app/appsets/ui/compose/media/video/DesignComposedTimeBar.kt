package xcj.app.appsets.ui.compose.media.video

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.Configuration
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.DragInteraction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.C
import androidx.media3.common.util.Util
import androidx.media3.ui.TimeBar
import androidx.media3.ui.TimeBar.OnScrubListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.custom_component.DesignBackButton
import xcj.app.starter.android.util.PurpleLogger
import java.util.Formatter
import java.util.Locale
import java.util.concurrent.CopyOnWriteArraySet

@SuppressLint("UnsafeOptInUsageError", "ViewConstructor")
class DesignComposedTimeBar(
    context: Context,
    private val customNightThemeState: MutableState<Boolean>,
    private val aspectRadioState: MutableState<Boolean>,
) : FrameLayout(context), TimeBar {
    companion object {
        private const val TAG = "DesignTimeBar"
    }

    private var durationState: MutableState<Long> = mutableLongStateOf(0)
    private var positionState: MutableState<Long> = mutableLongStateOf(0)
    private var bufferPositionState: MutableState<Long> = mutableLongStateOf(0)

    private val formatBuilder = StringBuilder()
    private val formatter = Formatter(formatBuilder, Locale.getDefault())

    private val listeners: CopyOnWriteArraySet<OnScrubListener> =
        CopyOnWriteArraySet<OnScrubListener>()

    override fun addListener(listener: OnScrubListener) {
        PurpleLogger.current.d(TAG, "addListener")
        listeners.add(listener)
    }

    override fun removeListener(listener: OnScrubListener) {
        PurpleLogger.current.d(TAG, "removeListener")
        listeners.remove(listener)
    }

    override fun setEnabled(enabled: Boolean) {
        PurpleLogger.current.d(TAG, "setEnabled, enable:$enabled")
    }

    override fun setKeyTimeIncrement(time: Long) {
        PurpleLogger.current.d(TAG, "setKeyTimeIncrement, time:$time")
    }

    override fun setKeyCountIncrement(count: Int) {
        PurpleLogger.current.d(TAG, "setKeyCountIncrement, count:$count")
    }

    override fun setPosition(position: Long) {
        this.positionState.value = position
        PurpleLogger.current.d(TAG, "setPosition, position:$position")
    }

    override fun setBufferedPosition(bufferedPosition: Long) {
        this.bufferPositionState.value = bufferedPosition
        PurpleLogger.current.d(TAG, "setBufferedPosition, bufferedPosition:$bufferedPosition")
    }

    override fun setDuration(duration: Long) {
        this.durationState.value = duration
    }

    override fun getPreferredUpdateDelay(): Long {
        if (durationState.value == 0L || durationState.value == C.TIME_UNSET) {
            return Long.MAX_VALUE
        }
        return 1000
    }

    override fun setAdGroupTimesMs(
        adGroupTimesMs: LongArray?,
        playedAdGroups: BooleanArray?,
        adGroupCount: Int
    ) {
        PurpleLogger.current.d(TAG, "setAdGroupTimesMs")
    }

    init {
        val composeView = ComposeView(context).apply {
            layoutParams = ViewGroup.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
            )
            setContent {
                TimerBarComposeContent()
            }
        }
        addView(composeView)
    }

    @Composable
    fun TimerBarComposeContent() {
        val density = LocalDensity.current
        var boxSize = remember {
            mutableStateOf(IntSize.Zero)
        }
        val bufferBoxWidth = remember {
            derivedStateOf {
                val duration = durationState.value
                val bufferPosition = bufferPositionState.value
                if (duration == 0L || duration == C.TIME_UNSET) {
                    0.dp
                } else {
                    with(density) {
                        val availableWidth = boxSize.value.width
                        val percentage = bufferPosition.toFloat() / duration.toFloat()
                        (availableWidth * percentage).toDp()
                    }
                }
            }
        }
        val bufferBoxWidthAnimateState = animateDpAsState(bufferBoxWidth.value)
        val hapticFeedback = LocalHapticFeedback.current
        Box(
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            val configuration = LocalConfiguration.current
            val overrideModifier =
                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Modifier
                        .shadow(24.dp, MaterialTheme.shapes.extraLarge)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.extraLarge
                        )
                        .fillMaxWidth()
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        )
                } else {
                    Modifier
                        .shadow(24.dp, MaterialTheme.shapes.extraLarge)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.shapes.extraLarge
                        )
                        .width(TextFieldDefaults.MinWidth)
                        .clip(MaterialTheme.shapes.extraLarge)
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            MaterialTheme.shapes.extraLarge
                        )
                }
            Box(
                modifier = overrideModifier
            ) {
                Column(
                    Modifier
                        .clip(MaterialTheme.shapes.extraLarge)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = androidx.compose.ui.Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    CircleShape
                                )
                                .padding(
                                    horizontal = 6.dp,
                                    vertical = 2.dp
                                )
                                .clip(CircleShape)
                        ) {
                            val positionText =
                                Util.getStringForTime(formatBuilder, formatter, positionState.value)
                            val durationText =
                                Util.getStringForTime(formatBuilder, formatter, durationState.value)
                            Text("$positionText | $durationText", fontSize = 8.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Row(
                            modifier = Modifier
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    CircleShape
                                )
                                .clip(CircleShape),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val lightThemeIconId = if (customNightThemeState.value) {
                                xcj.app.compose_share.R.drawable.ic_outline_light_mode_24
                            } else {
                                xcj.app.compose_share.R.drawable.ic_outline_nightlight_24
                            }
                            Icon(
                                modifier = Modifier
                                    .clickable(onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        customNightThemeState.value = !customNightThemeState.value
                                    })
                                    .padding(4.dp),
                                painter = painterResource(id = lightThemeIconId),
                                contentDescription = "change ui mode"
                            )
                            val zoomIconResId = if (aspectRadioState.value) {
                                xcj.app.compose_share.R.drawable.ic_round_close_fullscreen_24
                            } else {
                                xcj.app.compose_share.R.drawable.ic_open_in_full_24px
                            }
                            Icon(
                                modifier = Modifier
                                    .clickable(onClick = {
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        aspectRadioState.value = !aspectRadioState.value
                                    })
                                    .padding(4.dp),
                                painter = painterResource(id = zoomIconResId),
                                contentDescription = "change ui mode"
                            )
                        }
                    }


                    val sliderPosition = remember {
                        mutableFloatStateOf(0f)
                    }
                    LaunchedEffect(positionState.value) {
                        val newSliderPosition =
                            if (durationState.value == 0L || durationState.value == C.TIME_UNSET) {
                                0f
                            } else {
                                positionState.value.toFloat() / durationState.value.toFloat()
                            }
                        if (sliderPosition.floatValue != newSliderPosition) {
                            sliderPosition.floatValue = newSliderPosition
                        }
                    }

                    val coroutineScope = rememberCoroutineScope()

                    val interactionSource: MutableInteractionSource =
                        remember { MutableInteractionSource() }

                    LaunchedEffect(interactionSource) {
                        interactionSource.interactions.collect { interaction ->
                            PurpleLogger.current.d(TAG, "interactions:$interaction ")
                            when (interaction) {
                                is DragInteraction.Start -> {
                                    dispatchScrubStart(coroutineScope, sliderPosition.floatValue)
                                }

                                is DragInteraction.Cancel -> {
                                    dispatchScrubStop(coroutineScope, sliderPosition.floatValue, true)
                                }

                                is DragInteraction.Stop -> {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    dispatchScrubStop(coroutineScope, sliderPosition.floatValue, false)
                                }
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .fillMaxWidth()
                            .onSizeChanged {
                                boxSize.value = it
                            },
                    ) {
                        Spacer(
                            modifier = Modifier
                                .align(Alignment.CenterStart)
                                .background(
                                    MaterialTheme.colorScheme.outline,
                                    CircleShape
                                )
                                .clip(CircleShape)
                                .width(bufferBoxWidthAnimateState.value)
                                .height(44.dp)
                        )
                        Slider(
                            value = sliderPosition.floatValue,
                            interactionSource = interactionSource,
                            onValueChange = { progress ->
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                sliderPosition.floatValue = progress
                                dispatchScrubMove(coroutineScope, progress)
                            }
                        )
                    }


                    val backPressedDispatcherOwner = LocalOnBackPressedDispatcherOwner.current
                    DesignBackButton(modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        backPressedDispatcherOwner?.onBackPressedDispatcher?.onBackPressed()
                    }
                }
            }
        }
    }

    fun dispatchScrubMove(scope: CoroutineScope, progress: Float) {
        val duration = durationState.value
        if (duration == 0L || duration == C.TIME_UNSET) {
            return
        }
        PurpleLogger.current.d(TAG, "dispatchScrubMove, progress:$progress")
        scope.launch {
            listeners.forEach {
                it.onScrubMove(
                    this@DesignComposedTimeBar,
                    (progress * duration).toLong()
                )
            }
        }
    }

    fun dispatchScrubStart(scope: CoroutineScope, progress: Float) {
        val duration = durationState.value
        if (duration == 0L || duration == C.TIME_UNSET) {
            return
        }
        PurpleLogger.current.d(TAG, "dispatchScrubStart, progress:$progress")
        scope.launch {
            listeners.forEach {
                it.onScrubStart(
                    this@DesignComposedTimeBar,
                    (progress * duration).toLong()
                )
            }
        }
    }

    fun dispatchScrubStop(scope: CoroutineScope, progress: Float, cancel: Boolean) {
        val duration = durationState.value
        if (duration == 0L || duration == C.TIME_UNSET) {
            return
        }
        PurpleLogger.current.d(TAG, "dispatchScrubStop, progress:$progress")
        scope.launch {
            listeners.forEach {
                it.onScrubStop(
                    this@DesignComposedTimeBar,
                    (progress * duration).toLong(),
                    cancel
                )
            }
        }
    }
}