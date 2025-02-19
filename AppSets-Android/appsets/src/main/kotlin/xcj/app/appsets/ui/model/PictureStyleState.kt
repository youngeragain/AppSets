package xcj.app.appsets.ui.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class PictureStyle(
    val lineCount: Int = Int.MAX_VALUE,
    val oneLineCount: Int = 3,
    val lineHeight: Dp
)

fun defaultScreenPictureStyle(): PictureStyle {
    return PictureStyle(lineHeight = 120.dp)
}

class PictureStyleState(
    pictureStyle: PictureStyle = defaultScreenPictureStyle()
) {
    private var pictureStyle: MutableState<PictureStyle> = mutableStateOf(pictureStyle)
    var oneLineCount
        set(value) {
            pictureStyle.value = pictureStyle.value.copy(oneLineCount = value)
        }
        get() = pictureStyle.value.oneLineCount

    var lineHeight
        set(value) {
            pictureStyle.value = pictureStyle.value.copy(lineHeight = value)
        }
        get() = pictureStyle.value.lineHeight

    fun nextStyle() {
        oneLineCount = (1..5).random()
    }
}