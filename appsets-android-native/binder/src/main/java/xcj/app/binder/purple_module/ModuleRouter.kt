package xcj.app.module.binder.purple_context

import android.util.Log
import xcj.app.core.android.ModuleMainEntry
import xcj.app.core.android.ModuleRouter

class ModuleRouter: ModuleRouter, ModuleMainEntry {
    private val TAG = "ModuleRouter"
    fun initModule() {
        Log.e(TAG, "initModule")
    }
}