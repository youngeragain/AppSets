package xcj.app.main.service

import xcj.app.DesignResponse
import xcj.app.main.model.req.AddRoleParams
import xcj.app.main.model.req.DeleteRoleParams
import xcj.app.main.model.req.UpdateRoleParams

/**
 * 必须role为admin的才可以设置角色
 */
interface UserRoleService {

    fun addRoleByUid(uid: String, addRoleParams: AddRoleParams): DesignResponse<Boolean>

    fun addRoleByToken(token: String, addRoleParams: AddRoleParams): DesignResponse<Boolean>

    fun deleteRoleByUid(uid: String, deleteRoleParams: DeleteRoleParams): DesignResponse<Boolean>

    fun deleteRoleByToken(token: String, deleteRoleParams: DeleteRoleParams): DesignResponse<Boolean>

    fun updateRoleByUid(uid: String, updateRoleParams: UpdateRoleParams): DesignResponse<Boolean>

    fun updateRoleByToken(token: String, updateRoleParams: UpdateRoleParams): DesignResponse<Boolean>

    fun userRoleIsAdminByToken(token: String): Boolean

    fun userRoleIsAdminByUid(uid: String): Boolean

    ////任意一个role满足即可
    fun userHasThoseRolesNotStrict(token: String, rolesNeeded: Array<String>): Boolean

    //role必须都满足
    fun userHasThoseRolesStrict(token: String, rolesNeeded: Array<String>): Boolean

}