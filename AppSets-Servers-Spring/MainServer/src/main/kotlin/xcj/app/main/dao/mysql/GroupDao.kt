package xcj.app.main.dao.mysql

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Select
import xcj.app.main.model.res.GroupInfoRes
import xcj.app.main.model.table.mysql.ChatGroup

@Mapper
interface GroupDao {
    companion object {
        const val DEFAULT_GROUP_1_ID = "G7775"
    }

    fun getGroupInfoResByGroupId(groupId: String): GroupInfoRes?

    fun getChatGroupInfoByGroupId(groupId: String): ChatGroup?

    fun getGroupInfoResListByUserId(uid: String): List<GroupInfoRes>?

    fun getUserIdsInGroupByGroupId(groupId: String): List<String>?

    fun createGroup(
        name: String? = null,
        groupId: String,
        type: Int? = null,
        iconUrl: String? = null,
        introduction: String? = null,
        isPublic: Int? = null,
        maxMembers: Int? = null,
        uid: String,
        uids: List<String>? = null
    ): Int

    fun deleteGroup(groupId: String): Int

    fun deleteUsersInGroup(groupId: String, uids: List<String>): Int

    fun deleteUserInGroup(groupId: String, uid: String): Int

    fun isGroupNameExist(name: String): Boolean

    fun addUsersInGroup(groupId: String, uids: List<String>): Int

    fun isGroupIdExist(groupId: String): Boolean

    fun searchChatGroupResListByKeywords(keywords: String, limit: Int, offset: Int): List<GroupInfoRes>

    @Select("select count(1) from user_chat_group_2022 where group_id = #{groupId} and uid = #{uid}")
    fun isUserExistInGroup(groupId: String, uid: String): Boolean

}