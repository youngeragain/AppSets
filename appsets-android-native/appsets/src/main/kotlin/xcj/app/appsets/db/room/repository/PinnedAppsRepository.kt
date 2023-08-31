package xcj.app.appsets.db.room.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import xcj.app.appsets.db.room.dao.PinnedAppsDao
import xcj.app.appsets.db.room.entity.PinnedApp

class PinnedAppsRepository(
    private var pinnedAppsDao: PinnedAppsDao?
){
    suspend fun getAllPinnedApps():List<String>{
        return pinnedAppsDao?.getPinnedApps() ?: emptyList()
    }
    fun getAllPinnedAppsLiveData(): LiveData<List<String>> {
        return pinnedAppsDao?.getPinnedAppsFlow()?.asLiveData() ?: MutableLiveData()
    }

    suspend fun addPinnedApp(pinnedAppPackageName: String?) {
        if(pinnedAppPackageName.isNullOrEmpty())
            return
        pinnedAppsDao?.addPinnedApp(PinnedApp(packageName = pinnedAppPackageName))
    }

    suspend fun unPinApp(appPackageName: String) {
        pinnedAppsDao?.unPinAppByPackageName(appPackageName)
    }
}