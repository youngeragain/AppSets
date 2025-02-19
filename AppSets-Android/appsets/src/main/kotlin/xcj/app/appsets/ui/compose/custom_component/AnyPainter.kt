package xcj.app.appsets.ui.compose.custom_component

import android.graphics.Bitmap
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.imageResource

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

        else -> {
            ColorPainter(defaultColor)
        }
    }
    return painter
}
