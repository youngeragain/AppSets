package xcj.app.appsets.server.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.model.GroupInfoForCreate
import xcj.app.appsets.ui.model.UserInfoForCreate
import xcj.app.appsets.ui.model.UserInfoForModify
import xcj.app.appsets.util.DeviceInfoHelper
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.io.components.LocalFileIO
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.foundation.http.DesignResponse
import java.util.UUID

class UserRepository(private val userApi: UserApi) {

    suspend fun getLoggedUserInfo(): DesignResponse<UserInfo> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getLoggedUserInfo, thread:${Thread.currentThread()}")
        val designResponse = userApi.getLoggedUserInfo()
        PictureUrlMapper.mapPictureUrl(designResponse.data)
        return@withContext designResponse
    }

    suspend fun getUserInfoByUid(uid: String): DesignResponse<UserInfo> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getUserInfoByUid, thread:${Thread.currentThread()}")
            val designResponse = userApi.getUserInfoByUid(uid)
            PictureUrlMapper.mapPictureUrl(designResponse.data)
            return@withContext designResponse
        }

    suspend fun updateUserInfo(
        context: Context,
        oldUserInfo: UserInfo,
        userInfoForModify: UserInfoForModify,
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "updateUserInfo")
        var avatarUrlEndpoint: String? = null
        val avatarImageUri = userInfoForModify.userAvatarUriProvider.value?.provideUri()
        if (avatarImageUri != null) {
            avatarUrlEndpoint = UUID.randomUUID().toString()
            LocalFileIO.current.uploadWithUri(context, avatarImageUri, avatarUrlEndpoint)
        }
        val body = hashMapOf<String, String?>().apply {
            if (userInfoForModify.userName.value.isNotEmpty() && userInfoForModify.userName.value != oldUserInfo.bioName) {
                put("name", userInfoForModify.userName.value)
            }
            if (userInfoForModify.userAge.value.isNotEmpty() && userInfoForModify.userAge.value.toInt() != oldUserInfo.age) {
                put("age", userInfoForModify.userAge.value)
            }
            if (userInfoForModify.userSex.value.isNotEmpty() && userInfoForModify.userSex.value != oldUserInfo.sex) {
                put("sex", userInfoForModify.userSex.value)
            }
            if (userInfoForModify.userEmail.value.isNotEmpty() && userInfoForModify.userEmail.value != oldUserInfo.email) {
                put("email", userInfoForModify.userEmail.value)
            }
            if (userInfoForModify.userPhone.value.isNotEmpty() && userInfoForModify.userPhone.value != oldUserInfo.phone) {
                put("phone", userInfoForModify.userPhone.value)
            }
            if (userInfoForModify.userAddress.value.isNotEmpty() && userInfoForModify.userAddress.value != oldUserInfo.address) {
                put("address", userInfoForModify.userAddress.value)
            }
            if (!avatarUrlEndpoint.isNullOrEmpty()) {
                put("avatarUrl", avatarUrlEndpoint)
            }
            if (userInfoForModify.userIntroduction.value.isNotEmpty() && userInfoForModify.userIntroduction.value != oldUserInfo.introduction) {
                put("intro", userInfoForModify.userIntroduction.value)
            }
            if (userInfoForModify.userWebsite.value.isNotEmpty() && userInfoForModify.userWebsite.value != oldUserInfo.website) {
                put("website", userInfoForModify.userWebsite.value)
            }
        }
        if (body.isEmpty()) {
            return@withContext DesignResponse(data = false)
        }
        body["uid"] = oldUserInfo.uid
        return@withContext userApi.updateUserInfo(body)
    }

    suspend fun getFriends(): DesignResponse<List<UserInfo>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getFriends")
        val designResponse = userApi.getFriends()
        PictureUrlMapper.mapPictureUrl(designResponse.data)
        return@withContext designResponse
    }

    suspend fun getChatGroups(): DesignResponse<List<GroupInfo>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getChatGroups")
        val designResponse = userApi.getChatGroupInfoList()
        PictureUrlMapper.mapPictureUrl(designResponse.data)
        return@withContext designResponse
    }

    suspend fun createChatGroup(
        context: Context,
        groupInfoForCreate: GroupInfoForCreate
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "createChatGroup")
        var iconUrlEndpoint: String? = null
        val uri = groupInfoForCreate.iconUriProvider.value?.provideUri()
        if (uri != null) {
            iconUrlEndpoint = UUID.randomUUID().toString()
            LocalFileIO.current.uploadWithUri(context, uri, iconUrlEndpoint)
        }
        val body = hashMapOf<String, Any?>().apply {
            put("name", groupInfoForCreate.name.value)
            put("maxMembers", groupInfoForCreate.membersCount.value.toIntOrNull() ?: 1000)
            put("isPublic", groupInfoForCreate.isPublic.value)
            put("introduction", groupInfoForCreate.introduction.value)
            iconUrlEndpoint?.let {
                put("iconUrl", it)
            }
        }
        return@withContext userApi.createChatGroup(body)
    }

    suspend fun getGroupInfoById(groupId: String): DesignResponse<GroupInfo> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getGroupInfoById")
            return@withContext userApi.getGroupInfoById(groupId)
        }

    suspend fun requestAddFriend(
        uid: String,
        hello: String,
        reason: String?,
    ): DesignResponse<String> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "requestAddFriend")
        val body = hashMapOf<String, Any?>(
            "uid" to uid,
            "hello" to hello,
        ).apply {
            reason?.let {
                put("reason", it)
            }
        }
        return@withContext userApi.requestAddFriend(body)
    }

    suspend fun sendAddRequestFriendFeedback(
        requestId: String,
        requestUid: String,
        result: Boolean,
        reason: String?
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(
            TAG,
            "sendAddRequestFriendFeedback"
        )
        val body = hashMapOf<String, Any?>(
            "requestId" to requestId,
            "requestUid" to requestUid,
            "isAccept" to result,
        ).apply {
            reason?.let {
                put("reason", it)
            }
        }
        return@withContext userApi.requestAddFriendFeedback(body)
    }

    suspend fun requestJoinGroup(
        groupId: String,
        hello: String,
        reason: String?,
    ): DesignResponse<String> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "requestJoinGroup")
        val body = hashMapOf<String, Any?>(
            "groupId" to groupId,
            "hello" to hello,
        ).apply {
            reason?.let {
                put("reason", it)
            }
        }
        return@withContext userApi.requestJoinGroup(body)
    }

    suspend fun sendRequestJoinGroupFeedback(
        requestId: String,
        requestUid: String,
        groupId: String,
        result: Boolean,
        reason: String?
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(
            TAG,
            "sendRequestJoinGroupFeedback"
        )
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
        return@withContext userApi.requestJoinGroupFeedback(body)
    }

    suspend fun flipFollowToUserState(uid: String): DesignResponse<Boolean> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "flipFollowToUserState")
            return@withContext userApi.flipFollowToUserState(uid)
        }

    suspend fun createChatGroupPreCheck(groupName: String): DesignResponse<Boolean> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "createChatGroupPreCheck")
            return@withContext userApi.createChatGroupPreCheck(groupName)
        }

    suspend fun getFollowersAndFollowedByUser(uid: String): DesignResponse<Map<String, List<UserInfo>?>> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(
                TAG,
                "getFollowersAndFollowedByUser"
            )
            val designResponse = userApi.getFollowersByUser(uid)
            designResponse.data?.flatMap { it.value ?: emptyList() }?.let { userInfoList ->
                PictureUrlMapper.mapPictureUrl(userInfoList)
            }
            return@withContext designResponse
        }

    suspend fun getMyFollowedThisUser(uid: String): DesignResponse<Boolean> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getMyFollowedThisUser")
            return@withContext userApi.getMyFollowedThisUser(uid)
        }

    suspend fun login(account: String, password: String): DesignResponse<String> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "login")
            val body = hashMapOf<String, Any?>(
                "account" to account,
                "password" to password,
                "signInDeviceInfo" to DeviceInfoHelper.provideInfo(),
                "signInLocation" to "Si Chuan"
            )
            return@withContext userApi.login(
                body
            )
        }

    suspend fun login2(): DesignResponse<String> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "login2")
        val body = hashMapOf<String, Any?>(
            "account" to "account",
            "password" to "password",
            "signInDeviceInfo" to DeviceInfoHelper.provideInfo(),
            "signInLocation" to "Si Chuan"
        )
        return@withContext userApi.login2(
            body
        )
    }

    suspend fun loginCall(account: String, password: String): DesignResponse<String> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "loginCall")
            val loginCall =
                userApi.loginCall(hashMapOf("account" to account, "password" to password))
            val awaitResponse = loginCall.awaitResponse().body()
            return@withContext DesignResponse(data = awaitResponse)
        }

    suspend fun signUp(
        context: Context,
        accountEncoded: String,
        passwordEncoded: String,
        userInfoForCreate: UserInfoForCreate
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "signUp")
        var avatarUrlEndpoint: String? = null
        val avatarImageUri = userInfoForCreate.userAvatarUriProvider.value?.provideUri()
        if (avatarImageUri != null) {
            avatarUrlEndpoint = UUID.randomUUID().toString()
            LocalFileIO.current
                .uploadWithUri(context, avatarImageUri, avatarUrlEndpoint)
        }

        val body = hashMapOf<String, Any?>().apply {
            put("account", accountEncoded)
            put("password", passwordEncoded)
            if (userInfoForCreate.userName.value.isNotEmpty()) {
                put("name", userInfoForCreate.userName.value)
            }
            if (!avatarUrlEndpoint.isNullOrEmpty()) {
                put("avatarUrl", avatarUrlEndpoint)
            }
            if (userInfoForCreate.userIntroduction.value.isNotEmpty()) {
                put("introduction", userInfoForCreate.userIntroduction.value)
            }
            if (userInfoForCreate.userTags.value.isNotEmpty()) {
                put("tags", userInfoForCreate.userTags.value)
            }
            if (userInfoForCreate.userSex.value.isNotEmpty()) {
                put("sex", userInfoForCreate.userSex.value)
            }
            if (userInfoForCreate.userAge.value.isNotEmpty()) {
                put("age", userInfoForCreate.userAge.value.toIntOrNull() ?: 0)
            }
            if (userInfoForCreate.userPhone.value.isNotEmpty()) {
                put("phone", userInfoForCreate.userPhone.value)
            }
            if (userInfoForCreate.userEmail.value.isNotEmpty()) {
                put("email", userInfoForCreate.userEmail.value)
            }
            if (userInfoForCreate.userArea.value.isNotEmpty()) {
                put("area", userInfoForCreate.userArea.value)
            }
            if (userInfoForCreate.userAddress.value.isNotEmpty()) {
                put("address", userInfoForCreate.userAddress.value)
            }
            if (userInfoForCreate.userWebsite.value.isNotEmpty()) {
                put("website", userInfoForCreate.userWebsite.value)
            }
        }
        return@withContext userApi.signUp(body)
    }

    suspend fun signOut(): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "signOut")
        return@withContext userApi.signOut()
    }

    suspend fun preSignUp(accountEncoded: String): DesignResponse<Boolean> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "preSignUp")
            return@withContext userApi.preSignUp(accountEncoded)
        }

    companion object {
        private const val TAG = "UserRepository"
        private var INSTANCE: UserRepository? = null

        fun getInstance(): UserRepository {
            if (INSTANCE == null) {
                val api = ApiProvider.provide(UserApi::class.java)
                val repository = UserRepository(api)
                INSTANCE = repository
            }
            return INSTANCE!!
        }
    }
}