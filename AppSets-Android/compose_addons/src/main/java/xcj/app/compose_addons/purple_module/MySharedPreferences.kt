package xcj.app.compose_addons.purple_module

import android.content.Context
import android.content.SharedPreferences
import xcj.app.starter.android.SharedPreferencesInterface
import xcj.app.starter.test.LocalApplication

object MySharedPreferences : SharedPreferencesInterface {
    private lateinit var sharedPreferences: SharedPreferences

    override fun getSharedPreferences(): SharedPreferences {
        if (!::sharedPreferences.isInitialized) {
            sharedPreferences =
                LocalApplication.current.getSharedPreferences(
                    ModuleConstant.MODULE_SHARED_PREFERENCES_DEFAULT, Context.MODE_PRIVATE
                )
        }
        return sharedPreferences
    }

}
