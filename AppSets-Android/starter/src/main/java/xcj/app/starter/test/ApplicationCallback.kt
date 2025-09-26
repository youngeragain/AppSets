package xcj.app.starter.test

import android.content.res.Configuration

interface ApplicationCallback {
    fun onTrimMemory(level: Int)

    fun onLowMemory()

    fun onConfigurationChanged(newConfig: Configuration)
}