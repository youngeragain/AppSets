package xcj.app.core.graphics

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.util.Log
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red
import com.google.android.material.animation.AnimatorSetCompat
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import xcj.app.core.foundation.Chain
import kotlin.concurrent.thread
import kotlin.math.sqrt

data class Point(var x:Float, var y:Float):Chain<Point>{
    override var previous: Point? = null
    override var next: Point? = null
}

class CurveHolder(private val view: CurveView) {

    //private val rawPoints:MutableList<Point> = mutableListOf()

    private var times = 2//2次，3个点

    private val usePoints:MutableSet<Point> = mutableSetOf()

    private val curvePoints:MutableMap<Animator, MutableList<Point>> = mutableMapOf()

    val pointEnd:Point = Point(0f, 0f)

    val path =  Path()

    fun initPoints(maxWidth:Int, maxHeight:Int){

    }

    private val usePaint:Paint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
    }
    private val curvePointPaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }


    private val movePointPaint = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true
    }


    fun onDraw(canvas: Canvas) {
        usePoints.forEach {
            canvas.drawCircle(it.x, it.y, 20f, usePaint)
        }
        curvePoints.values.flatten().forEach {
            val random = (0..255).random()
            val red = random.red.toFloat()
            val blue = random.blue.toFloat()
            val green = random.green.toFloat()
            curvePointPaint.setColor(Color.valueOf(red, blue, green).toArgb())
            canvas.drawCircle(it.x, it.y, 4f, curvePointPaint)
        }
        canvas.drawCircle(pointEnd.x, pointEnd.y, 20f, movePointPaint)
        //canvas.drawPath(path, curvePointPaint)
    }

    fun changePoint(x: Float, y: Float) {
        for(p in usePoints){
            if(p.x==x&&p.y==y){
                usePoints.remove(p)
                break
            }
        }
        usePoints.add(Point(x, y))
        view.invalidate()
    }

    fun pointMove(x: Float, y: Float){
        pointEnd.x = x
        pointEnd.y = y
    }



    fun doAnimate(){
        thread {
            while (true){
                runBlocking {
                    delay(16)
                    view.postInvalidate()
                }
            }
        }
        thread {
            while (true){
                runBlocking {
                    if(usePoints.size<=1)
                        return@runBlocking

                    pointEnd.x = usePoints.last().x
                    pointEnd.y = usePoints.last().y

                    val animatorSet = AnimatorSet()

                    val animators = mutableListOf<Animator>()
                    repeat(12){
                        animators.add(getObjectAnimator())
                    }

                    animators.forEach {
                        view.post {
                            it.start()
                        }
                    }
                    delay(100)
                }

            }
        }


    }
    fun getObjectAnimator():ValueAnimator{
        val pointStart = usePoints.first()
        val randomPoints = mutableListOf<Point>()
        repeat(1){
            val randomPoint = getRandomPoint(pointStart, (150..550).random().toFloat())
            randomPoints.add(randomPoint)
        }
        return ObjectAnimator.ofFloat(0f, 1f).apply {
            duration = 800

            addUpdateListener {
                val animatedV = it.animatedValue as Float
                synchronized(curvePoints) {
                    with(curvePoints) {
                        val v = curvePoints.get(this@apply)?: mutableListOf()
                        if(v.isNotEmpty())
                            v.clear()
                        randomPoints.forEach { randomPoint ->
                            val generaCurvePoint =
                                generaCurvePoint(pointStart, randomPoint, pointEnd, animatedV)
                            v.add(generaCurvePoint)
                        }
                        if(!curvePoints.containsKey(this@apply)){
                            curvePoints.put(this@apply, v)
                        }
                    }
                }
            }
        }
    }
    //给定一个点，然后返回一个距离该点半径为radius的随机点
    fun getRandomPoint(point:Point, radius:Float):Point{
        val randomPoint = Point(0f, 0f)
        val randomX = ((point.x-radius.toInt()).toInt()..(point.x+radius.toInt()).toInt()).random()
        val y = sqrt(radius*radius-((randomX-point.x)*(randomX-point.x)))+point.y
        randomPoint.x = randomX.toFloat()
        randomPoint.y = y
        return randomPoint
    }
    fun generaCurvePoint(point1:Point, point2:Point, point3:Point, factor:Float):Point{
        val curvePoint = Point(0f, 0f)
        val k1 = (1-factor)*(1-factor)
        val k2 = -2*(factor*factor)+2*factor
        val k3 = factor*factor
        val x = k1*point1.x+k2*point2.x+k3*point3.x
        val y = k1*point1.y+k2*point2.y+k3*point3.y
        curvePoint.x = x
        curvePoint.y = y
        return curvePoint
    }

}