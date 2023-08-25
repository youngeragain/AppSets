package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xcj.app.appsets.server.model.GroupInfo

@Dao
interface GroupInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addGroup(vararg groupInfo: GroupInfo)

    @Query("SELECT * FROM groupinfo where group_id in (:groupId)")
    suspend fun getGroups(vararg groupId: String): List<GroupInfo>

}