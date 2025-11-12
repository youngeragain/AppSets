package xcj.app.appsets.usecase

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
import xcj.app.appsets.service.DataSyncService
import xcj.app.appsets.service.IMService
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.settings.ModuleConfig
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.login.UserAgreementComposeViewProvider
import xcj.app.appsets.ui.model.GroupInfoForCreate
import xcj.app.appsets.ui.model.UserInfoForCreate
import xcj.app.appsets.ui.model.page_state.CreateGroupPageUIState
import xcj.app.appsets.ui.model.page_state.LoginPageUIState
import xcj.app.appsets.ui.model.page_state.SignUpPageUIState
import xcj.app.appsets.ui.model.state.NowSpaceContent
import xcj.app.appsets.util.compose_state.ComposeStateUpdater
import xcj.app.appsets.util.compose_state.SingleStateUpdater
import xcj.app.appsets.util.ktx.toast
import xcj.app.appsets.util.ktx.toastSuspend
import xcj.app.appsets.util.message_digest.MessageDigestUtil
import xcj.app.compose_share.components.BottomSheetVisibilityComposeState
import xcj.app.compose_share.components.VisibilityComposeStateProvider
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.request
import xcj.app.starter.server.requestRaw
import xcj.app.starter.test.LocalAndroidContextFileDir
import xcj.app.starter.test.LocalApplication
import xcj.app.starter.test.LocalPurpleCoroutineScope
import java.util.Calendar

class SystemUseCase(
    private val userRepository: UserRepository,
    private val appSetsRepository: AppSetsRepository,
) : ComposeLifecycleAware {

    private val requestIdMap: MutableMap<String, String> = mutableMapOf()

    suspend fun initAppToken() {
        PurpleLogger.current.d(TAG, "initAppToken")
        request {
            appSetsRepository.getAppToken()
        }.onSuccess {
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

    suspend fun showPlatformPermissionUsageTipsIfNeeded(
        nowSpaceContentUseCase: NowSpaceContentUseCase,
        showFlow: Flow<Boolean> = flowOf(true),
        platformPermissionsUsagesProvider: () -> List<PlatformPermissionsUsage> = {
            PlatformPermissionsUsage.provideAll(LocalApplication.current)
        }
    ) {
        showFlow.collect { show ->
            if (!show) {
                return@collect
            }
            nowSpaceContentUseCase.replaceOrAddContent { nowSpaceContents ->
                val oldNowSpaceContentOfPlatformPermissionUsageTips =
                    nowSpaceContents.firstOrNull { it is NowSpaceContent.PlatformPermissionUsageTips }
                val platformPermissionsUsages = platformPermissionsUsagesProvider()
                val newNowSpaceContentOfPlatformPermissionUsageTips =
                    NowSpaceContent.PlatformPermissionUsageTips(
                        tips = xcj.app.appsets.R.string.app_platform_permissions_usage_tips,
                        subTips = xcj.app.appsets.R.string.app_platform_permissions_usage_tips_des,
                        platformPermissionsUsages = platformPermissionsUsages
                    )
                oldNowSpaceContentOfPlatformPermissionUsageTips to newNowSpaceContentOfPlatformPermissionUsageTips
            }
        }
    }

    suspend fun checkUpdate(nowSpaceContentUseCase: NowSpaceContentUseCase) {
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

            nowSpaceContentUseCase.replaceOrAddContent { nowSpaceContents ->
                val newNowSpaceContentOfAppVersionChecked = NowSpaceContent.AppVersionChecked(it)
                null to newNowSpaceContentOfAppVersionChecked
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
            ContextCompat.getString(context, xcj.app.appsets.R.string.you_are_already_friends)
                .toast()
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
            ContextCompat.getString(context, xcj.app.appsets.R.string.you_are_already_friends)
                .toast()
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

                ContextCompat.getString(
                    context,
                    xcj.app.appsets.R.string.friend_request_is_send_please_waiting
                )
                    .toastSuspend()
                requestIdMap[uid] = requestId
            }
        }
    }

    fun requestJoinGroup(context: Context, groupId: String, hello: String, reason: String?) {
        if (RelationsUseCase.getInstance().hasGroupRelated(groupId)) {
            ContextCompat.getString(context, xcj.app.appsets.R.string.you_are_already_in_the_group)
                .toast()
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
                ContextCompat.getString(
                    context,
                    xcj.app.appsets.R.string.group_request_is_send_please_waiting
                )
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

    suspend fun login(
        context: Context,
        account: String,
        password: String,
        visibilityComposeStateProvider: VisibilityComposeStateProvider,
        composeStateUpdater: ComposeStateUpdater<LoginPageUIState>
    ) {
        PurpleLogger.current.d(TAG, "login")
        if (composeStateUpdater !is SingleStateUpdater) {
            return
        }
        val loginPageUIState = composeStateUpdater.getStateValue()
        if (loginPageUIState is LoginPageUIState.Logging) {
            return
        }
        if (account.isEmpty()) {
            ContextCompat.getString(context, xcj.app.appsets.R.string.please_input_account).toast()
            return
        }
        if (password.isEmpty()) {
            ContextCompat.getString(context, xcj.app.appsets.R.string.please_input_password).toast()
            return
        }
        composeStateUpdater.update(LoginPageUIState.Logging())
        requestRaw(action = {
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
                loginResponse.info.toastSuspend()
                composeStateUpdater.update(
                    1000,
                    LoginPageUIState.LoggingFailed(),
                    LoginPageUIState.LoginStart()
                )
                return
            }

            LocalAccountManager.onUserLogged(UserInfo.default(), token, isTemp = true)

            val userInfoResponse = userRepository.getLoggedUserInfo()
            val userInfo = userInfoResponse.data
            if (userInfo == null) {
                PurpleLogger.current.d(
                    TAG, "login, failed, userInfo isNullOrEmpty!"
                )
                composeStateUpdater.update(LoginPageUIState.LoginStart())
                return
            }

            if (userInfo.agreeToTheAgreement == 1) {
                LocalAccountManager.onUserLogged(userInfo, token, false)
                composeStateUpdater.update(LoginPageUIState.LoggingSuccess())
                return
            }
            val bottomSheetState = visibilityComposeStateProvider.bottomSheetState()
            val bottomSheetVisibilityComposeState =
                bottomSheetState as BottomSheetVisibilityComposeState
            bottomSheetVisibilityComposeState.makeCannotDismiss()
            val provider = UserAgreementComposeViewProvider(
                onNextClick = {
                    bottomSheetState.hide()
                    LocalAccountManager.onUserLogged(userInfo, token, false)
                    composeStateUpdater.update(LoginPageUIState.LoggingSuccess())
                }
            )
            bottomSheetState.show(null, provider)
        }).onFailure {
            PurpleLogger.current.d(TAG, "login failed")
            logout()
            composeStateUpdater.update(LoginPageUIState.LoginStart())
        }
    }

    private suspend fun logout() {
        PurpleLogger.current.d(TAG, "logout")
        LocalAccountManager.onUserLogout(LocalAccountManager.LOGOUT_BY_MANUALLY)
        if (LocalAccountManager.isLogged()) {
            request(action = userRepository::signOut)
        }
    }

    suspend fun loginToggle(context: Context, navController: NavController) {
        if (LocalAccountManager.isLogged()) {
            logout()
        } else {
            navController.navigate(PageRouteNames.LoginPage)
        }
    }

    suspend fun signUp(
        context: Context,
        signUpUserInfo: UserInfoForCreate,
        composeStateUpdater: ComposeStateUpdater<SignUpPageUIState>
    ) {
        if (!ModuleConfig.moduleConfiguration.canSignUp) {
            ContextCompat.getString(
                context,
                xcj.app.appsets.R.string.current_version_cannot_be_registered
            ).toast()
            return
        }
        if (composeStateUpdater !is SingleStateUpdater) {
            return
        }
        val signUpPageUIState = composeStateUpdater.getStateValue()
        if (signUpPageUIState !is SignUpPageUIState.SignUpStart) {
            return
        }
        if (signUpUserInfo.account.value.isEmpty() || signUpUserInfo.account.value.isBlank()) {
            ContextCompat.getString(context, xcj.app.appsets.R.string.please_input_account).toast()
            return
        }
        if (signUpUserInfo.password.value.isEmpty() || signUpUserInfo.password.value.isBlank()) {
            ContextCompat.getString(context, xcj.app.appsets.R.string.please_input_password).toast()
            return
        }
        val avatarImageUri = signUpUserInfo.userAvatarUriProvider.value?.provideUri()
        if (avatarImageUri == null) {
            ContextCompat.getString(context, xcj.app.appsets.R.string.please_choose_avatar).toast()
            return
        }
        if (signUpUserInfo.userName.value.isEmpty() || signUpUserInfo.userName.value.isBlank()) {
            ContextCompat.getString(context, xcj.app.appsets.R.string.please_input_name).toast()
            return
        }
        PurpleLogger.current.d(TAG, "signUp")
        composeStateUpdater.update(SignUpPageUIState.SignUpping())
        requestRaw(action = {
            val accountEncode =
                MessageDigestUtil.transformWithMD5(signUpUserInfo.account.value)?.outContent
            val passwordEncode =
                MessageDigestUtil.transformWithMD5(signUpUserInfo.password.value)?.outContent
            if (accountEncode.isNullOrEmpty() || passwordEncode.isNullOrEmpty()) {
                composeStateUpdater.update(SignUpPageUIState.SignUpStart())
                return
            }
            val preSignUpRes = userRepository.preSignUp(accountEncode)
            val canSignUp = preSignUpRes.data
            if (canSignUp == null) {
                ContextCompat.getString(context, xcj.app.appsets.R.string.register_failed)
                    .toastSuspend()
                composeStateUpdater.update(
                    1000,
                    SignUpPageUIState.SignUpPageFailed(xcj.app.appsets.R.string.register_failed),
                    SignUpPageUIState.SignUpStart()
                )
                return
            } else if (!canSignUp) {
                ContextCompat.getString(
                    context,
                    xcj.app.appsets.R.string.a_account_exist_please_retry
                )
                    .toastSuspend()
                composeStateUpdater.update(
                    1000,
                    SignUpPageUIState.SignUpPageFailed(xcj.app.appsets.R.string.a_account_exist_please_retry),
                    SignUpPageUIState.SignUpStart()
                )

                return
            }
            val signUpRes = userRepository.signUp(
                context, accountEncode, passwordEncode, signUpUserInfo
            )
            val signUpSuccess = signUpRes.data
            if (signUpSuccess != true) {
                ContextCompat.getString(context, xcj.app.appsets.R.string.register_failed)
                    .toastSuspend()
                composeStateUpdater.update(
                    1000,
                    SignUpPageUIState.SignUpPageFailed(xcj.app.appsets.R.string.register_failed),
                    SignUpPageUIState.SignUpStart()
                )
                return
            }
            ContextCompat.getString(context, xcj.app.appsets.R.string.register_appsets_success)
                .toastSuspend()
            composeStateUpdater.update(SignUpPageUIState.SignUpFinish())
        }).onFailure {
            ContextCompat.getString(context, xcj.app.appsets.R.string.register_failed)
                .toastSuspend()
            composeStateUpdater.update(
                1000,
                SignUpPageUIState.SignUpPageFailed(
                    xcj.app.appsets.R.string.register_failed
                ), SignUpPageUIState.SignUpStart()
            )
        }
    }

    suspend fun createGroup(
        context: Context,
        groupInfoForCreate: GroupInfoForCreate,
        composeStateUpdater: ComposeStateUpdater<CreateGroupPageUIState>
    ) {
        if (composeStateUpdater !is SingleStateUpdater) {
            return
        }
        val createGroupPageUIState = composeStateUpdater.getStateValue()
        if (createGroupPageUIState !is CreateGroupPageUIState.CreateStart) {
            return
        }
        if (groupInfoForCreate.name.value.isEmpty()) {
            ContextCompat.getString(context, xcj.app.appsets.R.string.group_name_can_not_be_empty)
                .toast()
            return
        }
        PurpleLogger.current.d(TAG, "createGroup")
        composeStateUpdater.update(CreateGroupPageUIState.Creating())
        LocalPurpleCoroutineScope.current.launch {
            requestRaw(action = {
                val preCheckRes =
                    userRepository.createChatGroupPreCheck(groupInfoForCreate.name.value)
                if (preCheckRes.data != true) {
                    ContextCompat.getString(
                        context,
                        xcj.app.appsets.R.string.a_group_name_existed_please_retry
                    )
                        .toastSuspend()
                    composeStateUpdater.update(
                        CreateGroupPageUIState.CreateStart()
                    )
                    return@requestRaw
                }

                val createChatGroupRes = userRepository.createChatGroup(
                    context, groupInfoForCreate
                )
                if (createChatGroupRes.data == true) {
                    ContextCompat.getString(context, xcj.app.appsets.R.string.create_success)
                        .toastSuspend()
                    startServiceToSyncGroupsFromServer(context)
                    composeStateUpdater.update(
                        300,
                        CreateGroupPageUIState.CreateSuccess(),
                        CreateGroupPageUIState.CreateStart()
                    )
                } else {
                    ContextCompat.getString(context, xcj.app.appsets.R.string.create_failed)
                        .toastSuspend()
                    composeStateUpdater.update(
                        300,
                        CreateGroupPageUIState.CreateFailed(),
                        CreateGroupPageUIState.CreateStart()
                    )
                }
            }).onFailure {
                ContextCompat.getString(context, xcj.app.appsets.R.string.create_failed)
                    .toastSuspend()
                composeStateUpdater.update(
                    300,
                    CreateGroupPageUIState.CreateFailed(),
                    CreateGroupPageUIState.CreateStart()
                )

            }
        }
    }

    override fun onComposeDispose(by: String?) {

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
            val intent = Intent(context, DataSyncService::class.java)
            intent.putExtra(
                DataSyncService.KEY_WHAT_TO_DO,
                DataSyncService.DO_TO_SYNC_USER_FRIENDS_FROM_SERVER
            )
            ContextCompat.startForegroundService(context, intent)
        }

        fun startServiceToSyncGroupsFromServer(context: Context) {
            val intent = Intent(context, DataSyncService::class.java)
            intent.putExtra(
                DataSyncService.KEY_WHAT_TO_DO,
                DataSyncService.DO_TO_SYNC_USER_GROUPS_FROM_SERVER
            )
            ContextCompat.startForegroundService(context, intent)
        }

        fun startServiceToSyncAllFromServer(context: Context) {
            val intent = Intent(context, DataSyncService::class.java)
            intent.putExtra(
                DataSyncService.KEY_WHAT_TO_DO,
                DataSyncService.DO_TO_SYNC_USER_DATA_FROM_SERVER
            )
            ContextCompat.startForegroundService(context, intent)
        }

        fun startServiceToSyncFriendsFromLocal(context: Context) {
            val intent = Intent(context, DataSyncService::class.java)
            intent.putExtra(
                DataSyncService.KEY_WHAT_TO_DO,
                DataSyncService.DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL
            )
            ContextCompat.startForegroundService(context, intent)
        }

        fun startServiceToSyncGroupsFromLocal(context: Context) {
            val intent = Intent(context, DataSyncService::class.java)
            intent.putExtra(
                DataSyncService.KEY_WHAT_TO_DO,
                DataSyncService.DO_TO_SYNC_USER_GROUPS_FROM_LOCAL
            )
            ContextCompat.startForegroundService(context, intent)
        }

        fun startServiceToSyncAllFromLocal(context: Context) {
            val intent = Intent(context, DataSyncService::class.java)
            intent.putExtra(
                DataSyncService.KEY_WHAT_TO_DO,
                DataSyncService.DO_TO_SYNC_USER_DATA_FROM_LOCAL
            )
            ContextCompat.startForegroundService(context, intent)
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
            return ContextCompat.getString(context, xcj.app.appsets.R.string.user_agreement)
        }

        fun startIMServiceIfNeeded(context: Context, isAppInBackground: Boolean) {
            if (!LocalAccountManager.isLogged()) {
                return
            }
            if (AppSetsModuleSettings.get().isBackgroundIMEnable) {
                val intent = Intent(context, IMService::class.java)
                intent.putExtra(
                    IMService.KEY_IS_APP_IN_BACKGROUND,
                    isAppInBackground
                )
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }
}