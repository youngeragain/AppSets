package xcj.app.core.graphics

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CurveView @JvmOverloads constructor(context: Context, attributeSet: AttributeSet, defStyleAttr:Int=0, defStyleAttrRes:Int=0)
    : View(context, attributeSet, defStyleAttr, defStyleAttrRes) {
    private val curveHolder:CurveHolder = CurveHolder(this)
    init {
        isClickable = true
    }
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        curveHolder.initPoints(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        curveHolder.onDraw(canvas)
    }


    override fun setOnTouchListener(l: OnTouchListener?) {
        super.setOnTouchListener(l)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event?.action==MotionEvent.ACTION_DOWN){
            curveHolder.changePoint(event.x, event.y)
        }else if(event?.action==MotionEvent.ACTION_MOVE){
            curveHolder.pointMove(event.x, event.y)
        }
        return super.onTouchEvent(event)
    }

    fun doAnimate(){
        curveHolder.doAnimate()
    }
}