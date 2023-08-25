package xcj.app.userinfo.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.userinfo.model.res.UserScreenInfoRes
import xcj.app.userinfo.model.table.mysql.UserScreen

@Mapper
interface UserScreenDao {
    fun getScreenPageListByUidPaged(
        uid:String,
        checkReviewResult:Boolean,
        orderByTime:Boolean,
        limit:Int?,
        offset: Int?):List<UserScreenInfoRes>

    fun getIndexRandomUserScreen(
        orderByTime: Boolean,
        checkReviewResult: Boolean,
        limit: Int?,
        offset: Int?
    ):List<UserScreenInfoRes>

    fun addScreen(userScreen: UserScreen):Int

    fun deleteScreen(screenId: String):Int
    fun updateAdminReviewResult(screenId:String, systemReviewResult: Int):Int

    fun getScreenByScreenId(screenId: String):UserScreen?
    fun updateScreenPublicStatus(screen: UserScreen):Int
    fun searchScreenByKeywords(keywords: String, limit:Int, offset:Int): List<UserScreenInfoRes>
}