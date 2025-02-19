package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper

@Mapper
interface RoleDao {
    /**
     * @param hostUid 由谁赋予的角色uid,Null为系统
     * @param beenGivenUid 被赋予角色的uid
     */
    fun addRole(beenGivenUid: String, role: String, hostUid: String? = null, reason: String? = null): Int

    fun addRoles(uid: String, roles: Set<String>, hostUid: String? = null, reason: String? = null): Int

    fun deleteUserRole(uid: String, role: String): Int

    fun deleteUserRoles(uid: String, roles: Set<String>)

    fun updateUserRole(uid: String, oldRole: String, newRole: String): Int

    fun getAllRoleByUid(uid: String): List<String>

    fun getAllRoleByUidPaged(
        uid: String,
        limit: Int? = null,
        offset: Int? = null,
        orderByTime: Boolean = false
    ): List<String>

    fun isUserHasRole(uid: String, role: String): Boolean

    fun getUserRolesByUid(uid: String): List<String>

}