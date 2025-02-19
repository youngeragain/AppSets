package xcj.app.appsets.purple_module

import android.content.Intent
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.settings.AppSettings
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.ModuleRouter
import xcj.app.starter.android.IPurpleModuleMain
import xcj.app.starter.android.PurpleModulePageUri
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.test.LocalPurpleCoroutineScope

class PurpleModuleMain : ModuleRouter, IPurpleModuleMain {
    companion object {
        private const val TAG = "PurpleModuleMain"
    }

    fun initModule() {
        PurpleLogger.current.d(TAG, "initModule")
        if (ModuleHelper.isModuleInit(ModuleConstant.MODULE_NAME)) {
            return
        }
        val appDatabase = AppDatabase.getRoomDatabase(
            ModuleConstant.MODULE_DATABASE_NAME,
            LocalApplication.current,
            LocalPurpleCoroutineScope.current
        )
        ModuleHelper.addDataBase(ModuleConstant.MODULE_NAME, appDatabase)
        ModuleHelper.moduleInit(ModuleConstant.MODULE_NAME, this)
        AppSettings.initAppConfig()
    }

    override fun findSupportPageUri(uri: PurpleModulePageUri): Intent? {
        return null
    }
}