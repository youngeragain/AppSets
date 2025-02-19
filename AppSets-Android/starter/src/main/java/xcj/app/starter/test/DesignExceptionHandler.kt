package xcj.app.starter.test

import kotlinx.coroutines.CoroutineExceptionHandler
import xcj.app.starter.android.util.PurpleLogger
import java.io.File

class DesignExceptionHandler : Thread.UncaughtExceptionHandler {
    companion object {
        private const val TAG = "DesignExceptionHandler"
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        PurpleLogger.current.d(TAG, "$thread throw exception:${throwable.message}")
        throwable.printStackTrace()
        writeExceptionToFile(thread, throwable)
    }

    private fun writeExceptionToFile(thread: Thread?, throwable: Throwable) {
        PurpleLogger.current.d(TAG, "writeExceptionToFile")
        val enable = PurpleLogger.current.enable
        if (enable) {
            return
        }
        val errorsCacheDir =
            LocalAndroidContextFileDir.current.errorsCacheDir
        if (errorsCacheDir.isNullOrEmpty()) {
            return
        }
        val currentTimeMillis = System.currentTimeMillis()
        val file =
            File(errorsCacheDir + File.separator + "exception-" + currentTimeMillis + ".txt")
        if (file.exists()) {
            file.delete()
        } else {
            file.createNewFile()
        }
        if (file.canWrite()) {
            val text = "Thread:${thread}\n${throwable.stackTraceToString()}"
            file.writeText(text)
        }
    }

    val coExceptionHandler: CoroutineExceptionHandler = CoroutineExceptionHandler { coctx, e ->
        PurpleLogger.current.d(
            TAG,
            "Coroutine throw exception:${e.message}"
        )
        e.printStackTrace()
        writeExceptionToFile(null, e)
    }
}
