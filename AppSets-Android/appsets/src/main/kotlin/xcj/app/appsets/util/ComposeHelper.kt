package xcj.app.appsets.util

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.PixelCopy
import android.view.View
import androidx.compose.ui.geometry.Rect
import androidx.core.graphics.applyCanvas
import xcj.app.appsets.util.ktx.writeBitmap
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File
import java.util.UUID
import kotlin.math.roundToInt

private const val TAG = "ComposeHelper"

internal fun saveComposeNodeAsBitmap(
    context: Context,
    bounds: Rect,
    view: View
): File {
    val bitmap = Bitmap.createBitmap(
        bounds.width.roundToInt(), bounds.height.roundToInt(),
        Bitmap.Config.ARGB_8888
    )
    val cacheDir = LocalAndroidContextFileDir.current.tempImagesCacheDir
    val fileName = "${UUID.randomUUID()}.png"
    val file = File(cacheDir, fileName)
    file.createNewFile()

    fun saveInternal() {
        runCatching {
            file.writeBitmap(bitmap, Bitmap.CompressFormat.PNG, 85)
            file
        }.onFailure {
            PurpleLogger.current.d(
                TAG,
                "saveInternal, save compose node image failure with name:$fileName"
            )
        }.onSuccess {
            PurpleLogger.current.d(
                TAG,
                "saveInternal, save compose node image success with name:$fileName"
            )

        }
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        // Above Android O, use PixelCopy
        val window = (context as Activity).window
        PixelCopy.request(
            window,
            android.graphics.Rect(
                bounds.left.toInt(),
                bounds.top.toInt(),
                bounds.right.toInt(),
                bounds.bottom.toInt()
            ),
            bitmap,
            {
                if (it == PixelCopy.SUCCESS) {
                    saveInternal()
                } else {
                    PurpleLogger.current.d(
                        TAG,
                        "above o, save compose node image failure with name:$fileName"
                    )
                    null
                }
            },
            Handler(Looper.getMainLooper())
        )
    } else {
        bitmap.applyCanvas(view::draw)
        saveInternal()
    }
    return file
}