package xcj.app.appsets.ui.nonecompose.ui.common

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout

class SquareConstrainLayout @JvmOverloads constructor(
    context: Context,
    attributes: AttributeSet?,
    defStyleAttr: Int = 0,
    defStyleRes:Int = 0,
): ConstraintLayout(context, attributes, defStyleAttr, defStyleRes) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = getDefaultSize(0, widthMeasureSpec)
        val height = getDefaultSize(0, heightMeasureSpec)
        setMeasuredDimension(width, height)
        val widthMeasureSpec1= MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)
        super.onMeasure(widthMeasureSpec1, widthMeasureSpec1)
    }
}