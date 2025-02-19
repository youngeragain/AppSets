package xcj.app.main.service

import org.springframework.stereotype.Service
import xcj.app.ApiDesignCode
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.dao.mysql.RoleDao
import xcj.app.main.model.req.AddRoleParams
import xcj.app.main.model.req.DeleteRoleParams
import xcj.app.main.model.req.UpdateRoleParams
import xcj.app.main.util.TokenHelper

/**
 * 必须role为admin的才可以设置角色
 */
@Service
class SimpleUserRoleServiceImpl(
    private val tokenHelper: TokenHelper,
    private val roleDao: RoleDao
) : UserRoleService {
    override fun addRoleByUid(uid: String, addRoleParams: AddRoleParams): DesignResponse<Boolean> {
        val userHasRole = roleDao.isUserHasRole(addRoleParams.uid, addRoleParams.role)
        if (userHasRole) {
            return DesignResponse(
                ApiDesignCode.ERROR_CODE_FATAL,
                "The userId:${addRoleParams.uid} has  role:${addRoleParams.role}!",
                false
            )
        }
        val addRoleResult =
            roleDao.addRole(addRoleParams.uid, addRoleParams.role, uid, addRoleParams.addReason)
        return if (addRoleResult == 1) {
            DesignResponse(
                info = "Add role:${addRoleParams.role} for userId:${addRoleParams.uid} successful!",
                data = true
            )
        } else {
            DesignResponse(
                ApiDesignCode.ERROR_CODE_FATAL,
                "Add role:${addRoleParams.role} for userId:${addRoleParams.uid} failed!",
                data = false
            )
        }
    }

    override fun addRoleByToken(token: String, addRoleParams: AddRoleParams): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        return addRoleByUid(uid, addRoleParams)
    }

    override fun deleteRoleByUid(uid: String, deleteRoleParams: DeleteRoleParams): DesignResponse<Boolean> {
        val userHasRole = roleDao.isUserHasRole(deleteRoleParams.uid, deleteRoleParams.role)
        if (!userHasRole) {
            return DesignResponse(
                ApiDesignCode.ERROR_CODE_FATAL,
                "The userId:${deleteRoleParams.uid} does not have role:${deleteRoleParams.role}!",
                false
            )
        }
        val deleteUserRoleResult = roleDao.deleteUserRole(deleteRoleParams.uid, deleteRoleParams.role)
        return if (deleteUserRoleResult == 1) {
            DesignResponse(
                info = "Delete userId:${deleteRoleParams.uid}} role:${deleteRoleParams.role} successful!",
                data = true
            )
        } else {
            DesignResponse(
                info = "Delete userId:${deleteRoleParams.uid}} role:${deleteRoleParams.role} failed!",
                data = false
            )
        }
    }

    override fun deleteRoleByToken(token: String, deleteRoleParams: DeleteRoleParams): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        return deleteRoleByUid(uid, deleteRoleParams)
    }

    override fun updateRoleByUid(uid: String, updateRoleParams: UpdateRoleParams): DesignResponse<Boolean> {
        val userHasRole = roleDao.isUserHasRole(updateRoleParams.uid, updateRoleParams.oldRole)
        if (!userHasRole) {
            return DesignResponse(
                ApiDesignCode.ERROR_CODE_FATAL,
                "The userId:${updateRoleParams.uid} has not role:${updateRoleParams.oldRole}",
                false
            )
        }
        val updateUserRoleResult =
            roleDao.updateUserRole(updateRoleParams.uid, updateRoleParams.oldRole, updateRoleParams.newRole)
        return if (updateUserRoleResult == 1) {
            DesignResponse(
                info = "Update userId:${updateRoleParams.uid} old role:${updateRoleParams.oldRole} to new role:${updateRoleParams.newRole} successful!",
                data = true
            )
        } else {
            DesignResponse(
                info = "Update userId:${updateRoleParams.uid} old role:${updateRoleParams.oldRole} to new role:${updateRoleParams.newRole} failed!",
                data = false
            )
        }
    }

    override fun updateRoleByToken(token: String, updateRoleParams: UpdateRoleParams): DesignResponse<Boolean> {
        val uid = tokenHelper.getUidByToken(token)
        return updateRoleByUid(uid, updateRoleParams)
    }

    override fun userRoleIsAdminByToken(token: String): Boolean {
        val uid = tokenHelper.getUidByToken(token)
        return userRoleIsAdminByUid(uid)
    }

    override fun userRoleIsAdminByUid(uid: String): Boolean {
        return roleDao.isUserHasRole(uid, ApiDesignPermission.UserRoleRequired.ROLE_ADMIN)
    }

    override fun userHasThoseRolesStrict(token: String, rolesNeeded: Array<String>): Boolean {
        val uid = tokenHelper.getUidByToken(token)
        val userRoles = roleDao.getUserRolesByUid(uid)
        val mustCount = rolesNeeded.size
        var count = 0
        for (userRole in userRoles) {
            if (count == mustCount) {
                break
            }
            if (userRole in rolesNeeded) {
                count++
            }
        }
        return count == mustCount
    }

    override fun userHasThoseRolesNotStrict(token: String, rolesNeeded: Array<String>): Boolean {
        val uid = tokenHelper.getUidByToken(token)
        val userRoles = roleDao.getUserRolesByUid(uid)
        for (userRole in userRoles) {
            if (userRole in rolesNeeded) {
                return true
            }
        }
        return false
    }
}