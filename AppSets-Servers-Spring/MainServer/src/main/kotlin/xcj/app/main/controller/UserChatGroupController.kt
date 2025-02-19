package xcj.app.main.controller

import org.springframework.web.bind.annotation.*
import xcj.app.ApiDesignKeys
import xcj.app.ApiDesignPermission
import xcj.app.DesignResponse
import xcj.app.main.model.req.*
import xcj.app.main.model.res.GroupInfoRes
import xcj.app.main.service.ChatGroupService

@RequestMapping("/user")
@RestController
class UserChatGroupController(
    private val simpleChatGroupServiceImpl: ChatGroupService
) {

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("chatgroup/{groupId}")
    fun getUserChatGroupInfo(
        @PathVariable(name = "groupId") groupId: String
    ): DesignResponse<GroupInfoRes> {
        return simpleChatGroupServiceImpl.getGroupInfo(groupId)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("chatgroups")
    fun getUserChatGroupInfoList(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<List<GroupInfoRes>> {
        return simpleChatGroupServiceImpl.getGroupInfoListByToken(token)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @ApiDesignPermission.AdministratorRequired
    @RequestMapping("admin/chatgroups/{userId}")
    fun getUserChatGroupInfoListAdmin(
        @PathVariable("userId") userId: String
    ): DesignResponse<List<GroupInfoRes>> {
        return simpleChatGroupServiceImpl.getGroupInfoListByUserId(userId)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("chatgroup/userids/{groupId}")
    fun getUserIdsInChatGroup(
        @PathVariable(name = "groupId") groupId: String
    ): DesignResponse<List<String>> {
        return simpleChatGroupServiceImpl.getUserIdsInGroup(groupId)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("chatgroup")
    fun createGroup(
        @RequestBody createGroupParams: CreateGroupParams,
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<Boolean> {
        return simpleChatGroupServiceImpl.createGroup(token, createGroupParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("chatgroup/precheck")
    fun createGroupPreCheck(
        @RequestParam(name = "name") groupName: String,
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String
    ): DesignResponse<Boolean> {
        return simpleChatGroupServiceImpl.createGroupPreCheck(groupName)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("chatgroup/delete")
    fun deleteGroup(
        @RequestBody deleteGroupParams: DeleteGroupParams
    ): DesignResponse<Boolean> {
        return simpleChatGroupServiceImpl.deleteGroup(deleteGroupParams)
    }

    /**
     * 注意，如果群type设置为-1，表示该群可以被系统托管，即使群里面没有用户也没有群主
     * 退群，
     * 1:群主选择部分用户退出
     * 2:用户自己退出群
     * 2.1:群主自己退出群
     * 2.2:其他用户退出用户
     */
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("chatgroup/deleteusers")
    fun deleteUsersInGroup(
        @RequestBody deleteUsersInGroupParams: DeleteUsersInGroupParams
    ): DesignResponse<Boolean> {
        return simpleChatGroupServiceImpl.deleteUsersInGroup(deleteUsersInGroupParams)
    }


    /**
     * 请求加入群组反馈
     * @return requestId
     */
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("chatgroup/requestjoin/feedback", method = [RequestMethod.POST])
    fun requestJoinGroupFeedback(
        @RequestBody requestJoinGroupFeedbackParams: RequestJoinGroupFeedbackParams
    ): DesignResponse<Boolean> {
        return simpleChatGroupServiceImpl.requestJoinGroupFeedback(requestJoinGroupFeedbackParams)
    }

    /**
     * 请求加入群组
     * @return requestId
     */
    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @RequestMapping("chatgroup/requestjoin", method = [RequestMethod.POST])
    fun requestJoinGroup(
        @RequestHeader(name = ApiDesignKeys.TOKEN_MD5) token: String,
        @RequestBody requestJoinGroupParams: RequestJoinGroupParams
    ): DesignResponse<String?> {
        return simpleChatGroupServiceImpl.requestJoinGroup(token, requestJoinGroupParams)
    }

    @ApiDesignPermission.LoginRequired
    @ApiDesignPermission.VersionRequired(200)
    @GetMapping("chatgroup/search")
    fun searchChatGroupsByKeywords(
        @RequestParam(name = "keywords") keywords: String,
        @RequestParam(name = "page", required = false) page: Int? = 1,
        @RequestParam(name = "size", required = false) pageSize: Int? = 20,
    ): DesignResponse<List<GroupInfoRes>> {
        return simpleChatGroupServiceImpl.searchChatGroupsByKeywords(keywords, page, pageSize)
    }

}