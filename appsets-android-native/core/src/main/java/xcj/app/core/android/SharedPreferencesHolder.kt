package xcj.app.core.android

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SharedPreferencesHolder {

    private var sharedPreferencesMap: MutableMap<String, SharedPreferences?> = mutableMapOf()

    fun getSharedPreferences(name: String): SharedPreferences? {
        return sharedPreferencesMap[name] ?: synchronized(this) {
            ApplicationHelper.application.getSharedPreferences(name, Context.MODE_PRIVATE).also {
                sharedPreferencesMap[name] = it
            }
        }
    }

    fun clear(name: String?) {
        for (m in sharedPreferencesMap) {
            if (!name.isNullOrEmpty() && m.key == name) {
                clearSharedPreferences(m.value)
                return
            } else {
                clearSharedPreferences(m.value)
            }
        }
    }

    private fun clearSharedPreferences(sharedPreferences: SharedPreferences?) {
        sharedPreferences?.apply {
            edit {
                clear()
                //commit to sync
                //apply to async
                commit()
            }
        }
    }
}