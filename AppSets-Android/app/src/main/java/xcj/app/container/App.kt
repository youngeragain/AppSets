package xcj.app.container

import xcj.app.appsets.settings.ModuleConfig
import xcj.app.starter.android.DesignApplication

class App : DesignApplication() {

    override fun onCreate() {
        super.onCreate()
    }

    override fun purpleBootstrap(isTest: Boolean) {
        val isTestOverride = ModuleConfig.isTest
        super.purpleBootstrap(isTestOverride)
    }
}

