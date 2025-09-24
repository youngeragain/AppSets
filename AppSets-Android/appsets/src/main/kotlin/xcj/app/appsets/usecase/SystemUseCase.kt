package xcj.app.appsets.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.R
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
import xcj.app.appsets.settings.ModuleConfig
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.login.UserAgreementComposeViewProvider
import xcj.app.appsets.ui.model.GroupInfoForCreate
import xcj.app.appsets.ui.model.SelectedContentsStateHolder
import xcj.app.appsets.ui.model.UserInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateGroupPageState
import xcj.app.appsets.ui.model.page_state.LoginSignUpPageState
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.appsets.util.message_digest.MessageDigestUtil
import xcj.app.appsets.util.model.UriProvider
import xcj.app.compose_share.components.VisibilityComposeStateProvider
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.RequestFail
import xcj.app.starter.server.request
import xcj.app.starter.server.requestRaw
import xcj.app.starter.test.LocalAndroidContextFileDir
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.test.LocalPurpleCoroutineScope
import java.util.Calendar

sealed interface AppUpdateState {
    data object None : AppUpdateState

    data object Checking : AppUpdateState
    data class Checked(
        val updateCheckResult: UpdateCheckResult
    ) : AppUpdateState
}

class SystemUseCase(
    private val userRepository: UserRepository,
    private val appSetsRepository: AppSetsRepository,
) : IComposeLifecycleAware {

    val selectedContentsStateHolder: SelectedContentsStateHolder = SelectedContentsStateHolder()

    private val requestIdMap: MutableMap<String, String> = mutableMapOf()

    val appUpdateState: MutableState<AppUpdateState> = mutableStateOf(AppUpdateState.None)

    val loginSignUpPageState: MutableState<LoginSignUpPageState> =
        mutableStateOf(LoginSignUpPageState.LoginDefault)

    val createGroupPageState: MutableState<CreateGroupPageState> = mutableStateOf(
        CreateGroupPageState.NewGroupPage(
            GroupInfoForCreate()
        )
    )


    fun dismissNewVersionTips() {
        val appUpdateState = appUpdateState.value
        if (appUpdateState !is AppUpdateState.Checked) {
            return
        }
        if (appUpdateState.updateCheckResult.forceUpdate == true) {
            return
        }
        PurpleLogger.current.d(TAG, "dismissNewVersionTips")
        this.appUpdateState.value = AppUpdateState.None
    }

    suspend fun initAppToken() {
        PurpleLogger.current.d(TAG, "initAppToken")
        request(
            action = {
                appSetsRepository.getAppToken()
            }).onSuccess {
            LocalAccountManager.saveAppToken(it)
        }.onFailure {
            PurpleLogger.current.e(TAG, "initAppToken failed!")
        }
    }

    suspend fun updateIMBrokerProperties() {
        if (!ModuleConfig.isNeedUpdateImBrokerProperties()) {
            return
        }
        request {
            appSetsRepository.getIMBrokerProperties()
        }.onSuccess { properties ->
            if (properties.isEmpty()) {
                return
            }
            ModuleConfig.updateImBrokerProperties(properties)
        }
    }

    suspend fun checkUpdate() {
        val context = LocalApplication.current
        val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName, PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
        request {
            appSetsRepository.checkUpdate(packageInfo.versionCode)
        }.onSuccess {
            if (!it.canUpdate) {
                return
            }
            delay(1000 * 3)
            it.versionFromTo = "${packageInfo.versionName} → ${it.newestVersion}"
            appUpdateState.value = AppUpdateState.Checked(it)
            if (it.forceUpdate != true) {
                delay(1000 * 300)
                appUpdateState.value = AppUpdateState.None
            }
        }
    }

    suspend fun getUpdateHistory(onFetched: (List<UpdateCheckResult>) -> Unit) {
        request {
            appSetsRepository.getUpdateHistory()
        }.onSuccess { updateCheckResultList ->
            onFetched(updateCheckResultList.sortedByDescending { it.publishDateTime })
        }
    }

    private fun userFeedbackJoinGroupRequest(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: SystemMessage,
        requestJson: GroupRequestJson,
    ) {
        PurpleLogger.current.d(TAG, "userFeedbackJoinGroupRequest")
        imMessage.handling.value = true
        LocalPurpleCoroutineScope.current.launch {
            request {
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
            }.onSuccess {
                PurpleLogger.current.d(
                    TAG, "userFeedbackFriendsRequest, call backend result:$it"
                )/* if(result){
                         startServiceToSyncAllFromServer(context)
                     }*/
                delay(1400)
                imMessage.handling.value = false
                delay(100)
                session.conversationState.removeMessage(imMessage)
            }
        }
    }

    private fun userFeedbackFriendsRequest(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: SystemMessage,
        requestJson: FriendRequestJson,
    ) {
        if (RelationsUseCase.getInstance().hasUserRelated(requestJson.uid)) {
            context.getString(xcj.app.appsets.R.string.you_are_already_friends).toast()
            return
        }
        PurpleLogger.current.d(TAG, "userFeedbackFriendsRequest")
        imMessage.handling.value = true
        LocalPurpleCoroutineScope.current.launch {
            request {
                userRepository.sendAddRequestFriendFeedback(
                    requestJson.requestId, requestJson.uid, result, if (result) {
                        "Agree"
                    } else {
                        "Reject"
                    }
                )
            }.onSuccess {
                PurpleLogger.current.d(
                    TAG, "userFeedbackFriendsRequest, call backend result:$it"
                )
                if (result && it) {
                    startServiceToSyncFriendsFromServer(context)
                }
                delay(1000)
                imMessage.handling.value = false
                delay(100)
                session.conversationState.removeMessage(imMessage)
            }
        }
    }


    fun requestAddFriend(context: Context, uid: String, hello: String, reason: String?) {
        if (RelationsUseCase.getInstance().hasUserRelated(uid)) {
            context.getString(xcj.app.appsets.R.string.you_are_already_friends).toast()
            return
        }
        PurpleLogger.current.d(TAG, "requestAddFriend")
        LocalPurpleCoroutineScope.current.launch {
            requestRaw {
                userRepository.requestAddFriend(uid, hello, reason)
            }.onSuccess {
                val requestId = it.data
                PurpleLogger.current.d(
                    TAG, "requestAddFriend, result: requestId${requestId}"
                )

                if (requestId.isNullOrEmpty()) {
                    it.info.toastSuspend()
                    return@onSuccess
                }

                context.getString(xcj.app.appsets.R.string.friend_request_is_send_please_waiting)
                    .toastSuspend()
                requestIdMap[uid] = requestId
            }
        }
    }

    fun requestJoinGroup(context: Context, groupId: String, hello: String, reason: String?) {
        if (RelationsUseCase.getInstance().hasGroupRelated(groupId)) {
            context.getString(xcj.app.appsets.R.string.you_are_already_in_the_group).toast()
            return
        }
        PurpleLogger.current.d(TAG, "requestJoinGroup")
        LocalPurpleCoroutineScope.current.launch {
            requestRaw {
                userRepository.requestJoinGroup(groupId, hello, reason)
            }.onSuccess {
                val requestId = it.data
                PurpleLogger.current.d(
                    TAG, "requestJoinGroup,  result: requestId${it}"
                )

                if (requestId.isNullOrEmpty()) {
                    it.info.toastSuspend()
                    return@onSuccess
                }
                context.getString(xcj.app.appsets.R.string.group_request_is_send_please_waiting)
                    .toastSuspend()
                requestIdMap.put(groupId, requestId)
            }
        }
    }

    fun flipFollowToUserState(userInfo: UserInfo, userInfoUseCase: UserInfoUseCase) {
        PurpleLogger.current.d(TAG, "flipFollowToUserState")
        LocalPurpleCoroutineScope.current.launch {
            request {
                userRepository.flipFollowToUserState(userInfo.uid)
            }.onSuccess {
                PurpleLogger.current.d(
                    TAG, "flipFollowToUserState, result:${it}"
                )
                userInfoUseCase.updateUserFollowState()
            }
        }
    }

    fun handleUserRequestResult(
        context: Context,
        result: Boolean,
        session: Session,
        imMessage: SystemMessage,
    ) {
        PurpleLogger.current.d(TAG, "handleUserRequestResult")
        val systemContentInterface = imMessage.systemContentInterface
        when (systemContentInterface) {
            is FriendRequestJson -> {
                userFeedbackFriendsRequest(
                    context, result, session, imMessage, systemContentInterface
                )
            }

            is GroupRequestJson -> {
                userFeedbackJoinGroupRequest(
                    context, result, session, imMessage, systemContentInterface
                )
            }

            else -> Unit
        }
    }

    fun cleanCaches() {
        LocalPurpleCoroutineScope.current.launch {
            LocalAndroidContextFileDir.current.cleanCaches()
        }
    }

    fun updateSignUpUserSelectAvatarUri(uriProvider: UriProvider) {
        UserInfoForCreate.updateStateUserAvatar(loginSignUpPageState, uriProvider)
    }

    fun updateGroupCreateIconUri(uriProvider: UriProvider) {
        GroupInfoForCreate.updateGroupCreateIconUri(createGroupPageState, uriProvider)
    }

    suspend fun login(
        context: Context,
        account: String,
        password: String,
        visibilityComposeStateProvider: VisibilityComposeStateProvider,
    ) {
        PurpleLogger.current.d(TAG, "login")
        val signUpState = loginSignUpPageState.value
        if (signUpState is LoginSignUpPageState.Logging) {
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
        loginSignUpPageState.value = LoginSignUpPageState.Logging()
        requestRaw(action = {
            delay(500)
            val accountEncode = MessageDigestUtil.transformWithMD5(account)?.outContent
            val passwordEncode = MessageDigestUtil.transformWithMD5(password)?.outContent
            if (accountEncode.isNullOrEmpty() || passwordEncode.isNullOrEmpty()) {
                return
            }
            val loginResponse = userRepository.login(
                accountEncode, passwordEncode
            )
            val token = loginResponse.data

            if (!loginResponse.success || token.isNullOrEmpty()) {
                PurpleLogger.current.d(TAG, "login, failed, token get failed!")
                loginSignUpPageState.value = LoginSignUpPageState.LoggingFail()
                loginResponse.info.toastSuspend()
                return
            }

            LocalAccountManager.onUserLogged(UserInfo.default(), token, isTemp = true)

            val userInfoResponse = userRepository.getLoggedUserInfo()
            val userInfo = userInfoResponse.data
            if (userInfo == null) {
                PurpleLogger.current.d(
                    TAG, "login, failed, userInfo isNullOrEmpty!"
                )
                loginSignUpPageState.value = LoginSignUpPageState.LoggingFail()
                return
            }

            if (userInfo.agreeToTheAgreement == 1) {
                LocalAccountManager.onUserLogged(userInfo, token, false)
                loginSignUpPageState.value = LoginSignUpPageState.LoggingFinish()
                return
            }
            val bottomSheetContainerState = visibilityComposeStateProvider.bottomSheetState()
            val provider = UserAgreementComposeViewProvider(onNextClick = {
                bottomSheetContainerState.hide()
                LocalAccountManager.onUserLogged(userInfo, token, false)
                loginSignUpPageState.value = LoginSignUpPageState.LoggingFinish()
            })
            delay(500)
            bottomSheetContainerState.show(provider)
            delay(2000)
            loginSignUpPageState.value = LoginSignUpPageState.LoginDefault
        }).onFailure {
            PurpleLogger.current.d(TAG, "login failed")
            logout()
            loginSignUpPageState.value = LoginSignUpPageState.LoggingFail()
        }
    }

    private fun logout() {
        PurpleLogger.current.d(TAG, "logout")

        LocalAccountManager.onUserLogout(LocalAccountManager.LOGOUT_BY_MANUALLY)
        LocalPurpleCoroutineScope.current.launch {
            request(
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

    suspend fun signUp(
        context: Context,
    ) {
        if (!ModuleConfig.moduleConfiguration.canSignUp) {
            context.getString(xcj.app.appsets.R.string.current_version_cannot_be_registered).toast()
            return
        }
        val oldLoginSignUpState = loginSignUpPageState.value
        if (oldLoginSignUpState !is LoginSignUpPageState.SignUpDefault) {
            return
        }
        val signUpUserInfo = oldLoginSignUpState.userInfoForCreate
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
        loginSignUpPageState.value = LoginSignUpPageState.SignUpping(signUpUserInfo)
        requestRaw(action = {
            val accountEncode =
                MessageDigestUtil.transformWithMD5(signUpUserInfo.account)?.outContent
            val passwordEncode =
                MessageDigestUtil.transformWithMD5(signUpUserInfo.password)?.outContent
            if (accountEncode.isNullOrEmpty() || passwordEncode.isNullOrEmpty()) {
                loginSignUpPageState.value = LoginSignUpPageState.SignUpDefault(signUpUserInfo)
                return
            }
            val preSignUpRes = userRepository.preSignUp(accountEncode)
            val canSignUp = preSignUpRes.data
            if (canSignUp == null) {
                loginSignUpPageState.value = LoginSignUpPageState.SignUpPageFail(
                    signUpUserInfo, R.string.register_failed
                )
                delay(1000)
                context.getString(xcj.app.appsets.R.string.register_failed).toastSuspend()
                loginSignUpPageState.value = LoginSignUpPageState.LoginDefault
                return
            } else if (!canSignUp) {
                loginSignUpPageState.value = LoginSignUpPageState.SignUpPageFail(
                    signUpUserInfo, R.string.a_account_exist_please_retry
                )
                context.getString(xcj.app.appsets.R.string.a_account_exist_please_retry)
                    .toastSuspend()
                delay(1000)
                loginSignUpPageState.value = LoginSignUpPageState.SignUpDefault(signUpUserInfo)
                return
            }
            val signUpRes = userRepository.signUp(
                context, accountEncode, passwordEncode, signUpUserInfo
            )
            val signUpSuccess = signUpRes.data
            if (signUpSuccess != true) {
                loginSignUpPageState.value = LoginSignUpPageState.SignUpPageFail(
                    signUpUserInfo, R.string.register_failed
                )
                context.getString(xcj.app.appsets.R.string.register_failed).toastSuspend()
                delay(1000)
                loginSignUpPageState.value = LoginSignUpPageState.SignUpDefault(signUpUserInfo)
                return
            }
            loginSignUpPageState.value = LoginSignUpPageState.SignUpFinish(signUpUserInfo)
            context.getString(xcj.app.appsets.R.string.register_appsets_success).toastSuspend()
        }).onFailure {
            loginSignUpPageState.value = LoginSignUpPageState.SignUpPageFail(
                signUpUserInfo, R.string.register_failed
            )
            context.getString(xcj.app.appsets.R.string.register_failed).toastSuspend()
            delay(1000)
            loginSignUpPageState.value = LoginSignUpPageState.SignUpDefault(signUpUserInfo)

            (it as? RequestFail)?.info?.toastSuspend()
        }
    }

    fun createGroup(
        context: Context,
    ) {
        val groupCreateState = createGroupPageState.value
        if (groupCreateState !is CreateGroupPageState.NewGroupPage) {
            return
        }
        val groupCreateInfo = groupCreateState.groupInfoForCreate
        if (groupCreateInfo.name.isEmpty()) {
            context.getString(xcj.app.appsets.R.string.group_name_can_not_be_empty).toast()
            return
        }
        PurpleLogger.current.d(TAG, "createGroup")
        createGroupPageState.value = CreateGroupPageState.Creating(groupCreateInfo)
        LocalPurpleCoroutineScope.current.launch {
            requestRaw(action = {
                val preCheckRes = userRepository.createChatGroupPreCheck(groupCreateInfo.name)
                if (preCheckRes.data != true) {
                    context.getString(xcj.app.appsets.R.string.a_group_name_existed_please_retry)
                        .toastSuspend()
                    createGroupPageState.value = CreateGroupPageState.NewGroupPage(groupCreateInfo)
                    return@requestRaw
                }

                val createChatGroupRes = userRepository.createChatGroup(
                    context, groupCreateInfo
                )
                if (createChatGroupRes.data == true) {
                    context.getString(xcj.app.appsets.R.string.create_success).toastSuspend()
                    startServiceToSyncGroupsFromServer(context)
                    createGroupPageState.value =
                        CreateGroupPageState.CreateFinishPage(groupCreateInfo)
                    delay(300)
                    createGroupPageState.value = CreateGroupPageState.NewGroupPage(groupCreateInfo)
                } else {
                    context.getString(xcj.app.appsets.R.string.create_failed).toastSuspend()
                    createGroupPageState.value =
                        CreateGroupPageState.CreateFailedPage(groupCreateInfo)
                    delay(300)
                    createGroupPageState.value = CreateGroupPageState.NewGroupPage(groupCreateInfo)
                }
            }).onFailure {
                context.getString(xcj.app.appsets.R.string.create_failed).toastSuspend()
                createGroupPageState.value = CreateGroupPageState.CreateFailedPage(groupCreateInfo)
                delay(300)
                createGroupPageState.value = CreateGroupPageState.NewGroupPage(groupCreateInfo)
            }
        }
    }

    override fun onComposeDispose(by: String?) {
        createGroupPageState.value = CreateGroupPageState.NewGroupPage(GroupInfoForCreate())
        loginSignUpPageState.value = LoginSignUpPageState.LoginDefault
    }

    suspend fun getWeatherInfo(
        locationName: String,
        longitude: String,
        latitude: String,
    ): WeatherInfo? {
        val temperatureInfo = listOf(
            TemperatureInfo(Calendar.getInstance().time.time, 0f),
        )
        return WeatherInfo(temperatureInfo)
    }

    suspend fun restoreLoginStatusStateIfNeeded() {
        LocalAccountManager.restoreLoginStatusStateIfNeeded()
    }


    fun prepareSignUpState() {
        loginSignUpPageState.value = LoginSignUpPageState.SignUpDefault(UserInfoForCreate())
    }

    fun prepareLoginState() {
        loginSignUpPageState.value = LoginSignUpPageState.LoginDefault
    }

    companion object {

        private const val TAG = "SystemUseCase"

        private var INSTANCE: SystemUseCase? = null

        fun getInstance(): SystemUseCase {
            return INSTANCE ?: run {
                val useCase = SystemUseCase(
                    UserRepository.getInstance(), AppSetsRepository.getInstance()
                )
                INSTANCE = useCase
                useCase
            }
        }


        fun startServiceToSyncFriendsFromServer(context: Context) {
            val intent = Intent(context, MainService::class.java)
            intent.putExtra(
                MainService.KEY_WHAT_TO_DO, MainService.DO_TO_SYNC_USER_FRIENDS_FROM_SERVER
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
                MainService.KEY_WHAT_TO_DO, MainService.DO_TO_SYNC_USER_GROUPS_FROM_SERVER
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
                MainService.KEY_WHAT_TO_DO, MainService.DO_TO_SYNC_USER_DATA_FROM_SERVER
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
                MainService.KEY_WHAT_TO_DO, MainService.DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL
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
                MainService.KEY_WHAT_TO_DO, MainService.DO_TO_SYNC_USER_GROUPS_FROM_LOCAL
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
                    context.packageName, PackageManager.PackageInfoFlags.of(0)
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