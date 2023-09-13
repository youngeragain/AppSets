package xcj.app.userinfo.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignEncodeStr
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.AddAppParams
import xcj.app.userinfo.model.req.UpdateAppParams
import xcj.app.userinfo.model.res.AppsWithCategory
import xcj.app.userinfo.model.table.mongo.Application
import xcj.app.userinfo.service.mongo.AppService

@RequestMapping("/appsets")
@RestController
class AppsController(

    private val mongoAppServiceImpl: AppService
    ) {

    @RequestMapping("apps/{page}/{pageSize}")
    fun getAllApplicationsPaged(@PathVariable(name = "page") page:Int,
                                @PathVariable(name = "pageSize", required = false) pageSize:Int?):DesignResponse<List<Application>>{
        return mongoAppServiceImpl.getAllApplicationsPaged(page, pageSize?:20)
    }

    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)
    @RequestMapping("apps/index/recommend")
    fun getIndexApplications():DesignResponse<List<AppsWithCategory>?>{
        return mongoAppServiceImpl.getIndexApplications()
    }

    @ApiDesignPermission.UserRoleRequired(andRoles = [ApiDesignPermission.UserRoleRequired.ROLE_DEVELOPER], orRoles = [])
    @ApiDesignPermission.LoginRequired
    @RequestMapping("app", method = [RequestMethod.POST])
    fun addApplicationByDeveloper(@RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
                                  @RequestBody addAppParams: AddAppParams):DesignResponse<Boolean>{
        return mongoAppServiceImpl.addApplicationByDeveloper(token, addAppParams)
    }


    @ApiDesignPermission.LoginRequired
    @PostMapping("app/create")
    fun createAppByUser(@RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
                          @RequestBody createAppParams: Map<String, Any?>):DesignResponse<Boolean>{
        return mongoAppServiceImpl.createApplicationByUser(token, createAppParams)
    }

    @ApiDesignPermission.LoginRequired
    @GetMapping("app/create/precheck")
    fun createApplicationPreCheckByUser(@RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
                                        @RequestParam("appName") appName: String):DesignResponse<Boolean>{
        return mongoAppServiceImpl.createApplicationPreCheckByUser(token, appName)
    }

    @ApiDesignPermission.UserRoleRequired(andRoles = [ApiDesignPermission.UserRoleRequired.ROLE_DEVELOPER], orRoles = [])
    @ApiDesignPermission.LoginRequired
    @RequestMapping("app", method = [RequestMethod.PUT])
    fun updateApplicationByDeveloper(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
        @RequestBody updateAppParams: UpdateAppParams):DesignResponse<Boolean>{
        return mongoAppServiceImpl.updateApplicationByDeveloper(token, updateAppParams)
    }



   /* @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.SpecialRequired(ApiDesignEncodeStr.appTokenStrToMd5)*/
    @GetMapping("application/search")
    fun searchApplicationByKeywords(
//        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
        @RequestParam(name = "keywords") keywords:String,
        @RequestParam(name = "page", required = false) page:Int? = 1,
        @RequestParam(name = "size", required = false) pageSize:Int? = 20,
    ):DesignResponse<List<Application>?>{
        return mongoAppServiceImpl.searchApplicationsByKeywords(keywords, page, pageSize)
    }

    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.LoginRequired
    @GetMapping("application/user/{uid}")
    fun getUsersApplications(
        @PathVariable(name = "uid") uid: String):DesignResponse<List<Application>?>{
        return mongoAppServiceImpl.getUsersApplications(uid)
    }

}