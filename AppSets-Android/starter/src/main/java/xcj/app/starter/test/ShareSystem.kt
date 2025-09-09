package xcj.app.starter.test

import xcj.app.starter.android.util.PurpleLogger
import java.io.File

object ShareSystem {

    private const val TAG = "ShareSystem"
    const val SHARE_SYSTEM_CLOSE = "appsets/share/system/close"
    private const val MAX_FILE_NAME_TIMES_TO_FIND = 10000

    fun getShareDirPath(): String {
        val path = LocalAndroidContextFileDir.current.appSetsShareDir + File.separator
        return path
    }

    fun makeFileIfNeeded(fileName: String, createFile: Boolean = true): File? {
        val pathPrefix = getShareDirPath()
        val filePath = pathPrefix + fileName
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
                    runCatching {
                        file.createNewFile()
                        file.setWritable(true)
                        return file
                    }.onFailure {
                        it.printStackTrace()
                        PurpleLogger.current.d(
                            TAG,
                            "makeFileIfNeeded, file:${file}, failed:${it.message}"
                        )
                        return null
                    }
                } else {
                    return file
                }
            }
            if (count > MAX_FILE_NAME_TIMES_TO_FIND) {
                return null
            }
            PurpleLogger.current.d(
                TAG,
                "makeFileIfNeeded, file:${file}, exist find next available file..."
            )
            count += 1
            val filePath = pathPrefix + firstNameWithoutExtension + "-$count." + file.extension
            file = File(filePath)
        } while (true)
    }
}