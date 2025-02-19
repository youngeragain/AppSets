package xcj.app.starter.android

import android.app.Application
import android.content.res.Configuration
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.Purple

open class DesignApplication : Application() {
    companion object {
        private const val TAG = "DesignApplication"
    }

    override fun onCreate() {
        super.onCreate()
        Purple().bootstrap(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        PurpleLogger.current.d(TAG, "onTrimMemory, level:$level")
    }

    override fun onLowMemory() {
        super.onLowMemory()
        PurpleLogger.current.d(TAG, "onLowMemory")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        PurpleLogger.current.d(TAG, "onConfigurationChanged, newConfig:$newConfig")
    }
}