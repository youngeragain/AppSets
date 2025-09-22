package xcj.app.appsets.purple_module

import android.content.Intent
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.starter.android.IPurpleModule
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.ModuleRouter
import xcj.app.starter.android.PurpleModulePageUri
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.AndroidEvent

class PurpleModule : ModuleRouter, IPurpleModule {
    companion object {
        private const val TAG = "PurpleModuleMain"
    }

    fun onAndroidEvent(event: AndroidEvent) {
        if ("onApplicationCreated" == event.name) {
            PurpleLogger.current.d(TAG, "initModule")
            ModuleHelper.moduleInitHooks(this)
        }
    }

    override fun initModule() {
        AppSetsModuleSettings.get().init()
    }

    override fun findSupportPageUri(uri: PurpleModulePageUri): Intent? {
        return null
    }
}