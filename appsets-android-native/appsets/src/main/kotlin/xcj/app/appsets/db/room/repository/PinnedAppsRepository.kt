package xcj.app.appsets.db.room.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import xcj.app.appsets.db.room.dao.PinnedAppsDao
import xcj.app.appsets.db.room.entity.PinnedApp

class PinnedAppsRepository(
    private val pinnedAppsDao: PinnedAppsDao
){
    suspend fun getAllPinnedApps():List<String>{
        return pinnedAppsDao.getPinnedApps()
    }
    fun getAllPinnedAppsLiveData(): LiveData<List<String>> {
        return pinnedAppsDao.getPinnedAppsFlow().asLiveData()
    }

    suspend fun addPinnedApp(pinnedAppPackageName: String?) {
        if(pinnedAppPackageName.isNullOrEmpty())
            return
        pinnedAppsDao.addPinnedApp(PinnedApp(packageName = pinnedAppPackageName))
    }

    suspend fun unPinApp(appPackageName: String) {
        pinnedAppsDao.unPinAppByPackageName(appPackageName)
    }
}