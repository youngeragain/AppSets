package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper

@Mapper
interface UserScreenSystemReviewDao {
    fun addAdminReview(
        reviewUid: String,
        screenId: String,
        reviewResult: Int,
        reviewMessage: String
    ): Int
}