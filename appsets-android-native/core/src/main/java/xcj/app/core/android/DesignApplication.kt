package xcj.app.core.android

import android.app.Application
import xcj.app.core.test.Purple

open class DesignApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Purple.bootstrap(this)
    }
}