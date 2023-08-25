package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import xcj.app.appsets.db.room.entity.PinnedApp

@Dao
interface PinnedAppsDao{
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
}