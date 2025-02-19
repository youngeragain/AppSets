package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.util.PurpleLogger

@Dao
interface GroupInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGroup(vararg groupInfo: GroupInfo)

    @Query("SELECT * FROM groupinfo where group_id in (:groupId)")
    suspend fun getGroups(vararg groupId: String): List<GroupInfo>?

    companion object {

        private const val TAG = "GroupInfoDao"

        fun getInstance(): GroupInfoDao {
            val dataBase = ModuleHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            PurpleLogger.current.d(TAG, "getInstance, dataBase:${dataBase}")
            if (dataBase == null) {
                PurpleLogger.current.e(TAG, "getInstance, dataBase is null!!!")
                throw Exception()
            }
            return dataBase.groupInfoDao()
        }

    }

}