package xcj.app.starter.test

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.os.Environment
import androidx.lifecycle.Lifecycle
import xcj.app.starter.android.util.PurpleLogger
import java.io.File
import kotlin.collections.set

class AndroidContexts(application: Application) {
    companion object {
        private const val TAG = "AndroidContexts"
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

        runCatching {

            val androidContextFileDir = AndroidContextFileDir()

            LocalAndroidContextFileDir.provide(androidContextFileDir)

            PurpleLogger.current.d(
                TAG,
                "simpleInit, init cache dir file paths on purple init"
            )


            val parentPath0 = LocalApplication.current.getExternalFilesDir(null)?.path ?: return
            val childPaths0 = listOf<String>(
                "/dynamic_aar",
                "/dynamic_aar/opt"
            )
            initDirs(parentPath0, childPaths0)
            androidContextFileDir.dynamicAARDir = parentPath0 + childPaths0[0]
            androidContextFileDir.dynamicAAROPTDir = parentPath0 + childPaths0[1]

            val parentPath1 =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)?.path
            val childPaths1 = listOf<String>(
                "/AppSets/ContentShare"
            )
            initDirs(parentPath1, childPaths1)
            androidContextFileDir.appSetsShareDir = parentPath1 + childPaths1[0]

            val childPaths = listOf<String>(
                "/errors", "/logs", "/temp", "/temp/files",
                "/temp/images", "/temp/videos", "/temp/dbs",
                "/temp/audios"
            )
            val parentPath = LocalApplication.current.externalCacheDir?.path
            initDirs(parentPath, childPaths)
            androidContextFileDir.errorsCacheDir = parentPath + childPaths[0]
            androidContextFileDir.logsCacheDir = parentPath + childPaths[1]
            androidContextFileDir.tempCacheDir = parentPath + childPaths[2]
            androidContextFileDir.tempFilesCacheDir = parentPath + childPaths[3]
            androidContextFileDir.tempImagesCacheDir = parentPath + childPaths[4]
            androidContextFileDir.tempVideosCacheDir = parentPath + childPaths[5]
            androidContextFileDir.tempDbsCacheDir = parentPath + childPaths[6]
            androidContextFileDir.tempAudiosCacheDir = parentPath + childPaths[7]
        }
        val designAppComponentCallback = DesignAppComponentCallback()
        LocalApplication.current.registerActivityLifecycleCallbacks(designAppComponentCallback)
    }

    private fun initDirs(parentPath: String?, childPaths: List<String>) {
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

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }
    }
}