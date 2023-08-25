package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import xcj.app.appsets.db.room.entity.UserRelation


@Dao
interface UserRelationDao {
    @Query("select * from userrelation")
    suspend fun getRelationList(): List<UserRelation>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUserRelation(vararg userRelation: UserRelation)
}