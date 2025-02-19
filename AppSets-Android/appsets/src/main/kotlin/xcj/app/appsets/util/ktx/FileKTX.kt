package xcj.app.appsets.util.ktx

import android.graphics.Bitmap
import java.io.File

internal fun File.deleteDir(): Boolean {
    if (!this.exists()) {
        return false
    }
    if (this.isDirectory) {
        val children = this.list()
        if (children.isNullOrEmpty()) {
            return false
        }
        for (child in children) {
            val success = File(this, child).deleteDir()
            if (!success) {
                return false
            }
        }
    }
    return this.delete()
}

internal fun File.writeBitmap(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int) {
    outputStream().use { out ->
        bitmap.compress(format, quality, out)
        out.flush()
    }
}