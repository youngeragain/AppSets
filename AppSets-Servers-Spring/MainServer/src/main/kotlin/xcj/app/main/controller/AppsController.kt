package xcj.app.main.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.req.AddAppParams
import xcj.app.main.model.req.UpdateAppParams
import xcj.app.main.model.res.AppsWithCategory
import xcj.app.main.model.table.mongo.Application
import xcj.app.main.service.mongo.AppService

@RequestMapping("/appsets")
@RestController
class AppsController(
    private val mongoAppServiceImpl: AppService
) {
    @RequestMapping("apps/{page}/{pageSize}")
    fun getAllApplicationsPaged(
        @PathVariable(name = "page") page: Int,
        @PathVariable(name = "pageSize", required = false) pageSize: Int?
    ): DesignResponse<List<Application>> {
        return mongoAppServiceImpl.getAllApplicationsPaged(page, pageSize ?: 20)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @RequestMapping("apps/index/recommend")
    fun getIndexApplications(): DesignResponse<List<AppsWithCategory>?> {
        return mongoAppServiceImpl.getIndexApplications()
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @RequestMapping("apps/index/recommend_v2")
    fun getIndexApplicationsV2(): DesignResponse<List<AppsWithCategory>?> {
        return mongoAppServiceImpl.getIndexApplicationsV2()
    }

    @ApiDesignPermission.UserRoleRequired(
        andRoles = [ApiDesignPermission.UserRoleRequired.ROLE_DEVELOPER],
        orRoles = []
    )
    @ApiDesignPermission.LoginRequired
    @RequestMapping("app", method = [RequestMethod.POST])
    fun addApplicationByDeveloper(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody addAppParams: AddAppParams
    ): DesignResponse<Boolean> {
        return mongoAppServiceImpl.addApplicationByDeveloper(token, addAppParams)
    }


    @ApiDesignPermission.LoginRequired
    @PostMapping("app/create")
    fun createAppByUser(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody createAppParams: Map<String, Any?>
    ): DesignResponse<Boolean> {
        return mongoAppServiceImpl.createApplicationByUser(token, createAppParams)
    }

    @ApiDesignPermission.LoginRequired
    @GetMapping("app/create/precheck")
    fun createApplicationPreCheckByUser(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestParam("appName") appName: String
    ): DesignResponse<Boolean> {
        return mongoAppServiceImpl.createApplicationPreCheckByUser(token, appName)
    }

    @ApiDesignPermission.UserRoleRequired(
        andRoles = [ApiDesignPermission.UserRoleRequired.ROLE_DEVELOPER],
        orRoles = []
    )
    @ApiDesignPermission.LoginRequired
    @RequestMapping("app", method = [RequestMethod.PUT])
    fun updateApplicationByDeveloper(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody updateAppParams: UpdateAppParams
    ): DesignResponse<Boolean> {
        return mongoAppServiceImpl.updateApplicationByDeveloper(token, updateAppParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.SpecialRequired(ApiDesignKeys.APP_TOKEN_MD5)
    @GetMapping("application/search")
    fun searchApplicationByKeywords(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestParam(name = "keywords") keywords: String,
        @RequestParam(name = "page", required = false) page: Int? = 1,
        @RequestParam(name = "size", required = false) pageSize: Int? = 20,
    ): DesignResponse<List<Application>?> {
        return mongoAppServiceImpl.searchApplicationsByKeywords(keywords, page, pageSize)
    }

    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.LoginRequired
    @GetMapping("application/user/{uid}")
    fun getUsersApplications(
        @PathVariable(name = "uid") uid: String
    ): DesignResponse<List<Application>?> {
        return mongoAppServiceImpl.getUsersApplications(uid)
    }
}