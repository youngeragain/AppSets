package xcj.app.core.android

import android.content.SharedPreferences
import androidx.core.content.edit

interface SharedPreferencesDelegate{

    fun getSharedPreferences(): SharedPreferences?

    fun remove(key: String) {
        getSharedPreferences()?.apply {
            edit {
                remove(key)
                apply()
            }
        }
    }

    fun putString(key: String, value: String) {
        getSharedPreferences()?.apply {
            edit {
                putString(key, value)
                apply()
            }
        }
    }

    fun clear() {
        getSharedPreferences()?.apply {
            edit {
                clear()
                //commit to sync
                //apply to async
                commit()
            }
        }
    }
    fun putInt(key: String, value: Int){
        getSharedPreferences()?.apply {
            edit {
                putInt(key, value)
                apply()
            }
        }
    }
    fun putBoolean(key: String, value: Boolean){
        getSharedPreferences()?.apply {
            edit{
                putBoolean(key, value)
                apply()
            }
        }
    }

    fun getString(key: String): String? {
        return getSharedPreferences()?.getString(key, null)
    }

    fun getInt(key: String):Int {
        return getSharedPreferences()?.getInt(key, -1)?:-1
    }

    fun getBoolean(key: String):Boolean {
        return getSharedPreferences()?.getBoolean(key, false)?:false
    }
}