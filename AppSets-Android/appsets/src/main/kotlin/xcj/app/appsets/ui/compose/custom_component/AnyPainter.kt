package xcj.app.appsets.ui.compose.custom_component

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp

@Composable
fun anyPainter(any: Any?, defaultColor: Color = MaterialTheme.colorScheme.primary): Painter {
    val painter = when (any) {
        is Painter -> {
            any
        }

        is Int -> {
            BitmapPainter(ImageBitmap.imageResource(id = any))
        }

        is ImageBitmap -> {
            BitmapPainter(any)
        }

        is Bitmap -> {
            BitmapPainter(any.asImageBitmap())
        }

        is String -> {
            TextPainter(any.first().toString())
        }

        else -> {
            ColorPainter(defaultColor)
        }
    }
    return painter
}


class TextPainter(
    val text: String,
    val fontSize: TextUnit = 16.sp,
    val color: Color = Color.Black,
    // You can add more text styling options here (e.g., textAlign, letterSpacing)
) : Painter() {

    private val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        color = this@TextPainter.color.toArgb() // Convert Compose Color to Android's integer color
        // Example for alignment, more can be added
        // this.textAlign = Paint.Align.CENTER
    }

    private var calculatedSize: Size = Size.Zero

    override val intrinsicSize: Size
        get() = calculatedSize

    override fun DrawScope.onDraw() {
        // Update textSize here because we need density, which is available in DrawScope
        textPaint.textSize = fontSize.toPx() // Convert sp to pixels

        // Calculate text bounds to determine the intrinsic size if not already done
        // or if parameters change that affect size.
        // For simplicity, this example calculates it once.
        // A more robust solution might recalculate if text/fontSize changes.
        if (calculatedSize == Size.Zero) {
            val bounds = android.graphics.Rect()
            textPaint.getTextBounds(text, 0, text.length, bounds)
            calculatedSize = Size(bounds.width().toFloat(), bounds.height().toFloat())
        }

        // Draw the text onto the canvas
        // Note: drawContext.canvas is a Compose canvas. We need the native Android canvas
        // to use Android's Paint object.
        val nativeCanvas = drawContext.canvas.nativeCanvas

        // Adjust Y position: Android's drawText y-coordinate is the baseline.
        // To draw from top-left, you might need to adjust.
        // For this example, we'll draw from (0, textPaint.textSize) for simplicity,
        // which places the baseline at textPaint.textSize from the top.
        // A more accurate vertical centering or top alignment requires measuring font metrics.
        val yPos = textPaint.textSize // Approximate for drawing from top (baseline at textSize)

        nativeCanvas.drawText(
            text,
            (size.width - calculatedSize.width) / 2, // x-coordinate
            (size.height + calculatedSize.height) / 2, // y-coordinate (baseline)
            textPaint
        )
    }

    // Helper to convert Compose Color to Android's int color
    private fun Color.toArgb(): Int {
        return android.graphics.Color.argb(
            (alpha * 255.0f + 0.5f).toInt(),
            (red * 255.0f + 0.5f).toInt(),
            (green * 255.0f + 0.5f).toInt(),
            (blue * 255.0f + 0.5f).toInt()
        )
    }
}