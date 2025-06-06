package xcj.app.container

import xcj.app.appsets.settings.AppConfig
import xcj.app.starter.android.DesignApplication
import xcj.app.starter.android.util.PurpleLogger

class App : DesignApplication() {

    override fun onCreate() {
        PurpleLogger.current.enable = AppConfig.isTest
        super.onCreate()
    }
}

