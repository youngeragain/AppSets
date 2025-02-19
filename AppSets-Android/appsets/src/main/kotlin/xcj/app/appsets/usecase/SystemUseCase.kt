package xcj.app.appsets.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupRequestJson
import xcj.app.appsets.server.model.TemperatureInfo
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.WeatherInfo
import xcj.app.appsets.server.repository.AppSetsRepository
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.service.MainService
import xcj.app.appsets.settings.AppConfig
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.content_selection.ContentSelectionResults
import xcj.app.appsets.ui.compose.login.UserAgreementComposeViewProvider
import xcj.app.appsets.ui.model.LoginSignUpState
import xcj.app.appsets.ui.model.SignUpUserInfo
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.appsets.util.message_digest.MessageDigestUtil
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.components.AnyStateProvider
import xcj.app.compose_share.dynamic.IComposeDispose
import xcj.app.compose_share.ui.viewmodel.AnyStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.requestNotNull
import xcj.app.starter.server.requestNotNullRaw
import xcj.app.starter.test.LocalAndroidContextFileDir
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.test.LocalPurpleCoroutineScope
import java.util.Calendar

data class SelectedContents(
    val contents: MutableMap<String, ContentSelectionResults> = mutableMapOf()
)

class SelectedContentsStateHolder {

    val selectedContentsState: MutableState<SelectedContents> = mutableStateOf(SelectedContents())

    fun updateSelectedContent(contentSelectionResults: ContentSelectionResults) {
        val selectedContents = selectedContentsState.value
        selectedContents.contents.put(contentSelectionResults.requestKey, contentSelectionResults)
        selectedContentsState.value = SelectedContents(selectedContents.contents)
    }
}


data class GroupCreateInfo(
    val name: String = "",
    val membersCount: String = "",
    val isPublic: Boolean = false,
    val introduction: String = "",
    val icon: UriProvider? = null
) {
    companion object {
        fun updateGroupCreateIconUri(
            state: MutableState<CreateGroupState>,
            uriProvider: UriProvider?
        ) {
            val newGroupState = state.value as? CreateGroupState.NewGroup ?: return
            state.value = newGroupState.copy(newGroupState.groupCreateInfo.copy(icon = uriProvider))
        }

        fun updateGroupCreatePublicStatus(
            state: MutableState<CreateGroupState>,
            isPublic: Boolean
        ) {
            val newGroupState = state.value as? CreateGroupState.NewGroup ?: return
            state.value =
                newGroupState.copy(newGroupState.groupCreateInfo.copy(isPublic = isPublic))
        }

        fun updateGroupCreateName(state: MutableState<CreateGroupState>, string: String) {
            val newGroupState = state.value as? CreateGroupState.NewGroup ?: return
            state.value = newGroupState.copy(newGroupState.groupCreateInfo.copy(name = string))
        }

        fun updateGroupCreateMembersCount(state: MutableState<CreateGroupState>, string: String) {
            val newGroupState = state.value as? CreateGroupState.NewGroup ?: return
            state.value =
                newGroupState.copy(newGroupState.groupCreateInfo.copy(membersCount = string))
        }

        fun updateGroupCreateDescription(
            state: MutableState<CreateGroupState>,
            string: String
        ) {
            val newGroupState = state.value as? CreateGroupState.NewGroup ?: return
            state.value =
                newGroupState.copy(newGroupState.groupCreateInfo.copy(introduction = string))
        }
    }
}

sealed interface CreateGroupState {
    val groupCreateInfo: GroupCreateInfo

    data class NewGroup(override val groupCreateInfo: GroupCreateInfo) : CreateGroupState
    data class Creating(override val groupCreateInfo: GroupCreateInfo) : CreateGroupState
    data class CreateFailed(override val groupCreateInfo: GroupCreateInfo) : CreateGroupState
    data class CreateFinish(override val groupCreateInfo: GroupCreateInfo) : CreateGroupState
}

class SystemUseCase(
    private val userRepository: UserRepository,
    private val appSetsRepository: AppSetsRepository,
    private val coroutineScope: CoroutineScope = LocalPurpleCoroutineScope.current,
) : IComposeDispose {

    val selectedContentsStateHolder: SelectedContentsStateHolder = SelectedContentsStateHolder()

    private val requestIdMap: MutableMap<String, String> = mutableMapOf()

    private var newVersionStatePendingDismissJob: Job? = null

    val newVersionState: MutableState<UpdateCheckResult?> = mutableStateOf(null)

    val loginSignUpState: MutableState<LoginSignUpState> = mutableStateOf(LoginSignUpState.Nothing)

    val createGroupState: MutableState<CreateGroupState> = mutableStateOf(
        CreateGroupState.NewGroup(
            GroupCreateInfo()
        )
    )


    /**
     * 更新历史
     */
    val updateHistory: MutableList<UpdateCheckResult> = mutableStateListOf()

    init {
        initAppToken()
    }

    fun dismissNewVersionTips() {
        val updateCheckResult = newVersionState.value ?: return
        if (updateCheckResult.forceUpdate == true) {
            return
        }
        newVersionStatePendingDismissJob?.cancel()
        newVersionStatePendingDismissJob = null
        newVersionState.value = null
        PurpleLogger.current.d(TAG, "dismissNewVersionTips")
    }

    /**
     * 部分数据可以直接公开，不过也需要有访问权限
     */
    private fun initAppToken() {
        PurpleLogger.current.d(TAG, "initAppToken")
        coroutineScope.launch {
            requestNotNull(
                action = {
                    appSetsRepository.getAppToken()
                },
                onSuccess = {
                    LocalAccountManager.saveAppToken(it)
                },
                onFailed = {
                    PurpleLogger.current.e(TAG, "initAppToken failed:${it.info}")
                }
            )
        }
    }

    fun updateIMBrokerProperties() {
        if (!AppConfig.isNeedUpdateImBrokerProperties()) {
            return
        }
        coroutineScope.launch {
            requestNotNull(
                action = {
                    appSetsRepository.getIMBrokerProperties()
                },
                onSuccess = { properties ->
                    if (properties.isEmpty()) {
                        return@requestNotNull
                    }
                    AppConfig.updateImBrokerProperties(properties)
                }
            )
        }
    }

    fun checkUpdate() {
        val context = LocalApplication.current
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        coroutineScope.launch {
            requestNotNull(
                action = {
                    appSetsRepository.checkUpdate(packageInfo.versionCode)
                },
                onSuccess = {
                    if (!it.canUpdate) {
                        return@requestNotNull
                    }
                    delay(4000)
                    it.versionFromTo = "${packageInfo.versionName} → ${it.newestVersion}"
                    newVersionState.value = it
                    if (it.forceUpdate != true) {
                        newVersionStatePendingDismissJob = launch {
                            delay(1000 * 300)
                            newVersionState.value = null
                        }
                    }
                }
            )
        }
    }

    fun getUpdateHistory() {
        if (updateHistory.isNotEmpty()) {
            return
        }
        coroutineScope.launch {
            requestNotNull(
                action = {
                    appSetsRepository.getUpdateHistory()
                },
                onSuccess = { updateCheckResultList ->
                    updateHistory.addAll(updateCheckResultList.sortedByDescending { it.publishDateTime })
                }
            )
        }
    }


    fun cleanUpdateHistory() {
        updateHistory.clear()
    }

    private fun userFeedbackJoinGroupRequest(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: SystemMessage,
        requestJson: GroupRequestJson
    ) {
        PurpleLogger.current.d(TAG, "userFeedbackJoinGroupRequest")
        imMessage.handling.value = true
        coroutineScope.launch {
            requestNotNull(
                action = {
                    userRepository.sendRequestJoinGroupFeedback(
                        requestJson.requestId,
                        requestJson.uid,
                        requestJson.groupId,
                        result,
                        if (result) {
                            "Agree"
                        } else {
                            "Reject"
                        }
                    )
                },
                onSuccess = {
                    PurpleLogger.current.d(
                        TAG,
                        "userFeedbackFriendsRequest, call backend result:$it"
                    )
                    /* if(result){
                         startServiceToSyncAllFromServer(context)
                     }*/
                    delay(1400)
                    imMessage.handling.value = false
                    delay(100)
                    session.conversationState.removeMessage(imMessage)
                }
            )
        }
    }

    private fun userFeedbackFriendsRequest(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: SystemMessage,
        requestJson: FriendRequestJson
    ) {
        if (RelationsUseCase.getInstance().hasUserRelated(requestJson.uid)) {
            context.getString(xcj.app.appsets.R.string.you_are_already_friends).toast()
            return
        }
        PurpleLogger.current.d(TAG, "userFeedbackFriendsRequest")
        imMessage.handling.value = true
        coroutineScope.launch {
            requestNotNull(
                action = {
                    userRepository.sendAddRequestFriendFeedback(
                        requestJson.requestId,
                        requestJson.uid,
                        result,
                        if (result) {
                            "Agree"
                        } else {
                            "Reject"
                        }
                    )
                },
                onSuccess = {
                    PurpleLogger.current.d(
                        TAG,
                        "userFeedbackFriendsRequest, call backend result:$it"
                    )
                    if (result && it) {
                        startServiceToSyncFriendsFromServer(context)
                    }
                    delay(1000)
                    imMessage.handling.value = false
                    delay(100)
                    session.conversationState.removeMessage(imMessage)
                }
            )
        }
    }


    fun requestAddFriend(context: Context, uid: String, hello: String, reason: String?) {
        if (RelationsUseCase.getInstance().hasUserRelated(uid)) {
            context.getString(xcj.app.appsets.R.string.you_are_already_friends).toast()
            return
        }
        PurpleLogger.current.d(TAG, "requestAddFriend")
        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    userRepository.requestAddFriend(uid, hello, reason)
                },
                onSuccess = {
                    val requestId = it.data
                    PurpleLogger.current.d(
                        TAG,
                        "requestAddFriend, result: requestId${requestId}"
                    )

                    if (requestId.isNullOrEmpty()) {
                        it.info.toastSuspend()
                        return@requestNotNullRaw
                    }

                    context.getString(xcj.app.appsets.R.string.friend_request_is_send_please_waiting)
                        .toastSuspend()
                    requestIdMap[uid] = requestId
                }
            )
        }
    }

    fun requestJoinGroup(context: Context, groupId: String, hello: String, reason: String?) {
        if (RelationsUseCase.getInstance().hasGroupRelated(groupId)) {
            context.getString(xcj.app.appsets.R.string.you_are_already_in_the_group).toast()
            return
        }
        PurpleLogger.current.d(TAG, "requestJoinGroup")
        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    userRepository.requestJoinGroup(groupId, hello, reason)
                },
                onSuccess = {
                    val requestId = it.data
                    PurpleLogger.current.d(
                        TAG,
                        "requestJoinGroup,  result: requestId${it}"
                    )

                    if (requestId.isNullOrEmpty()) {
                        it.info.toastSuspend()
                        return@requestNotNullRaw
                    }
                    context.getString(xcj.app.appsets.R.string.group_request_is_send_please_waiting)
                        .toastSuspend()
                    requestIdMap.put(groupId, requestId)
                }
            )
        }
    }

    fun flipFollowToUserState(userInfo: UserInfo, callback: (() -> Unit)? = null) {
        PurpleLogger.current.d(TAG, "flipFollowToUserState")
        coroutineScope.launch {
            requestNotNull(
                action = {
                    userRepository.flipFollowToUserState(userInfo.uid)
                },
                onSuccess = {
                    PurpleLogger.current.d(
                        TAG,
                        "flipFollowToUserState, result:${it}"
                    )
                    callback?.invoke()
                }
            )
        }
    }

    fun handleUserRequestResult(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: SystemMessage
    ) {
        PurpleLogger.current.d(TAG, "handleUserRequestResult")
        val systemContentInterface = imMessage.systemContentInterface
        when (systemContentInterface) {
            is FriendRequestJson -> {
                userFeedbackFriendsRequest(
                    context, result, session, imMessage,
                    systemContentInterface
                )
            }

            is GroupRequestJson -> {
                userFeedbackJoinGroupRequest(
                    context, result, session, imMessage,
                    systemContentInterface
                )
            }

            else -> Unit
        }
    }

    fun cleanCaches() {
        coroutineScope.launch(Dispatchers.IO) {
            LocalAndroidContextFileDir.current.cleanCaches()
        }
    }

    fun updateSignUpUserSelectAvatarUri(uriProvider: UriProvider) {
        SignUpUserInfo.updateStateUserAvatar(loginSignUpState, uriProvider)
    }

    fun updateGroupCreateIconUri(uriProvider: UriProvider) {
        GroupCreateInfo.updateGroupCreateIconUri(createGroupState, uriProvider)
    }

    fun login(
        context: Context,
        account: String,
        password: String,
        anyStateProvider: AnyStateProvider
    ) {
        PurpleLogger.current.d(TAG, "login")
        val signUpState = loginSignUpState.value
        if (signUpState is LoginSignUpState.Logging) {
            return
        }
        if (account.isEmpty()) {
            context.getString(xcj.app.appsets.R.string.please_input_account).toast()
            return
        }
        if (password.isEmpty()) {
            context.getString(xcj.app.appsets.R.string.please_input_password).toast()
            return
        }
        loginSignUpState.value = LoginSignUpState.Logging
        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    delay(500)
                    val accountEncode = MessageDigestUtil.transformWithMD5(account)?.outContent
                    val passwordEncode = MessageDigestUtil.transformWithMD5(password)?.outContent
                    if (accountEncode.isNullOrEmpty() || passwordEncode.isNullOrEmpty()) {
                        return@requestNotNullRaw
                    }
                    val loginResponse =
                        userRepository.login(
                            accountEncode,
                            passwordEncode
                        )
                    val token = loginResponse.data

                    if (!loginResponse.success || token.isNullOrEmpty()) {
                        PurpleLogger.current.d(TAG, "login, failed, token get failed!")
                        loginSignUpState.value = LoginSignUpState.LoggingFail
                        loginResponse.info.toastSuspend()
                        return@requestNotNullRaw
                    }

                    LocalAccountManager.onUserLogged(UserInfo.default(), token, isTemp = true)

                    val userInfoResponse =
                        userRepository.getLoggedUserInfo()
                    val userInfo = userInfoResponse.data
                    if (userInfo == null) {
                        PurpleLogger.current.d(
                            TAG,
                            "login, failed, userInfo isNullOrEmpty!"
                        )
                        loginSignUpState.value = LoginSignUpState.LoggingFail
                        return@requestNotNullRaw
                    }

                    if (userInfo.agreeToTheAgreement == 1) {
                        LocalAccountManager.onUserLogged(userInfo, token, false)
                        loginSignUpState.value = LoginSignUpState.LoggingFinish
                        return@requestNotNullRaw
                    }
                    val bottomSheetContainerState = anyStateProvider.bottomSheetState()
                    val provider = UserAgreementComposeViewProvider(onNextClick = {
                        bottomSheetContainerState.hide()
                        LocalAccountManager.onUserLogged(userInfo, token, false)
                        loginSignUpState.value = LoginSignUpState.LoggingFinish
                    })
                    delay(500)
                    bottomSheetContainerState.show(provider)
                    delay(2000)
                    loginSignUpState.value = LoginSignUpState.Nothing
                },
                onFailed = {
                    PurpleLogger.current.d(TAG, "login failed, ${it.info}")
                    logout()
                    loginSignUpState.value = LoginSignUpState.LoggingFail
                }
            )
        }
    }

    private fun logout() {
        PurpleLogger.current.d(TAG, "logout")

        LocalAccountManager.onUserLogout(LocalAccountManager.LOGOUT_BY_MANUALLY)
        coroutineScope.launch {
            requestNotNull(
                action = userRepository::signOut
            )
        }
    }

    fun loginToggle(context: Context, navController: NavController) {
        if (LocalAccountManager.isLogged()) {
            logout()
        } else {
            navController.navigate(PageRouteNames.LoginPage)
        }
    }

    fun signUp(
        context: Context,
    ) {
        if (!AppConfig.appConfiguration.canSignUp) {
            context.getString(xcj.app.appsets.R.string.current_version_cannot_regiest).toast()
            return
        }
        val oldLoginSignUpState = loginSignUpState.value
        if (oldLoginSignUpState !is LoginSignUpState.SignUp) {
            return
        }
        val signUpUserInfo = oldLoginSignUpState.signUpUserInfo
        if (signUpUserInfo.account.isEmpty()) {
            context.getString(xcj.app.appsets.R.string.please_input_account).toast()
            return
        }
        if (signUpUserInfo.password.isEmpty()) {
            context.getString(xcj.app.appsets.R.string.please_input_password).toast()
            return
        }
        val avatarImageUri = signUpUserInfo.userAvatar?.provideUri()
        if (avatarImageUri == null) {
            context.getString(xcj.app.appsets.R.string.please_choose_avatar).toast()
            return
        }
        if (signUpUserInfo.userName.isEmpty()) {
            context.getString(xcj.app.appsets.R.string.please_input_name).toast()
            return
        }
        PurpleLogger.current.d(TAG, "signUp")
        loginSignUpState.value = LoginSignUpState.SignUping(signUpUserInfo)
        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    val accountEncode =
                        MessageDigestUtil.transformWithMD5(signUpUserInfo.account)?.outContent
                    val passwordEncode =
                        MessageDigestUtil.transformWithMD5(signUpUserInfo.password)?.outContent
                    if (accountEncode.isNullOrEmpty() || passwordEncode.isNullOrEmpty()) {
                        loginSignUpState.value = LoginSignUpState.SignUp(signUpUserInfo)
                        return@requestNotNullRaw
                    }
                    val preSignUpRes = userRepository.preSignUp(accountEncode)
                    val canSignUp = preSignUpRes.data
                    if (canSignUp == null) {
                        loginSignUpState.value =
                            LoginSignUpState.SignUpFail(
                                signUpUserInfo,
                                xcj.app.appsets.R.string.register_failed
                            )
                        delay(1000)
                        context.getString(xcj.app.appsets.R.string.register_failed).toastSuspend()
                        loginSignUpState.value = LoginSignUpState.Nothing
                        return@requestNotNullRaw
                    } else if (canSignUp == false) {
                        loginSignUpState.value =
                            LoginSignUpState.SignUpFail(
                                signUpUserInfo,
                                xcj.app.appsets.R.string.a_account_exist_please_retry
                            )
                        context.getString(xcj.app.appsets.R.string.a_account_exist_please_retry)
                            .toastSuspend()
                        delay(1000)
                        loginSignUpState.value = LoginSignUpState.SignUp(signUpUserInfo)
                        return@requestNotNullRaw
                    }
                    val signUpRes = userRepository.signUp(
                        context,
                        accountEncode,
                        passwordEncode,
                        signUpUserInfo
                    )
                    val signUpSuccess = signUpRes.data
                    if (signUpSuccess != true) {
                        loginSignUpState.value =
                            LoginSignUpState.SignUpFail(
                                signUpUserInfo,
                                xcj.app.appsets.R.string.register_failed
                            )
                        context.getString(xcj.app.appsets.R.string.register_failed).toastSuspend()
                        delay(1000)
                        loginSignUpState.value = LoginSignUpState.SignUp(signUpUserInfo)
                        return@requestNotNullRaw
                    }
                    loginSignUpState.value = LoginSignUpState.SignUpFinish(signUpUserInfo)
                    context.getString(xcj.app.appsets.R.string.register_appsets_success)
                        .toastSuspend()
                },
                onFailed = {
                    loginSignUpState.value =
                        LoginSignUpState.SignUpFail(
                            signUpUserInfo,
                            xcj.app.appsets.R.string.register_failed
                        )
                    context.getString(xcj.app.appsets.R.string.register_failed).toastSuspend()
                    delay(1000)
                    loginSignUpState.value = LoginSignUpState.SignUp(signUpUserInfo)
                    it.info.toastSuspend()
                }
            )
        }
    }

    fun createGroup(
        context: Context
    ) {
        val groupCreateState = createGroupState.value
        if (groupCreateState !is CreateGroupState.NewGroup) {
            return
        }
        val groupCreateInfo = groupCreateState.groupCreateInfo
        if (groupCreateInfo.name.isEmpty()) {
            context.getString(xcj.app.appsets.R.string.group_name_can_not_be_empty).toast()
            return
        }
        PurpleLogger.current.d(TAG, "createGroup")
        createGroupState.value = CreateGroupState.Creating(groupCreateInfo)
        coroutineScope.launch {
            requestNotNullRaw(
                action = {
                    val preCheckRes =
                        userRepository.createChatGroupPreCheck(groupCreateInfo.name)
                    if (preCheckRes.data != true) {
                        context.getString(xcj.app.appsets.R.string.a_group_name_existed_please_retry)
                            .toastSuspend()
                        createGroupState.value = CreateGroupState.NewGroup(groupCreateInfo)
                        return@requestNotNullRaw
                    }

                    val createChatGroupRes = userRepository.createChatGroup(
                        context,
                        groupCreateInfo
                    )
                    if (createChatGroupRes.data == true) {
                        context.getString(xcj.app.appsets.R.string.create_success).toastSuspend()
                        startServiceToSyncGroupsFromServer(context)
                        createGroupState.value = CreateGroupState.CreateFinish(groupCreateInfo)
                        delay(300)
                        createGroupState.value = CreateGroupState.NewGroup(groupCreateInfo)
                    } else {
                        context.getString(xcj.app.appsets.R.string.create_failed).toastSuspend()
                        createGroupState.value = CreateGroupState.CreateFailed(groupCreateInfo)
                        delay(300)
                        createGroupState.value = CreateGroupState.NewGroup(groupCreateInfo)
                    }
                },
                onFailed = {
                    context.getString(xcj.app.appsets.R.string.create_failed).toastSuspend()
                    createGroupState.value = CreateGroupState.CreateFailed(groupCreateInfo)
                    delay(300)
                    createGroupState.value = CreateGroupState.NewGroup(groupCreateInfo)
                }
            )
        }
    }

    override fun onComposeDispose(by: String?) {
        createGroupState.value = CreateGroupState.NewGroup(GroupCreateInfo())
        loginSignUpState.value = LoginSignUpState.Nothing
    }

    suspend fun getWeatherInfo(
        locationName: String,
        longitude: String,
        latitude: String
    ): WeatherInfo? {
        val temperatureInfo = listOf(
            TemperatureInfo(Calendar.getInstance().time.time, 0f),
        )
        return WeatherInfo(temperatureInfo)
    }

    fun restoreLoginStatusStateIfNeeded() {
        coroutineScope.launch {
            LocalAccountManager.restoreLoginStatusStateIfNeeded()
        }
    }


    fun prepareSignUpState() {
        loginSignUpState.value = LoginSignUpState.SignUp(SignUpUserInfo())
    }

    fun prepareLoginState() {
        loginSignUpState.value = LoginSignUpState.Nothing
    }

    companion object {

        private const val TAG = "SystemUseCase"

        private var INSTANCE: SystemUseCase? = null

        fun getInstance(): SystemUseCase {
            return INSTANCE ?: run {
                val useCase = SystemUseCase(
                    UserRepository.getInstance(),
                    AppSetsRepository.getInstance()
                )
                INSTANCE = useCase
                useCase
            }
        }


        fun startServiceToSyncFriendsFromServer(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(
                MainService.KEY_WHAT_TO_DO,
                MainService.DO_TO_SYNC_USER_FRIENDS_FROM_SERVER
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startServiceToSyncGroupsFromServer(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(
                MainService.KEY_WHAT_TO_DO,
                MainService.DO_TO_SYNC_USER_GROUPS_FROM_SERVER
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startServiceToSyncAllFromServer(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(
                MainService.KEY_WHAT_TO_DO,
                MainService.DO_TO_SYNC_USER_DATA_FROM_SERVER
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startServiceToSyncFriendsFromLocal(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(
                MainService.KEY_WHAT_TO_DO,
                MainService.DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startServiceToSyncGroupsFromLocal(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(
                MainService.KEY_WHAT_TO_DO,
                MainService.DO_TO_SYNC_USER_GROUPS_FROM_LOCAL
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun startServiceToSyncAllFromLocal(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(MainService.KEY_WHAT_TO_DO, MainService.DO_TO_SYNC_USER_DATA_FROM_LOCAL)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun getAppSetsPackageVersionName(context: Context): String? {
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName,
                    PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
            return packageInfo.versionName
        }

        fun providePrivacy(context: Context): String {
            return context.getString(xcj.app.appsets.R.string.user_agreement)
        }
    }
}