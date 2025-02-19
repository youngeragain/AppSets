package xcj.app.main.service

import xcj.app.DesignResponse
import xcj.app.main.model.req.*
import xcj.app.main.model.res.GroupInfoRes

interface ChatGroupService {

    fun getGroupInfo(groupId: String): DesignResponse<GroupInfoRes>

    fun getGroupInfoListByToken(token: String): DesignResponse<List<GroupInfoRes>>

    fun getGroupInfoListByUserId(userId: String): DesignResponse<List<GroupInfoRes>>

    fun getUserIdsInGroup(groupId: String): DesignResponse<List<String>>

    fun createGroup(token: String, createGroupParams: CreateGroupParams): DesignResponse<Boolean>

    fun deleteGroup(deleteGroupParams: DeleteGroupParams): DesignResponse<Boolean>

    fun deleteUsersInGroup(deleteUsersInGroupParams: DeleteUsersInGroupParams): DesignResponse<Boolean>

    fun searchChatGroupsByKeywords(keywords: String, page: Int?, pageSize: Int?): DesignResponse<List<GroupInfoRes>>

    fun requestJoinGroup(token: String, requestJoinGroupParams: RequestJoinGroupParams): DesignResponse<String?>

    fun requestJoinGroupFeedback(requestJoinGroupFeedbackParams: RequestJoinGroupFeedbackParams): DesignResponse<Boolean>

    fun createGroupPreCheck(groupName: String): DesignResponse<Boolean>

}