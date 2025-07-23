package xcj.app.screen_share.ui.compose.float_home

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlin.math.roundToInt

class FloatWindowHomeViewState {
    var showTimes = 0

    var isAnimate: Boolean = false

    var isViewAdded: Boolean = false

    private val _isShowingState: MutableLiveData<Boolean> = MutableLiveData()
    val isShowingState: LiveData<Boolean> = _isShowingState


    private val _windowOffset: MutableLiveData<IntOffset> = MutableLiveData()
    val windowOffset: LiveData<IntOffset> = _windowOffset

    private val _widthAndHeightState: MutableLiveData<Pair<Float, Float>> =
        MutableLiveData(0f to 0f)
    val widthAndHeightState: LiveData<Pair<Float, Float>> = _widthAndHeightState

    val isShowing: Boolean
        get() = _isShowingState.value ?: false
    val windowX: Int
        get() = _windowOffset.value?.x ?: 0
    val windowY: Int
        get() = _windowOffset.value?.y ?: 0
    val width: Float
        get() = _widthAndHeightState.value?.first ?: 0f
    val height: Float
        get() = _widthAndHeightState.value?.second ?: 0f

    fun hide(withAnimation: Boolean) {
        isAnimate = withAnimation
        _isShowingState.value = false
    }

    fun show(withAnimation: Boolean) {
        showTimes += 1
        if (showTimes > 1) {
            isAnimate = withAnimation
        }
        _isShowingState.value = true
    }

    fun updateWindowOffset(offset: Offset) {
        var (x, y) = windowOffset.value ?: IntOffset(0, 0)
        x += offset.x.roundToInt()
        y += offset.y.roundToInt()
        _windowOffset.value = IntOffset(x, y)
    }

    fun resetOffset() {
        _windowOffset.value = IntOffset.Zero
    }
}