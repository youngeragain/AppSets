package xcj.app.main.service

import xcj.app.DesignResponse
import xcj.app.main.model.req.AddScreenReviewParams
import xcj.app.main.model.req.AddUserScreenParams
import xcj.app.main.model.req.AdminReviewScreenParams
import xcj.app.main.model.req.DeleteUserScreenParams
import xcj.app.main.model.res.UserScreenRes
import xcj.app.main.model.table.mysql.ScreenReview

interface UserScreenService {

    fun getUserScreenListByUid(uid: String, page: Int?, pageSize: Int?): DesignResponse<List<UserScreenRes>>

    fun getUserScreenListByToken(token: String, page: Int?, pageSize: Int?): DesignResponse<List<UserScreenRes>>

    fun getIndexUserScreens(page: Int?, pageSize: Int?): DesignResponse<List<UserScreenRes>>

    fun addUserScreen(token: String, addUseScreenParams: AddUserScreenParams): DesignResponse<Boolean>

    fun deleteUserScreen(token: String, deleteUserScreenParams: DeleteUserScreenParams): DesignResponse<Boolean>

    fun deleteUserScreenLogic(token: String, deleteUserScreenParams: DeleteUserScreenParams): DesignResponse<Boolean>

    fun reviewScreenByAdmin(token: String, adminReviewScreenParams: AdminReviewScreenParams): DesignResponse<Boolean>

    fun flipScreenVisibility(token: String, screenId: String): DesignResponse<Boolean>

    fun searchScreenByKeywords(
        token: String,
        keywords: String,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<List<UserScreenRes>>

    fun getScreenReviewsByScreenId(screenId: String, page: Int?, pageSize: Int?): DesignResponse<List<ScreenReview>>

    fun addScreenReviews(token: String, addScreenReviewParams: AddScreenReviewParams): DesignResponse<Boolean>

    fun screenViewedByUser(token: String?, screenId: String): DesignResponse<Boolean>

    fun screenLikeItByUser(screenId: String, count: Int): DesignResponse<Boolean>

    fun screenCollectByUser(token: String, screenId: String, category: String?): DesignResponse<Boolean>

    fun changeScreenPublicState(token: String, screenId: String, isPublic: Boolean): DesignResponse<Boolean>

    fun isScreenCollectByUser(token: String, screenId: String): DesignResponse<Boolean>

    fun getScreenViewCount(screenId: String): DesignResponse<Int>

    fun getScreenLikedCount(screenId: String): DesignResponse<Int>

    fun removeCollectedScreen(token: String, screenId: String): DesignResponse<Boolean>

}