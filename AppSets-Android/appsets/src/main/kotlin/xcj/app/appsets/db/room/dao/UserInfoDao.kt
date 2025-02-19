package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.server.model.UserInfo
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.util.PurpleLogger

@Dao
interface UserInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUserInfo(vararg userInfo: UserInfo)

    @Query("select * from userinfo where uid=:uid")
    suspend fun getUserInfoByUid(uid: String): UserInfo?

    @Query("select * from userinfo where uid in (:uids)")
    suspend fun getUserInfoByUids(vararg uids: String): List<UserInfo>?

    companion object {

        private const val TAG = "UserInfoDao"

        fun getInstance(): UserInfoDao {
            val dataBase = ModuleHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            PurpleLogger.current.d(TAG, "getInstance, dataBase:${dataBase}")
            if (dataBase == null) {
                PurpleLogger.current.e(TAG, "getInstance, dataBase is null!!!")
                throw Exception()
            }
            return dataBase.userInfoDao()
        }

    }

}