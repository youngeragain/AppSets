package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xcj.app.appsets.db.room.entity.FlatImMessage

@Dao
interface FlatImMessageDao {
    @Insert
    suspend fun addFlatImMessage(flatImMessage: FlatImMessage)

    /**
     * 查询用户发的所有消息
     * @param uid 消息发送的主体用户
     * @param masterUid 当前请求加载会话的使用者
     */
    @Query("select * from flatimmessage where uid=:uid or (uid=:masterUid and toId=:uid) order by timestamp desc limit :limit offset :offset")
    suspend fun getFlatImMessageByUid(
        uid: String,
        masterUid: String,
        limit: Int,
        offset: Int
    ): List<FlatImMessage>

    @Query("select * from flatimmessage where toId=:groupId order by timestamp desc limit :limit offset :offset")
    suspend fun getFlatImMessageByGroupId(
        groupId: String,
        limit: Int,
        offset: Int
    ): List<FlatImMessage>
}