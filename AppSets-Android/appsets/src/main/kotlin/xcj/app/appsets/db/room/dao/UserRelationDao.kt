package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.entity.UserRelation
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.util.PurpleLogger

@Dao
interface UserRelationDao {

    @Query("select * from userrelation")
    suspend fun getRelationList(): List<UserRelation>?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUserRelation(vararg userRelation: UserRelation)

    companion object {

        private const val TAG = "UserRelationDao"

        fun getInstance(): UserRelationDao {
            val dataBase = ModuleHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            PurpleLogger.current.d(TAG, "getInstance, dataBase:${dataBase}")
            if (dataBase == null) {
                PurpleLogger.current.e(TAG, "getInstance, dataBase is null!")
                throw Exception()
            }
            return dataBase.userRelationDao()
        }
    }
}