package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.entity.PinnedApp
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.util.PurpleLogger

@Dao
interface PinnedAppsDao {

    @Query("select packageName from pinnedapp")
    suspend fun getPinnedApps(): List<String>

    @Query("select packageName from pinnedapp")
    fun getPinnedAppsFlow(): Flow<List<String>>

    @Insert
    suspend fun addPinnedApp(pinnedApp: PinnedApp)

    @Query("delete from pinnedapp where packageName=:appPackageName")
    suspend fun unPinAppByPackageName(appPackageName: String)

    @Delete
    suspend fun unPinApp(app: PinnedApp)

    companion object {

        private const val TAG = "PinnedAppsDaoCompanion"

        fun getInstance(): PinnedAppsDao {
            val dataBase = ModuleHelper.get<AppDatabase>(ModuleConstant.MODULE_NAME + "/database")
            PurpleLogger.current.d(TAG, "getInstance, dataBase:${dataBase}")
            if (dataBase == null) {
                PurpleLogger.current.e(TAG, "getInstance, dataBase is null!!!")
                throw Exception()
            }
            return dataBase.pinnedAppsDao()
        }

    }
}