package xcj.app.core.test

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import java.io.File


class AndroidContextFileDir {
    lateinit var dynamicAARDir: String
    lateinit var dynamicAAROPTDir: String
    lateinit var errorsCacheDir: String
    lateinit var logsCacheDir: String
    lateinit var tempCacheDir: String
    lateinit var tempFilesCacheDir: String
    lateinit var tempImagesCacheDir: String
    lateinit var tempVideosCacheDir: String
    lateinit var tempDbsCacheDir: String
    lateinit var tempAudiosCacheDir: String
    fun cleanCaches() {
        kotlin.runCatching {
            listOf(
                tempFilesCacheDir,
                tempImagesCacheDir,
                tempVideosCacheDir,
                tempDbsCacheDir,
                tempAudiosCacheDir
            ).forEach {
                val file = File(it)
                if (file.exists() && file.isDirectory) {
                    file.listFiles()?.forEach(File::delete)
                }
            }
        }
    }
}

class AndroidContexts(val application: Application) {
    private val androidContextFileDir = AndroidContextFileDir()
    private val activities: MutableList<ActivityWrapper> = mutableListOf()
    private val services: MutableList<Context> = mutableListOf()
    fun getTopActivityContext(): Context? {
        return activities.lastOrNull()?.activity
    }

    fun isApplicationInBackground(): Boolean {
        for (activityWrapper in activities) {
            if (activityWrapper.state != "Lifecycle.State.STOPPED") {
                return false
            }
        }
        return true
    }

    fun getContextFileDir(): AndroidContextFileDir {
        return androidContextFileDir
    }

    fun simpleInit() {
        kotlin.runCatching {
            Log.e("AndroidContexts", "simpleInit, init cache dir file paths on purple init")

            val parentPath0 = application.getExternalFilesDir(null)?.path
            val childPaths0 = listOf<String>(
                "/dynamic_aar",
                "/dynamic_aar/opt"
            )
            initDirs(parentPath0, childPaths0)
            androidContextFileDir.dynamicAARDir = parentPath0 + childPaths0[0]
            androidContextFileDir.dynamicAAROPTDir = parentPath0 + childPaths0[1]

            val childPaths = listOf<String>(
                "/errors", "/logs", "/temp", "/temp/files",
                "/temp/images", "/temp/videos", "/temp/dbs", "/temp/audios"
            )
            val parentPath = application.externalCacheDir?.path
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
        application.registerActivityLifecycleCallbacks(designAppComponentCallback)
    }

    private fun initDirs(parentPath: String?, childPaths: List<String>) {
        childPaths.forEach {
            val dir = File(parentPath + it)
            if (!dir.exists())
                dir.mkdirs()
        }
    }


    class ActivityWrapper(val activity: Activity, var state: String)
    inner class DesignAppComponentCallback : Application.ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activities.add(ActivityWrapper(activity, "Lifecycle.State.CREATED"))
        }

        override fun onActivityStarted(activity: Activity) {
            activities.firstOrNull { it.activity == activity }?.state = "Lifecycle.State.STARTED"
        }

        override fun onActivityResumed(activity: Activity) {
            activities.firstOrNull { it.activity == activity }?.state = "Lifecycle.State.RESUMED"
        }

        override fun onActivityPaused(activity: Activity) {
            activities.firstOrNull { it.activity == activity }?.state = "Lifecycle.State.PAUSED"
        }

        override fun onActivityStopped(activity: Activity) {
            activities.firstOrNull { it.activity == activity }?.state = "Lifecycle.State.STOPPED"
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

        }

        override fun onActivityDestroyed(activity: Activity) {
            activities.removeIf { it.activity == activity }
        }
    }
}