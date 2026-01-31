package xcj.app.module.binder.purple_module

class ModuleRouter: ModuleRouter, ModuleMainEntry {
    private val TAG = "ModuleRouter"
    fun initModule() {
        PurpleLogger.current.d(TAG, "initModule")
    }
}