package xcj.app.starter.test

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.lifecycle.Lifecycle
import xcj.app.starter.android.util.LocalMessenger
import xcj.app.starter.android.util.PurpleLogger
import java.io.File

class AndroidContexts(application: Application) {
    companion object {
        private const val TAG = "AndroidContexts"
        const val MESSAGE_KEY_ON_APP_GO_BACKGROUND = "on_app_go_background"
    }

    private val activities: LinkedHashMap<Activity, Lifecycle.State> = linkedMapOf()
    private val services: MutableList<Context> = mutableListOf()

    init {
        LocalApplication.provide(application)
        LocalActivities.provide(activities)
    }

    fun isApplicationInBackground(): Boolean {
        PurpleLogger.current.d(
            TAG, """
            isApplicationInBackground, activities:${activities}
        """.trimIndent()
        )
        for ((_, state) in activities) {
            if (state == Lifecycle.State.RESUMED) {
                return false
            }
        }
        return true
    }

    fun simpleInit() {
        initDirs()
        initComponentLifecycleCallback()
    }

    private fun initComponentLifecycleCallback() {
        val designAppComponentCallback = DesignAppComponentCallback()
        LocalApplication.current.registerActivityLifecycleCallbacks(designAppComponentCallback)
    }

    private fun initDirs() {
        runCatching {
            PurpleLogger.current.d(
                TAG,
                "initDirs, init cache dir file paths on purple init"
            )
            val parentPath0 = LocalApplication.current.getExternalFilesDir(null)?.path ?: return
            val childPaths0 = listOf(
                "/dynamic_aar",
                "/dynamic_aar/opt"
            )
            createDirsIfNeeded(parentPath0, childPaths0)
            val parentPath1 =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.path
            val childPaths1 = listOf(
                "/AppSets/ContentShare"
            )
            createDirsIfNeeded(parentPath1, childPaths1)
            val childPaths = listOf(
                "/errors", "/logs", "/temp", "/temp/files",
                "/temp/images", "/temp/videos", "/temp/dbs",
                "/temp/audios"
            )
            val parentPath = LocalApplication.current.externalCacheDir?.path
            createDirsIfNeeded(parentPath, childPaths)
            val androidContextFileDir = AndroidContextFileDir(
                dynamicAARDir = parentPath0 + childPaths0[0],
                dynamicAAROPTDir = parentPath0 + childPaths0[1],
                errorsCacheDir = parentPath + childPaths[0],
                logsCacheDir = parentPath + childPaths[1],
                tempCacheDir = parentPath + childPaths[2],
                tempFilesCacheDir = parentPath + childPaths[3],
                tempImagesCacheDir = parentPath + childPaths[4],
                tempVideosCacheDir = parentPath + childPaths[5],
                tempDbsCacheDir = parentPath + childPaths[6],
                tempAudiosCacheDir = parentPath + childPaths[7],
                appSetsShareDir = parentPath1 + childPaths1[0],
            )
            LocalAndroidContextFileDir.provide(androidContextFileDir)
        }
    }

    private fun createDirsIfNeeded(parentPath: String?, childPaths: List<String>) {
        childPaths.forEach {
            val dir = File(parentPath + it)
            if (!dir.exists()) {
                dir.mkdirs()
            }
        }
    }

    private inner class DesignAppComponentCallback : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities[activity] = Lifecycle.State.CREATED
        }

        override fun onActivityStarted(activity: Activity) {
            activities[activity] = Lifecycle.State.STARTED
        }

        override fun onActivityResumed(activity: Activity) {
            activities[activity] = Lifecycle.State.RESUMED
        }

        override fun onActivityPaused(activity: Activity) {
            activities[activity] = Lifecycle.State.CREATED
        }

        override fun onActivityStopped(activity: Activity) {
            activities[activity] = Lifecycle.State.CREATED
        }

        override fun onActivityDestroyed(activity: Activity) {
            activities.remove(activity)
        }

        override fun onActivityPostResumed(activity: Activity) {
            LocalMessenger.post(
                MESSAGE_KEY_ON_APP_GO_BACKGROUND,
                isApplicationInBackground()
            )
        }

        override fun onActivityPostStopped(activity: Activity) {
            LocalMessenger.post(
                MESSAGE_KEY_ON_APP_GO_BACKGROUND,
                isApplicationInBackground()
            )
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }
    }
}