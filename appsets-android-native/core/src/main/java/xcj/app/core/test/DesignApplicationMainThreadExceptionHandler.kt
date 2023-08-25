package xcj.app.core.test

import android.util.Log
import xcj.app.core.android.ApplicationHelper
import java.io.File

class DesignApplicationMainThreadExceptionHandler : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e("DesignApplication", "$thread throw exception:$throwable")
        val errorsCacheDir =
            ApplicationHelper.getContextFileDir().errorsCacheDir
        if (errorsCacheDir.isNotEmpty()) {
            val currentTimeMillis = System.currentTimeMillis()
            val file =
                File(errorsCacheDir + File.separator + "exception-" + currentTimeMillis + ".txt")
            if (file.exists())
                file.delete()
            else
                file.createNewFile()
            if (file.canWrite()) {
                val text = "Thread:${thread}\n${throwable.stackTraceToString()}"
                file.writeText(text)
            }
        }
    }
}
