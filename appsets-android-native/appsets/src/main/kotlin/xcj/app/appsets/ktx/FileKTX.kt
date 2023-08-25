package xcj.app.appsets.ktx

import java.io.File

internal fun File.deleteDir(): Boolean {
    if(!this.exists())
        return false
    if (this.isDirectory) {
        val children = this.list()
        if (children.isNullOrEmpty())
            return false
        for (child in children) {
            val success = File(this, child).deleteDir()
            if (!success) {
                return false
            }
        }
    }
    return this.delete()
}