package xcj.app.share.base

import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import java.io.File

object ShareSystem {

    private const val TAG = "ShareSystem"
    const val SHARE_SYSTEM_CLOSE = "appsets/share/system/close"

    fun getShareDirPath(): String {
        val path = LocalAndroidContextFileDir.current.appSetsShareDir + File.separator
        return path
    }

    fun makeFileIfNeeded(fileName: String, createFile: Boolean = true): File? {
        val pathPrefix = getShareDirPath()
        var filePath = pathPrefix + fileName
        var file = File(filePath)
        val firstNameWithoutExtension = file.nameWithoutExtension
        var count = 0
        do {
            if (!file.exists()) {
                PurpleLogger.current.d(
                    TAG,
                    "makeFileIfNeeded, file:${file}, not exist create and use it"
                )
                if (createFile) {
                    file.createNewFile()
                }
                return file
            }
            if (count > 100000) {
                return null
            }
            PurpleLogger.current.d(
                TAG,
                "makeFileIfNeeded, file:${file}, exist find next available file..."
            )
            count += 1
            var filePath = pathPrefix + firstNameWithoutExtension + "-$count." + file.extension
            file = File(filePath)
        } while (true)
        return null
    }
}