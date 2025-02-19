package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.main.model.res.UserScreenRes
import xcj.app.main.model.table.mysql.UserScreen

@Mapper
interface UserScreenDao {

    fun getScreenResPageListByUidPaged(
        uid: String,
        checkReviewResult: Boolean,
        orderByTime: Boolean,
        limit: Int?,
        offset: Int?
    ): List<UserScreenRes>

    fun getIndexRandomUserScreenRes(
        orderByTime: Boolean,
        checkReviewResult: Boolean,
        limit: Int?,
        offset: Int?
    ): List<UserScreenRes>

    fun addScreen(userScreen: UserScreen): Int

    fun deleteScreen(screenId: String): Int

    fun updateAdminReviewResult(screenId: String, systemReviewResult: Int): Int

    fun getScreenByScreenId(screenId: String): UserScreen?

    fun getScreenResByScreenId(screenId: String): UserScreenRes?

    fun updateScreenPublicStatus(screen: UserScreen): Int

    fun searchScreenResByKeywords(keywords: String, limit: Int, offset: Int): List<UserScreenRes>

}