package xcj.app.module.binder

import android.util.Log
import xcj.app.core.android.ModuleMainEntry
import xcj.app.core.android.ModuleRouter

class ModuleRouter: ModuleRouter, ModuleMainEntry {

    fun initModule() {
        Log.e("blue", "binder initModule")
    }
}