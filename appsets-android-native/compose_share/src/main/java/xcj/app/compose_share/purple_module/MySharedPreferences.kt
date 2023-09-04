package xcj.app.compose_share.purple_module

import android.content.SharedPreferences
import xcj.app.core.android.SharedPreferencesDelegate
import xcj.app.core.android.SharedPreferencesHolder

object MySharedPreferences : SharedPreferencesDelegate {
    private lateinit var sharedPreferencesHolder: SharedPreferencesHolder
    override fun getSharedPreferences(): SharedPreferences? {
        if (!MySharedPreferences::sharedPreferencesHolder.isInitialized) {
            sharedPreferencesHolder = SharedPreferencesHolder()
        }
        return sharedPreferencesHolder.getSharedPreferences(
            ModuleConstant.MODULE_SHARED_PREFERENCES_DEFAULT
        )
    }
}