package xcj.app.main.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.req.AddScreenReviewParams
import xcj.app.main.model.req.AddUserScreenParams
import xcj.app.main.model.req.AdminReviewScreenParams
import xcj.app.main.model.req.DeleteUserScreenParams
import xcj.app.main.model.res.UserScreenRes
import xcj.app.main.model.table.mysql.ScreenReview
import xcj.app.main.service.UserScreenService

@RequestMapping("/user")
@RestController
class UserScreenController(
    private val userScreenService: UserScreenService
) {
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @RequestMapping("screens/index/recommend", method = [RequestMethod.GET])
    fun getIndexRecommendScreens(
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "pageSize", required = false) pageSize: Int?
    ): DesignResponse<List<UserScreenRes>> {
        return userScreenService.getIndexUserScreens(page, pageSize)
    }

    @ApiDesignPermission.LoginRequired
    @RequestMapping("screens", method = [RequestMethod.GET])
    fun getUserScreensByToken(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "pageSize", required = false) pageSize: Int?
    ): DesignResponse<List<UserScreenRes>> {
        return userScreenService.getUserScreenListByToken(token, page, pageSize)
    }

    @ApiDesignPermission.LoginRequired
    @RequestMapping("screens/{userId}", method = [RequestMethod.GET])
    fun getUserScreensByUid(
        @PathVariable(name = "userId") userId: String,
        @RequestParam(name = "page", required = false) page: Int?,
        @RequestParam(name = "pageSize", required = false) pageSize: Int?
    ): DesignResponse<List<UserScreenRes>> {
        return userScreenService.getUserScreenListByUid(userId, page, pageSize)
    }

    @ApiDesignPermission.LoginRequired
    @RequestMapping("screen", method = [RequestMethod.POST])
    fun addScreen(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody addUserScreenParams: AddUserScreenParams
    ): DesignResponse<Boolean> {
        return userScreenService.addUserScreen(token, addUserScreenParams)
    }

    @ApiDesignPermission.LoginRequired
    @RequestMapping("screen", method = [RequestMethod.DELETE])
    fun deleteScreen(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody deleteUserScreenParams: DeleteUserScreenParams
    ): DesignResponse<Boolean> {
        return userScreenService.deleteUserScreen(token, deleteUserScreenParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("admin/screen/review", method = [RequestMethod.POST])
    fun reviewScreenByAdmin(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody adminReviewScreenParams: AdminReviewScreenParams
    ): DesignResponse<Boolean> {
        return userScreenService.reviewScreenByAdmin(token, adminReviewScreenParams)
    }

    @ApiDesignPermission.LoginRequired
    @GetMapping("screen/status/flip")
    fun flipScreenStatus(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestParam(name = "screen") screenId: String
    ): DesignResponse<Boolean> {
        return userScreenService.flipScreenVisibility(token, screenId)
    }

    @ApiDesignPermission.LoginRequired
    @GetMapping("screen/search")
    fun searchScreenByKeywords(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestParam(name = "keywords") keywords: String,
        @RequestParam(name = "page", required = false) page: Int? = 1,
        @RequestParam(name = "size", required = false) pageSize: Int? = 20,
    ): DesignResponse<List<UserScreenRes>> {
        return userScreenService.searchScreenByKeywords(token, keywords, page, pageSize)
    }

    @ApiDesignPermission.LoginRequired
    @GetMapping("screen/reviews/{screenId}")
    fun getScreenReviews(
        @PathVariable(name = "screenId") screenId: String,
        @RequestParam(name = "page", required = false) page: Int? = 1,
        @RequestParam(name = "size", required = false) pageSize: Int? = 20,
    ): DesignResponse<List<ScreenReview>> {
        return userScreenService.getScreenReviewsByScreenId(screenId, page, pageSize)
    }

    @ApiDesignPermission.LoginRequired
    @PostMapping("screen/review")
    fun addScreenReviews(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody addScreenReviewParams: AddScreenReviewParams
    ): DesignResponse<Boolean> {
        return userScreenService.addScreenReviews(token, addScreenReviewParams)
    }

    @ApiDesignPermission.LoginRequired
    @PostMapping("screen/viewedbyuser/{screenId}")
    fun screenViewedByUser(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "screenId") screenId: String,
    ): DesignResponse<Boolean> {
        return userScreenService.screenViewedByUser(token, screenId)
    }

    @ApiDesignPermission.LoginRequired
    @GetMapping("screen/viewcount/{screenId}")
    fun getScreenViewCount(
        @PathVariable(name = "screenId") screenId: String,
    ): DesignResponse<Int> {
        return userScreenService.getScreenViewCount(screenId)
    }

    @ApiDesignPermission.LoginRequired
    @PostMapping("screen/likeitbyuser/{screenId}")
    fun screenLikeItByUser(
        @RequestParam(name = "count") count: Int,
        @PathVariable(name = "screenId") screenId: String,
    ): DesignResponse<Boolean> {
        return userScreenService.screenLikeItByUser(screenId, count)
    }

    @ApiDesignPermission.LoginRequired
    @GetMapping("screen/likecount/{screenId}")
    fun getScreenLikedCount(
        @PathVariable(name = "screenId") screenId: String,
    ): DesignResponse<Int> {
        return userScreenService.getScreenLikedCount(screenId)
    }

    @ApiDesignPermission.LoginRequired
    @PostMapping("screen/collectbyuser/{screenId}")
    fun screenCollectByUser(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "screenId") screenId: String,
        @RequestParam(name = "category", required = false) category: String?
    ): DesignResponse<Boolean> {
        return userScreenService.screenCollectByUser(token, screenId, category)
    }

    @ApiDesignPermission.LoginRequired
    @PostMapping("screen/collectbyuser/remove/{screenId}")
    fun removeCollectedScreen(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "screenId") screenId: String,
    ): DesignResponse<Boolean> {
        return userScreenService.removeCollectedScreen(token, screenId)
    }

    @ApiDesignPermission.LoginRequired
    @GetMapping("screen/collectbyuser/{screenId}")
    fun isScreenCollectByUser(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "screenId") screenId: String
    ): DesignResponse<Boolean> {
        return userScreenService.isScreenCollectByUser(token, screenId)
    }

    @ApiDesignPermission.LoginRequired
    @PutMapping("screen/publicstate/{screenId}")
    fun changeScreenPublicState(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "screenId") screenId: String,
        @RequestParam(name = "public") isPublic: Boolean
    ): DesignResponse<Boolean> {
        return userScreenService.changeScreenPublicState(token, screenId, isPublic)
    }

    /**
     * 合并浏览screen时多个接口调用
     */
    @ApiDesignPermission.LoginRequired
    @PostMapping("screen/viewit/combineactions/{screenId}")
    fun combineUserViewScreenActions(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @PathVariable(name = "screenId") screenId: String,
        @RequestParam(name = "public") isPublic: Boolean
    ): DesignResponse<Boolean> {
        return DesignResponse(data = false)
    }
}

