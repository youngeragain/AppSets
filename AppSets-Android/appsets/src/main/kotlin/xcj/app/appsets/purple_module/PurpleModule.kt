package xcj.app.appsets.purple_module

import android.content.Intent
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.starter.android.IPurpleModule
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.ModuleRouter
import xcj.app.starter.android.PurpleModulePageUri
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.Identifiable
import xcj.app.starter.foundation.Provider
import xcj.app.starter.test.AndroidEvent
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.test.LocalPurpleCoroutineScope

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
        val provider = object : Provider<String, AppDatabase> {
            override fun key(): Identifiable<String> {
                return Identifiable.fromString(ModuleConstant.MODULE_NAME)
            }

            override fun provide(): AppDatabase {
                val moduleDatabase = AppDatabase.getRoomDatabase(
                    ModuleConstant.MODULE_DATABASE_NAME,
                    LocalApplication.current,
                    LocalPurpleCoroutineScope.current
                )
                return moduleDatabase
            }
        }

        ModuleHelper.addProvider(provider)
        AppSetsModuleSettings.get().initConfig()
    }

    override fun findSupportPageUri(uri: PurpleModulePageUri): Intent? {
        return null
    }
}