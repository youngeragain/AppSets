package xcj.app.starter.util

import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import androidx.core.graphics.createBitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.ktx.dp
import java.util.EnumMap

object QrCodeUtil {
    suspend fun encodeAsBitmap(content: String): Bitmap? = withContext(Dispatchers.Default) {
        if (content.length > 512) {
            return@withContext null
        }
        val hints: MutableMap<EncodeHintType?, Any?> = EnumMap(EncodeHintType::class.java)
        hints[EncodeHintType.CHARACTER_SET] = Charsets.UTF_8.name()
        val result: BitMatrix? = runCatching {
            val dimension = 150.dp()
            MultiFormatWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                dimension,
                dimension,
                hints
            )
        }.getOrNull()
        if (result == null) {
            return@withContext null
        }
        val width = result.width
        val height = result.height
        val pixels = IntArray(width * height)
        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (result[x, y]) BLACK else WHITE
            }
        }
        val bitmap = createBitmap(width, height, Bitmap.Config.ARGB_4444)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return@withContext bitmap
    }
}