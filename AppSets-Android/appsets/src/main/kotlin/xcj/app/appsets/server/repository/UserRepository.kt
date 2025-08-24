package xcj.app.appsets.server.repository

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.awaitResponse
import xcj.app.appsets.server.api.ApiProvider
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.model.SignUpUserInfo
import xcj.app.appsets.ui.model.UserInfoModification
import xcj.app.appsets.usecase.GroupCreateInfo
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
        userInfoModification: UserInfoModification,
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "updateUserInfo, thread:${Thread.currentThread()}")
        var avatarUrlEndpoint: String? = null
        val avatarImageUri = userInfoModification.userAvatarUri?.provideUri()
        if (avatarImageUri != null) {
            avatarUrlEndpoint = UUID.randomUUID().toString()
            LocalFileIO.current.uploadWithUri(context, avatarImageUri, avatarUrlEndpoint)
        }
        val updateParams = hashMapOf<String, String?>().apply {
            if (userInfoModification.userName.isNotEmpty() && userInfoModification.userName != oldUserInfo.name) {
                put("name", userInfoModification.userName)
            }
            if (userInfoModification.userAge.isNotEmpty() && userInfoModification.userAge.toInt() != oldUserInfo.age) {
                put("age", userInfoModification.userAge)
            }
            if (userInfoModification.userSex.isNotEmpty() && userInfoModification.userSex != oldUserInfo.sex) {
                put("sex", userInfoModification.userSex)
            }
            if (userInfoModification.userEmail.isNotEmpty() && userInfoModification.userEmail != oldUserInfo.email) {
                put("email", userInfoModification.userEmail)
            }
            if (userInfoModification.userPhone.isNotEmpty() && userInfoModification.userPhone != oldUserInfo.phone) {
                put("phone", userInfoModification.userPhone)
            }
            if (userInfoModification.userAddress.isNotEmpty() && userInfoModification.userAddress != oldUserInfo.address) {
                put("address", userInfoModification.userAddress)
            }
            if (!avatarUrlEndpoint.isNullOrEmpty()) {
                put("avatarUrl", avatarUrlEndpoint)
            }
            if (userInfoModification.userIntroduction.isNotEmpty() && userInfoModification.userIntroduction != oldUserInfo.introduction) {
                put("intro", userInfoModification.userIntroduction)
            }
            if (userInfoModification.userWebsite.isNotEmpty() && userInfoModification.userWebsite != oldUserInfo.website) {
                put("website", userInfoModification.userWebsite)
            }
        }
        if (updateParams.isEmpty()) {
            return@withContext DesignResponse(data = false)
        }
        updateParams["uid"] = oldUserInfo.uid
        return@withContext userApi.updateUserInfo(updateParams)
    }

    suspend fun getFriends(): DesignResponse<List<UserInfo>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getFriends, thread:${Thread.currentThread()}")
        val designResponse = userApi.getFriends()
        PictureUrlMapper.mapPictureUrl(designResponse.data)
        return@withContext designResponse
    }

    suspend fun getChatGroups(): DesignResponse<List<GroupInfo>> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getChatGroups, thread:${Thread.currentThread()}")
        val designResponse = userApi.getChatGroupInfoList()
        PictureUrlMapper.mapPictureUrl(designResponse.data)
        return@withContext designResponse
    }

    suspend fun createChatGroup(
        context: Context,
        groupCreateInfo: GroupCreateInfo
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "createChatGroup, thread:${Thread.currentThread()}")
        var iconUrlEndpoint: String? = null
        val uri = groupCreateInfo.icon?.provideUri()
        if (uri != null) {
            iconUrlEndpoint = UUID.randomUUID().toString()
            LocalFileIO.current.uploadWithUri(context, uri, iconUrlEndpoint)
        }
        return@withContext userApi.createChatGroup(hashMapOf<String, Any?>().apply {
            put("name", groupCreateInfo.name)
            put("maxMembers", groupCreateInfo.membersCount)
            put("isPublic", groupCreateInfo.isPublic)
            put("introduction", groupCreateInfo.introduction)
            iconUrlEndpoint?.let {
                put("iconUrl", it)
            }
        })
    }

    suspend fun getGroupInfoById(groupId: String): DesignResponse<GroupInfo> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getGroupInfoById, thread:${Thread.currentThread()}")
            return@withContext userApi.getGroupInfoById(groupId)
        }

    suspend fun requestAddFriend(
        uid: String,
        hello: String,
        reason: String?,
    ): DesignResponse<String> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "requestAddFriend, thread:${Thread.currentThread()}")
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
            "sendAddRequestFriendFeedback, thread:${Thread.currentThread()}"
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
        PurpleLogger.current.d(TAG, "requestJoinGroup, thread:${Thread.currentThread()}")
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
            "sendRequestJoinGroupFeedback, thread:${Thread.currentThread()}"
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
            PurpleLogger.current.d(TAG, "flipFollowToUserState, thread:${Thread.currentThread()}")
            return@withContext userApi.flipFollowToUserState(uid)
        }

    suspend fun createChatGroupPreCheck(groupName: String): DesignResponse<Boolean> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "createChatGroupPreCheck, thread:${Thread.currentThread()}")
            return@withContext userApi.createChatGroupPreCheck(groupName)
        }

    suspend fun getFollowersAndFollowedByUser(uid: String): DesignResponse<Map<String, List<UserInfo>?>> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(
                TAG,
                "getFollowersAndFollowedByUser, thread:${Thread.currentThread()}"
            )
            val designResponse = userApi.getFollowersByUser(uid)
            designResponse.data?.flatMap { it.value ?: emptyList() }?.let { userInfoList ->
                PictureUrlMapper.mapPictureUrl(userInfoList)
            }
            return@withContext designResponse
        }

    suspend fun getMyFollowedThisUser(uid: String): DesignResponse<Boolean> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getMyFollowedThisUser, thread:${Thread.currentThread()}")
            return@withContext userApi.getMyFollowedThisUser(uid)
        }

    suspend fun login(account: String, password: String): DesignResponse<String> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "login, thread:${Thread.currentThread()}")
            return@withContext userApi.login(
                hashMapOf(
                    "account" to account,
                    "password" to password,
                    "signInDeviceInfo" to DeviceInfoHelper.provideInfo(),
                    "signInLocation" to "Si Chuan"
                )
            )
        }

    suspend fun login2(): DesignResponse<String> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "login2, thread:${Thread.currentThread()}")
        return@withContext userApi.login2(
            hashMapOf(
                "account" to "account",
                "password" to "password",
                "signInDeviceInfo" to DeviceInfoHelper.provideInfo(),
                "signInLocation" to "Si Chuan"
            )
        )
    }

    suspend fun loginCall(account: String, password: String): DesignResponse<String> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "loginCall, thread:${Thread.currentThread()}")
            val loginCall =
                userApi.loginCall(hashMapOf("account" to account, "password" to password))
            val awaitResponse = loginCall.awaitResponse().body()
            return@withContext DesignResponse(data = awaitResponse)
        }

    suspend fun signUp(
        context: Context,
        account: String,
        password: String,
        signUpUserInfo: SignUpUserInfo
    ): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "signUp, thread:${Thread.currentThread()}")
        var avatarUrlEndpoint: String? = null
        val avatarImageUri = signUpUserInfo.userAvatar?.provideUri()
        if (avatarImageUri != null) {
            avatarUrlEndpoint = UUID.randomUUID().toString()
            LocalFileIO.current
                .uploadWithUri(context, avatarImageUri, avatarUrlEndpoint)
        }
        return@withContext userApi.signUp(
            hashMapOf<String, Any?>(
                "account" to account,
                "password" to password
            ).apply {
                if (signUpUserInfo.userName.isNotEmpty()) {
                    put("name", signUpUserInfo.userName)
                }
                if (!avatarUrlEndpoint.isNullOrEmpty()) {
                    put("avatarUrl", avatarUrlEndpoint)
                }
                if (signUpUserInfo.userIntroduction.isNotEmpty()) {
                    put("introduction", signUpUserInfo.userIntroduction)
                }
                if (signUpUserInfo.userTags.isNotEmpty()) {
                    put("tags", signUpUserInfo.userTags)
                }
                if (signUpUserInfo.userSex.isNotEmpty()) {
                    put("sex", signUpUserInfo.userSex)
                }
                if (signUpUserInfo.userAge.isNotEmpty()) {
                    put("age", signUpUserInfo.userAge.toIntOrNull() ?: 0)
                }
                if (signUpUserInfo.userPhone.isNotEmpty()) {
                    put("phone", signUpUserInfo.userPhone)
                }
                if (signUpUserInfo.userEmail.isNotEmpty()) {
                    put("email", signUpUserInfo.userEmail)
                }
                if (signUpUserInfo.userArea.isNotEmpty()) {
                    put("area", signUpUserInfo.userArea)
                }
                if (signUpUserInfo.userAddress.isNotEmpty()) {
                    put("address", signUpUserInfo.userAddress)
                }
                if (signUpUserInfo.userWebsite.isNotEmpty()) {
                    put("website", signUpUserInfo.userWebsite)
                }
            }
        )
    }

    suspend fun signOut(): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "signOut, thread:${Thread.currentThread()}")
        return@withContext userApi.signOut()
    }

    suspend fun preSignUp(account: String): DesignResponse<Boolean> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "preSignUp, thread:${Thread.currentThread()}")
        return@withContext userApi.preSignUp(account)
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