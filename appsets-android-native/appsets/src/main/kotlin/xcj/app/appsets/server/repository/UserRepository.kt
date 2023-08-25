package xcj.app.appsets.server.repository

import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.core.foundation.http.DesignResponse

class UserRepository(private val userApi: UserApi) {

    suspend fun getLoggedUserInfo(): DesignResponse<UserInfo> {
        return userApi.getLoggedUserInfo()
    }

    suspend fun getUserInfoByUid(uid: String): DesignResponse<UserInfo> {
        return userApi.getUserInfoByUid(uid)
    }

    suspend fun getFriends(): DesignResponse<List<UserInfo>> {
        return userApi.getFriends()
    }

    suspend fun getChatGroups(): DesignResponse<List<GroupInfo>> {
        return userApi.getChatGroupInfoList()
    }

    suspend fun createChatGroup(
        name: String,
        iconUrl: String?,
        groupMembersCount: String,
        isPublic: Boolean,
        groupIntroduction: String
    ): DesignResponse<Boolean> {
        return userApi.createChatGroup(hashMapOf<String, Any?>().apply {
            put("name", name)
            put("maxMembers", groupMembersCount)
            put("isPublic", isPublic)
            put("introduction", groupIntroduction)
            iconUrl?.let {
                put("iconUrl", it)
            }
        })
    }


    suspend fun getGroupInfoById(groupId: String): DesignResponse<GroupInfo?> {
        return userApi.getGroupInfoById(groupId)
    }


    suspend fun requestAddFriend(
        uid: String,
        hello: String,
        reason: String?,
    ): DesignResponse<String?> {
        val body = hashMapOf<String, Any?>(
            "uid" to uid,
            "hello" to hello,
        ).apply {
            reason?.let {
                put("reason", it)
            }
        }
        return userApi.requestAddFriend(body)
    }

    suspend fun sendAddRequestFriendFeedback(
        requestId: String,
        requestUid: String,
        result: Boolean,
        reason: String?
    ): DesignResponse<Boolean> {
        val body = hashMapOf<String, Any?>(
            "requestId" to requestId,
            "requestUid" to requestUid,
            "isAccept" to result,
        ).apply {
            reason?.let {
                put("reason", it)
            }
        }
        return userApi.requestAddFriendFeedback(body)
    }

    suspend fun requestJoinGroup(
        groupId: String,
        hello: String,
        reason: String?,
    ): DesignResponse<String?> {
        val body = hashMapOf<String, Any?>(
            "groupId" to groupId,
            "hello" to hello,
        ).apply {
            reason?.let {
                put("reason", it)
            }
        }
        return userApi.requestJoinGroup(body)
    }

    suspend fun sendRequestJoinGroupFeedback(
        requestId: String,
        requestUid: String,
        groupId: String,
        result: Boolean,
        reason: String?
    ): DesignResponse<Boolean> {
        val body = hashMapOf<String, Any?>(
            "requestId" to requestId,
            "requestUid" to requestUid,
            "groupId" to groupId,
            "isAccept" to result,
            "userIds" to listOf(requestUid)
        ).apply {
            reason?.let {
                put("reason", it)
            }
        }
        return userApi.requestJoinGroupFeedback(body)
    }

    suspend fun flipFollowToUserState(uid: String): DesignResponse<Boolean> {
        return userApi.flipFollowToUserState(uid)
    }

    suspend fun createChatGroupPreCheck(groupName: String): DesignResponse<Boolean> {
        return userApi.createChatGroupPreCheck(groupName)
    }
}