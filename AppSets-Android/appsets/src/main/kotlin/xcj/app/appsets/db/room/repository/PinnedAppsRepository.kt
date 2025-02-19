package xcj.app.appsets.db.room.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.db.room.dao.PinnedAppsDao
import xcj.app.appsets.db.room.entity.PinnedApp

class PinnedAppsRepository(
    private val pinnedAppsDao: PinnedAppsDao
) {
    suspend fun getAllPinnedApps(): List<String> = withContext(Dispatchers.IO) {
        return@withContext pinnedAppsDao.getPinnedApps()
    }

    fun getAllPinnedAppsLiveData(): LiveData<List<String>> {
        return pinnedAppsDao.getPinnedAppsFlow().asLiveData()
    }

    suspend fun addPinnedApp(pinnedAppPackageName: String?) = withContext(Dispatchers.IO) {
        if (pinnedAppPackageName.isNullOrEmpty()) {
            return@withContext
        }
        pinnedAppsDao.addPinnedApp(PinnedApp(packageName = pinnedAppPackageName))
    }

    suspend fun unPinApp(appPackageName: String) = withContext(Dispatchers.IO) {
        pinnedAppsDao.unPinAppByPackageName(appPackageName)
    }

    companion object {

        private const val TAG = "PinnedAppsRepository"

        fun newInstance(): PinnedAppsRepository {
            return PinnedAppsRepository(
                PinnedAppsDao.getInstance()
            )
        }

    }
}