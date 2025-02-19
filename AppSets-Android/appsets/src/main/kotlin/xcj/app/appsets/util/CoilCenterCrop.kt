package xcj.app.appsets.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import coil3.size.Size
import coil3.size.pxOrElse
import coil3.transform.Transformation
import java.util.concurrent.locks.ReentrantLock

class CoilCenterCrop() : Transformation() {
    override val cacheKey: String
        get() = javaClass.name

    override suspend fun transform(input: Bitmap, size: Size): Bitmap {
        return centerCrop(input, size.width.pxOrElse { 0 }, size.height.pxOrElse { 0 })
    }

    companion object {

        private const val PAINT_FLAGS = Paint.DITHER_FLAG or Paint.FILTER_BITMAP_FLAG
        private val BITMAP_DRAWABLE_LOCK = ReentrantLock()
        private val DEFAULT_PAINT = Paint(PAINT_FLAGS)


        fun centerCrop(
            inBitmap: Bitmap, width: Int, height: Int
        ): Bitmap {
            if (inBitmap.width == width && inBitmap.height == height) {
                return inBitmap
            }
            // From ImageView/Bitmap.createScaledBitmap.
            val scale: Float
            val dx: Float
            val dy: Float
            val m = Matrix()
            if (inBitmap.width * height > width * inBitmap.height) {
                scale = height.toFloat() / inBitmap.height.toFloat()
                dx = (width - inBitmap.width * scale) * 0.5f
                dy = 0f
            } else {
                scale = width.toFloat() / inBitmap.width.toFloat()
                dx = 0f
                dy = (height - inBitmap.height * scale) * 0.5f
            }
            m.setScale(scale, scale)
            m.postTranslate((dx + 0.5f).toInt().toFloat(), (dy + 0.5f).toInt().toFloat())
            val result = Bitmap.createBitmap(width, height, getNonNullConfig(inBitmap))
            // We don't add or remove alpha, so keep the alpha setting of the Bitmap we were given.
            setAlpha(inBitmap, result)
            applyMatrix(inBitmap, result, m)
            return result
        }


        private fun applyMatrix(
            inBitmap: Bitmap, targetBitmap: Bitmap, matrix: Matrix
        ) {
            BITMAP_DRAWABLE_LOCK.lock()
            try {
                val canvas = Canvas(targetBitmap)
                canvas.drawBitmap(inBitmap, matrix, DEFAULT_PAINT)
                clear(canvas)
            } finally {
                BITMAP_DRAWABLE_LOCK.unlock()
            }
        }

        private fun clear(canvas: Canvas) {
            canvas.setBitmap(null)
        }

        fun setAlpha(inBitmap: Bitmap, outBitmap: Bitmap) {
            outBitmap.setHasAlpha(inBitmap.hasAlpha())
        }

        private fun getNonNullConfig(bitmap: Bitmap): Bitmap.Config {
            return bitmap.config ?: Bitmap.Config.ARGB_8888
        }
    }
}