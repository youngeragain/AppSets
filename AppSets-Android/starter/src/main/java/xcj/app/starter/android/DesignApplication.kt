package xcj.app.starter.android

import android.app.Application
import android.content.res.Configuration
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalPurple
import xcj.app.starter.test.Purple

open class DesignApplication : Application() {
    companion object {
        private const val TAG = "DesignApplication"
    }

    override fun onCreate() {
        super.onCreate()
        purpleBootstrap(false)
    }

    open fun purpleBootstrap(isTest: Boolean) {
        PurpleLogger.current.enable = isTest
        Purple().bootstrap(this)
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        LocalPurple.current.onTrimMemory(level)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        LocalPurple.current.onLowMemory()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocalPurple.current.onConfigurationChanged(newConfig)
    }
}