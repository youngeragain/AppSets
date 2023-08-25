package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xcj.app.appsets.server.model.UserInfo

@Dao
interface UserInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUserInfo(vararg userInfoRes: UserInfo)

    @Query("select * from userinfo where uid=:uid")
    suspend fun getUserInfoByUid(uid: String): UserInfo?

    @Query("select * from userinfo where uid in (:uids)")
    suspend fun getUserInfoByUids(vararg uids: String): List<UserInfo>?

}