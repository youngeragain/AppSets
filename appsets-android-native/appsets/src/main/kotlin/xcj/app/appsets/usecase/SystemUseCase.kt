package xcj.app.appsets.usecase

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.BuildConfig
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.SystemContentInterface
import xcj.app.appsets.ktx.MediaStoreDataUriWrapper
import xcj.app.appsets.ktx.requestNotNull
import xcj.app.appsets.ktx.requestNotNullRaw
import xcj.app.appsets.ktx.toast
import xcj.app.appsets.ktx.toastSuspend
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.repository.UserLoginRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.service.MainService
import xcj.app.appsets.util.Md5Helper
import xcj.app.core.android.ApplicationHelper
import xcj.app.io.components.SimpleFileIO
import java.util.UUID

class SystemUseCase(private val coroutineScope: CoroutineScope) {
    private val TAG = "SystemUseCase"
    private var requestIdMap: MutableMap<String, String>? = mutableMapOf()
    private fun userFeedbackJoinGroupRequest(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: ImMessage.System,
        requestJson: SystemContentInterface.GroupRequestJson
    ) {
        imMessage.handling.value = true
        coroutineScope.requestNotNull({
            val userRepository = UserRepository(URLApi.provide(UserApi::class.java))
            userRepository.sendRequestJoinGroupFeedback(
                requestJson.requestId,
                requestJson.uid,
                requestJson.groupId,
                result,
                if (result) {
                    "同意"
                } else {
                    "拒绝"
                }
            )
        }, onSuccess = {
            Log.i(TAG, "userFeedbackFriendsRequest, call backend result:$it")
            /*if(result){
                val intent = Intent(context, MainService::class.java)
                intent.putExtra("what_to_do", "to_sync_user_data_from_server")
                context.startService(intent)
            }*/
            delay(1400)
            imMessage.handling.value = false
            delay(100)
            session.conversionState?.removeMessage(imMessage)
        })
    }

    private fun userFeedbackFriendsRequest(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: ImMessage.System,
        requestJson: SystemContentInterface.FriendRequestJson
    ) {
        if (UserRelationsCase.getInstance().hasUserRelated(requestJson.uid)) {
            "你们已经是好友".toast()
            return
        }
        imMessage.handling.value = true
        coroutineScope.requestNotNull({
            val userRepository = UserRepository(URLApi.provide(UserApi::class.java))
            userRepository.sendAddRequestFriendFeedback(
                requestJson.requestId,
                requestJson.uid,
                result,
                if (result) {
                    "同意"
                } else {
                    "拒绝"
                }
            )
        }, onSuccess = {
            Log.i(TAG, "userFeedbackFriendsRequest, call backend result:$it")
            if (result && it) {
                startServiceToSyncFriendsFromServer(context)
            }
            delay(1000)
            imMessage.handling.value = false
            delay(100)
            session.conversionState?.removeMessage(imMessage)
        })
    }


    fun requestAddFriend(uid: String, hello: String, reason: String?) {
        if (UserRelationsCase.getInstance().hasUserRelated(uid)) {
            "你们已经是好友".toast()
            return
        }
        coroutineScope.requestNotNullRaw({
            val userRepository = UserRepository(URLApi.provide(UserApi::class.java))
            userRepository.requestAddFriend(uid, hello, reason)
        }, onSuccess = {
            Log.i(TAG, "requestAddFriend, call backend result: requestId${it}")
            if (it.data.isNullOrEmpty()) {
                if (!it.info.isNullOrEmpty()) {
                    it.info.toastSuspend()
                }
            } else {
                "好友申请已发送，请等待，若对方没看到消息，10分钟后再试！".toastSuspend()
                requestIdMap?.put(uid, it.data!!)
            }
        })
    }

    fun requestJoinGroup(groupId: String, hello: String, reason: String?) {
        if (UserRelationsCase.getInstance().hasGroupRelated(groupId)) {
            "你已经在群组中".toast()
            return
        }
        coroutineScope.requestNotNullRaw({
            val userRepository = UserRepository(URLApi.provide(UserApi::class.java))
            userRepository.requestJoinGroup(groupId, hello, reason)
        }, onSuccess = {
            Log.i(TAG, "requestJoinGroup, call backend result: requestId${it}")
            if (it.data.isNullOrEmpty()) {
                if (!it.info.isNullOrEmpty()) {
                    it.info.toastSuspend()
                }
            } else {
                "加入申请已发送，请等待，若群组管理者没看到消息，10分钟后再试！".toastSuspend()
                requestIdMap?.put(groupId, it.data!!)
            }
        })
    }

    fun flipFollowToUserState(userInfo: UserInfo, callback: (() -> Unit)? = null) {
        coroutineScope.requestNotNull({
            val userRepository = UserRepository(URLApi.provide(UserApi::class.java))
            userRepository.flipFollowToUserState(userInfo.uid)
        }, onSuccess = {
            Log.i(TAG, "flipFollowToUserState, call backend result:${it}")
            callback?.invoke()
        })
    }

    fun handleUserRequestResult(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: ImMessage.System
    ) {
        when (imMessage.systemContentJson.contentObject) {
            is SystemContentInterface.FriendRequestJson -> {
                userFeedbackFriendsRequest(
                    context, result, session, imMessage,
                    imMessage.systemContentJson.contentObject as SystemContentInterface.FriendRequestJson
                )
            }

            is SystemContentInterface.GroupRequestJson -> {
                userFeedbackJoinGroupRequest(
                    context, result, session, imMessage,
                    imMessage.systemContentJson.contentObject as SystemContentInterface.GroupRequestJson
                )
            }

            else -> Unit
        }
    }

    fun cleanCaches() {
        coroutineScope.launch(Dispatchers.IO) {
            ApplicationHelper.getContextFileDir().cleanCaches()
        }
    }

    val loginSignUpState: MutableState<UserLoginUseCase.LoginSignUpState?> = mutableStateOf(null)

    val groupIconState: MutableState<MediaStoreDataUriWrapper?> = mutableStateOf(null)

    val userAvatarState: MutableState<MediaStoreDataUriWrapper?> = mutableStateOf(null)

    fun setUserSelectAvatarUri(imageUri: MediaStoreDataUriWrapper?) {
        userAvatarState.value = imageUri
    }

    fun signUp(
        context: Context,
        account: String,
        password: String,
        userName: String,
        userIntroduction: String,
        userTags: String,
        userSex: String,
        userAge: String,
        userPhone: String,
        userEmail: String,
        userArea: String,
        userAddress: String,
        userWebsite: String
    ) {
        if (!BuildConfig.CanSignUp) {
            "开发版本请向开发者索取账号!".toast()
            return
        }
        if (account.isEmpty()) {
            "请输入账号".toast()
            return
        }
        if (password.isEmpty()) {
            "请输入密码".toast()
            return
        }
        val tipsBuilder = StringBuilder()
        if (account.length < 8 || password.length < 8) {
            tipsBuilder.append("AppSets不建议账号密码长度小于8位")
        }
        if (account == password)
            tipsBuilder.append(",不建议你使用与账号一样的密码!")
        val tips = tipsBuilder.toString()
        if (tips.isNotEmpty()) {
            tips.toast()
        }
        loginSignUpState.value = UserLoginUseCase.LoginSignUpState.SignUping()
        coroutineScope.requestNotNullRaw({
            val accountEncode = Md5Helper.encode(account)
            val passwordEncode = Md5Helper.encode(password)
            val userLoginRepository = UserLoginRepository(URLApi.provide(UserApi::class.java))
            val preSignUpRes = userLoginRepository.preSignUp(accountEncode)
            if (preSignUpRes.data == null) {
                delay(200)
                loginSignUpState.value =
                    UserLoginUseCase.LoginSignUpState.SignUpFail("注册时出现错误")
                delay(300)
                loginSignUpState.value = UserLoginUseCase.LoginSignUpState.Default
            } else if (preSignUpRes.data == false) {
                delay(200)
                loginSignUpState.value =
                    UserLoginUseCase.LoginSignUpState.SignUpFail("账号已经存在，请更换另一个账号")
                delay(300)
                loginSignUpState.value = UserLoginUseCase.LoginSignUpState.Default
                return@requestNotNullRaw
            }
            var avatarUrlMarker: String? = null
            val avatarImageUri = userAvatarState.value?.uri
            if (avatarImageUri != null) {
                avatarUrlMarker = UUID.randomUUID().toString()
                SimpleFileIO.getInstance().uploadWithUri(context, avatarImageUri, avatarUrlMarker)
            }
            val signUpRes = userLoginRepository.signUp(
                accountEncode, passwordEncode,
                userName, avatarUrlMarker,
                userIntroduction,
                userTags, userSex,
                userAge, userPhone,
                userEmail, userArea,
                userAddress, userWebsite
            )
            if (signUpRes.data == true) {
                delay(1200)
                loginSignUpState.value = UserLoginUseCase.LoginSignUpState.SignUpFinish
                "成功注册AppSets账号".toastSuspend()
            } else {
                delay(200)
                loginSignUpState.value =
                    UserLoginUseCase.LoginSignUpState.SignUpFail("注册时出现错误")
                delay(300)
                loginSignUpState.value = UserLoginUseCase.LoginSignUpState.Default
            }
        }, onFailed = {
            loginSignUpState.value = UserLoginUseCase.LoginSignUpState.SignUpFail("注册时出现错误")
            delay(300)
            loginSignUpState.value = UserLoginUseCase.LoginSignUpState.Default
            it.info.toastSuspend()
        })
    }

    fun createGroup(
        context: Context,
        groupName: String,
        groupMembersCount: String,
        isPublic: Boolean,
        groupIntroduction: String
    ) {
        if (groupName.isEmpty()) {
            "群组名称不能为空".toast()
            return
        }
        coroutineScope.requestNotNullRaw({
            val userRepository = UserRepository(URLApi.provide(UserApi::class.java))
            val preCheckRes = userRepository.createChatGroupPreCheck(groupName)
            if (preCheckRes.data != true) {
                "存在相同名称的群组，请重试！".toastSuspend()
                return@requestNotNullRaw
            }
            var iconUrlMarker: String? = null
            val uri = groupIconState.value?.uri
            if (uri != null) {
                iconUrlMarker = UUID.randomUUID().toString()
                SimpleFileIO.getInstance().uploadWithUri(context, uri, iconUrlMarker)
            }
            val createChatGroupRes = userRepository.createChatGroup(
                groupName, iconUrlMarker,
                groupMembersCount, isPublic, groupIntroduction
            )
            if (createChatGroupRes.data == true) {
                "创建成功".toastSuspend()
                startServiceToSyncGroupsFromServer(context)
            } else {
                "创建失败".toastSuspend()
            }
        }, onFailed = {
            "创建失败".toastSuspend()
        })
    }

    fun setUserSelectGroupIconUri(imageUri: MediaStoreDataUriWrapper?) {
        groupIconState.value = imageUri
    }

    fun clean() {
        userAvatarState.value = null
        groupIconState.value = null
        loginSignUpState.value = null
    }

    companion object {
        fun startServiceToStartRabbit(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra("what_to_do", "to_start_rabbit")
            context.startService(intent)
        }

        fun startServiceToSyncFriendsFromServer(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra("what_to_do", "to_sync_user_friends_from_server")
            context.startService(intent)
        }

        fun startServiceToSyncGroupsFromServer(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra("what_to_do", "to_sync_user_groups_from_server")
            context.startService(intent)
        }

        fun startServiceToSyncAllFromServer(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra("what_to_do", "to_sync_user_data_from_server")
            context.startService(intent)
        }

        fun startServiceToSyncFriendsFromLocal(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra("what_to_do", "to_sync_user_friends_from_local")
            context.startService(intent)
        }

        fun startServiceToSyncGroupsFromLocal(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra("what_to_do", "to_sync_user_groups_from_local")
            context.startService(intent)
        }

        fun startServiceToSyncAllFromLocal(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra("what_to_do", "to_sync_user_data_from_local")
            context.startService(intent)
        }
    }
}