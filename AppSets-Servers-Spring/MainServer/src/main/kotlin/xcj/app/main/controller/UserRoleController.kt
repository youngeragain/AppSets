package xcj.app.main.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.req.AddRoleParams
import xcj.app.main.model.req.DeleteRoleParams
import xcj.app.main.model.req.UpdateRoleParams
import xcj.app.main.service.UserRoleService

@RequestMapping("/user")
@RestController
class UserRoleController(
    private val simpleUserRoleServiceImpl: UserRoleService
) {

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("role", method = [RequestMethod.POST])
    fun addRoleToUser(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody addRoleParams: AddRoleParams
    ): DesignResponse<Boolean> {
        return simpleUserRoleServiceImpl.addRoleByToken(token, addRoleParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("role", method = [RequestMethod.DELETE])
    fun deleteUserRole(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody deleteRoleParams: DeleteRoleParams
    ): DesignResponse<Boolean> {
        return simpleUserRoleServiceImpl.deleteRoleByToken(token, deleteRoleParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("role", method = [RequestMethod.PUT])
    fun updateUserRole(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody updateRoleParams: UpdateRoleParams
    ): DesignResponse<Boolean> {
        return simpleUserRoleServiceImpl.updateRoleByToken(token, updateRoleParams)
    }
}