package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper
import xcj.app.main.model.table.mysql.ScreenReview

@Mapper
interface ScreenReviewDao {

    fun getScreenReviewsByScreenId(
        screenId: String,
        checkReviewResult: Boolean,
        orderByTime: Boolean,
        limit: Int?,
        offset: Int?
    ): List<ScreenReview>

    fun addScreenReview(
        reviewId: String,
        content: String,
        screenReviewId: String?,
        reviewUid: String,
        screenId: String,
        reviewPassed: Int,
        isPublic: Int
    ): Int

}