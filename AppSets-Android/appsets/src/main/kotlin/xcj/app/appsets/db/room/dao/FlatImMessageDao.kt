package xcj.app.appsets.db.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.entity.FlatImMessage
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.starter.android.ModuleHelper
import xcj.app.starter.android.util.PurpleLogger

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

    companion object {

        private const val TAG = "FlatImMessageDao"

        fun getInstance(): FlatImMessageDao {
            val dataBase = ModuleHelper.get<AppDatabase>(ModuleConstant.MODULE_NAME + "/database")
            PurpleLogger.current.d(TAG, "getInstance, dataBase:${dataBase}")
            if (dataBase == null) {
                PurpleLogger.current.e(TAG, "getInstance, dataBase is null!!!")
                throw Exception()
            }
            return dataBase.flatImMessageDao()
        }

    }
}