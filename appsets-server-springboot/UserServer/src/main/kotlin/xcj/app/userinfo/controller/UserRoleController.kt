package xcj.app.userinfo.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignEncodeStr
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.AddRoleParams
import xcj.app.userinfo.model.req.DeleteRoleParams
import xcj.app.userinfo.model.req.UpdateRoleParams
import xcj.app.userinfo.service.UserRoleService

@RequestMapping("/user")
@RestController
class UserRoleController(
    private val simpleUserRoleServiceImpl: UserRoleService
) {

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("role", method = [RequestMethod.POST])
    fun addRoleToUser(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
        @RequestBody addRoleParams: AddRoleParams
    ):DesignResponse<Boolean>{
        return simpleUserRoleServiceImpl.addRoleByToken(token, addRoleParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("role", method = [RequestMethod.DELETE])
    fun deleteUserRole(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
        @RequestBody deleteRoleParams: DeleteRoleParams
    ):DesignResponse<Boolean>{
        return simpleUserRoleServiceImpl.deleteRoleByToken(token, deleteRoleParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("role", method = [RequestMethod.PUT])
    fun updateUserRole(
        @RequestHeader(name = ApiDesignEncodeStr.tokenStrToMd5) token:String,
        @RequestBody updateRoleParams: UpdateRoleParams
    ):DesignResponse<Boolean>{
        return simpleUserRoleServiceImpl.updateRoleByToken(token, updateRoleParams)
    }
}