package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import xcj.app.main.model.table.mysql.UserScreenCollect

@Mapper
interface UserScreenCollectDao {

    fun addUserScreenCollect(uid: String, screenId: String, category: String?): Int

    @Select("select count(1) from user_screen_collect_2022 where uid=#{uid} and screen_id=#{screenId} limit 1")
    fun isUserScreenCollectByUser(uid: String, screenId: String): Int

    fun removeCollectScreenByUidAndScreenId(uid: String, screenId: String): Int

    @Select("select uid, screen_id, collect_time, category from user_screen_collect_2022 where uid=#{uid} and screen_id=#{screenId} limit 1")
    fun getUserScreenCollectByUidAndScreenId(uid: String, screenId: String): UserScreenCollect?

}