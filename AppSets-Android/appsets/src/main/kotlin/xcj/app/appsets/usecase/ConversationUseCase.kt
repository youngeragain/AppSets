package xcj.app.appsets.usecase

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.RemoteInput
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.db.room.repository.FlatImMessageRepository
import xcj.app.appsets.db.room.repository.GroupInfoRepository
import xcj.app.appsets.db.room.repository.UserInfoRepository
import xcj.app.appsets.im.Bio
import xcj.app.appsets.im.BrokerTest
import xcj.app.appsets.im.ConversationState
import xcj.app.appsets.im.ImMessageGenerator
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.im.InputSelector
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.im.message.ImageMessage
import xcj.app.appsets.im.message.SystemMessage
import xcj.app.appsets.im.message.parseFromImObj
import xcj.app.appsets.im.message.parseToImObj
import xcj.app.appsets.im.model.FriendRequestFeedbackJson
import xcj.app.appsets.im.model.GroupJoinRequestFeedbackJson
import xcj.app.appsets.notification.NotificationPusher
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.server.model.UserRole
import xcj.app.appsets.ui.compose.PageRouteNames
import xcj.app.appsets.ui.compose.conversation.GenerativeAISession
import xcj.app.appsets.ui.model.NowSpaceObjectState
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.starter.android.util.LocalMessager
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalPurpleCoroutineScope

sealed interface SessionState {
    object None : SessionState
    data class Normal(val session: Session) : SessionState
}

class ConversationUseCase private constructor() : IComposeLifecycleAware {

    companion object {
        private const val TAG = "ConversationUseCase"
        const val KEY_SESSIONS_INIT_RESULT = "sessions_init_result"
        const val USER = "user"
        const val GROUP = "group"
        const val SYSTEM = "system"
        const val AI = "ai"

        private var INSTANCE: ConversationUseCase? = null

        fun getInstance(): ConversationUseCase {
            return INSTANCE ?: run {
                val useCase = ConversationUseCase()
                INSTANCE = useCase
                useCase
            }
        }
    }

    private var sessionsInitTimes = 0

    private val coroutineScope: CoroutineScope = LocalPurpleCoroutineScope.current
    private val nowSpaceContentUseCase: NowSpaceContentUseCase =
        NowSpaceContentUseCase.getInstance()

    private var mNotificationPusher: NotificationPusher? = null
    private val sessionsMap: MutableMap<String, MutableList<Session>> = mutableMapOf()

    val currentTab: MutableState<String> = mutableStateOf(AI)
    val isShowActions: MutableState<Boolean> = mutableStateOf(false)

    private var lastSessionState: SessionState = SessionState.None
    val currentSessionState: MutableState<SessionState> = mutableStateOf(SessionState.None)

    val complexContentSendingState: MutableState<Boolean> = mutableStateOf(false)
    private var navigationUseCase: NavigationUseCase? = null


    init {
        sessionsMap[SYSTEM] = mutableStateListOf()
        sessionsMap[USER] = mutableStateListOf()
        sessionsMap[GROUP] = mutableStateListOf()
        sessionsMap[AI] = mutableStateListOf()
    }

    fun setNavigationUseCase(navigationUseCase: NavigationUseCase) {
        this.navigationUseCase = navigationUseCase
    }

    /**
     * @param imObj 用户或群组imObj,没有找到对应的session，则创建一个session
     */
    private fun findSessionByImObj(imObj: ImObj? = null): Session? {
        PurpleLogger.current.d(
            TAG,
            "findSessionByImObj, imObj:${imObj}"
        )
        if (imObj == null) {
            val currentSessionState = currentSessionState.value
            if (currentSessionState is SessionState.None) {
                PurpleLogger.current.d(
                    TAG,
                    "findSessionByImObj, imObj is null and session is null, return"
                )
            }
            return (currentSessionState as? SessionState.Normal)?.session
        }

        when (imObj) {
            is ImObj.ImSingle -> {
                val userRoles = imObj.userRoles
                if (userRoles != null) {
                    if (userRoles.contains(UserRole.ROLE_ADMIN)) {
                        return getOrCreateSession(sessionsMap, SYSTEM, imObj)
                    }
                }

                return getOrCreateSession(sessionsMap, USER, imObj)
            }

            is ImObj.ImGroup -> {
                return getOrCreateSession(sessionsMap, GROUP, imObj)
            }
        }
    }

    private fun getOrCreateSession(
        container: Map<String, MutableList<Session>>,
        type: String,
        imObj: ImObj,
    ): Session? {
        if (!container.containsKey(type)) {
            return null
        }
        val sessions = container[type]
        if (sessions == null) {
            return null
        }
        for (session in sessions) {
            if (session.id == imObj.id) {
                return session
            }
        }
        val session = Session(imObj, ConversationState())
        sessions.add(0, session)
        return session
    }

    fun updateCurrentSessionBySessionId(sessionId: String) {
        PurpleLogger.current.d(
            TAG,
            "updateCurrentSessionBySessionId, sessionId:${sessionId}"
        )
        val session: Session? = getSessionBySessionId(sessionId)
        if (session == null) {
            PurpleLogger.current.d(
                TAG,
                "updateCurrentSessionBySessionId, session is null for sessionId:$sessionId, return!"
            )
            return
        }
        updateCurrentSessionBySession(session)
    }

    fun updateCurrentSessionBySession(session: Session) {
        PurpleLogger.current.d(
            TAG,
            "updateCurrentSessionBySession, session:${session}"
        )
        updateCurrentSession(session)
    }

    fun updateCurrentSessionByUserInfo(userInfo: UserInfo) {
        PurpleLogger.current.d(TAG, "updateCurrentSessionByUserInfo, userinfo:${userInfo}")
        updateCurrentSessionByBio(userInfo)
    }

    fun updateCurrentSessionByGroupInfo(groupInfo: GroupInfo) {
        PurpleLogger.current.d(TAG, "updateCurrentSessionByGroupInfo, groupInfo:${groupInfo}")
        updateCurrentSessionByBio(groupInfo)
    }

    fun updateCurrentSessionByBio(bio: Bio) {
        PurpleLogger.current.d(
            TAG,
            "updateCurrentSessionByBio, bio:${bio}"
        )
        val session = findSessionByImObj(ImObj.fromBio(bio))
        if (session == null) {
            PurpleLogger.current.d(
                TAG,
                "updateCurrentSessionByBio, session is not found, return"
            )
            return
        }
        updateCurrentSession(session)
        val ifNeeded =
            RelationsUseCase.getInstance().updateRelatedGroupIfNeeded(bio)
        if (ifNeeded) {
            coroutineScope.launch {
                BrokerTest.updateImGroupBindIfNeeded()
            }
        }
    }

    fun updateCurrentSession(session: Session?) {
        val oldState = currentSessionState.value
        PurpleLogger.current.d(TAG, "updateCurrentSession, old:$oldState, new:$session")
        if (session == null) {
            lastSessionState = currentSessionState.value
            currentSessionState.value = SessionState.None
        } else {
            nowSpaceContentUseCase.removeContentIf { topSpaceObjectState ->
                topSpaceObjectState is NowSpaceObjectState.NewImMessage && topSpaceObjectState.session.id == session.id
            }
            currentSessionState.value = SessionState.Normal(session)
        }
    }

    fun getSessionBySessionId(sessionId: String): Session? {
        return sessionsMap.values.flatten().firstOrNull { it.id == sessionId }
    }

    /**
     * @param isLocal msg是从本机发送
     */
    suspend fun onMessage(context: Context, imMessage: ImMessage, isLocal: Boolean) {
        val currentSessionState = currentSessionState.value
        PurpleLogger.current.d(
            TAG,
            "onMessage, currentSessionState:${currentSessionState}"
        )
        if (isLocal) {
            val session = (currentSessionState as? SessionState.Normal)?.session ?: return
            val sessionImObj = session.imObj
            if (sessionImObj.bio is GenerativeAISession.AIBio) {
                //todo
                GenerativeAISession.handleSessionNewMessage(session, imMessage)
            } else {
                BrokerTest.sendMessage(sessionImObj, imMessage)
                addMessageToSession(context, session, imMessage)
            }
        } else {
            val session: Session = findSessionByImMessage(context, imMessage) ?: return
            addMessageToSession(context, session, imMessage)
            saveMessageToLocalDB(imMessage)
            showNotificationForImMessages(context, session, imMessage)
        }
    }

    private fun findSessionByImMessage(context: Context, imMessage: ImMessage): Session? {
        PurpleLogger.current.d(
            TAG,
            "findSessionByImMessage, imMessage:${imMessage}"
        )
        val fromImObj = imMessage.parseFromImObj()
        if (fromImObj == null) {
            PurpleLogger.current.d(
                TAG,
                "findSessionByImMessage, parseFromImObj return null, return"
            )
            return null
        }
        when (fromImObj) {
            is ImObj.ImSingle -> {
                if (fromImObj.id == LocalAccountManager.userInfo.uid) {
                    //自己的其他设备发送的单聊消息,需要解析toImObj信息对应到相应的Session
                    val toImObj = imMessage.parseToImObj()
                    if (toImObj == null) {
                        return null
                    }
                    val session = findSessionByImObj(toImObj)
                    if (session == null) {
                        PurpleLogger.current.d(
                            TAG,
                            "findSessionByImMessage, session is not found 1, return"
                        )
                        return null
                    }

                    return session
                } else {
                    //来自他人的单聊消息
                    val session = findSessionByImObj(fromImObj)//获取消息From信息对应的Session
                    if (session == null) {
                        PurpleLogger.current.d(
                            TAG,
                            "findSessionByImMessage, session is not found 2, return"
                        )
                        return null
                    }
                    if (imMessage is SystemMessage) {
                        toHandleSystemMessageIfNeeded(context, session, imMessage)
                    }

                    return session
                }
            }

            is ImObj.ImGroup -> {
                //群组消息直接添加
                val session = findSessionByImObj(fromImObj)//获取消息From信息对应的Session
                if (session == null) {
                    PurpleLogger.current.d(
                        TAG,
                        "findSessionByImMessage, session is not found 3, return"
                    )
                    return null
                }

                return session
            }
        }
        return null
    }

    private fun addMessageToSession(
        context: Context,
        session: Session,
        imMessage: ImMessage,
    ) {
        session.conversationState.addMessage(imMessage)
    }

    private fun toHandleSystemMessageIfNeeded(
        context: Context,
        session: Session,
        imMessage: SystemMessage,
    ) {
        val systemContentInterface = imMessage.systemContentInterface
        when (systemContentInterface) {
            is FriendRequestFeedbackJson -> {
                PurpleLogger.current.d(
                    TAG,
                    "toHandleSystemMessageIfNeeded, FriendRequestFeedbackJson:${systemContentInterface.isAccept}"
                )
                if (systemContentInterface.isAccept) {
                    SystemUseCase.startServiceToSyncFriendsFromServer(context)
                }
            }

            is GroupJoinRequestFeedbackJson -> {
                PurpleLogger.current.d(
                    TAG,
                    "toHandleSystemMessageIfNeeded, GroupJoinRequestFeedbackJson:${systemContentInterface.isAccept}"
                )
                if (systemContentInterface.isAccept) {
                    SystemUseCase.startServiceToSyncGroupsFromServer(context)
                }
            }

            else -> {

            }
        }
    }

    private suspend fun showNotificationForImMessages(
        context: Context,
        session: Session,
        imMessage: ImMessage,
    ) {
        PurpleLogger.current.d(TAG, "showNotificationForImMessages")
        if (imMessage.fromInfo.uid == LocalAccountManager.userInfo.uid) {
            PurpleLogger.current.d(
                TAG,
                "showNotificationForImMessages, message from myself, return!"
            )
            return
        }
        addMessageToNowSpaceIfNeeded(context, session, imMessage)
        val notificationPusher = getNotificationPusher()
        pushNotificationIfNeeded(context, notificationPusher, session, imMessage)
    }

    private fun addMessageToNowSpaceIfNeeded(
        context: Context,
        session: Session,
        imMessage: ImMessage,
    ) {
        val sessionState = currentSessionState.value
        if (sessionState !is SessionState.Normal) {
            return
        }
        if (sessionState.session.id == session.id) {
            val navigationUseCase = navigationUseCase
            if (navigationUseCase == null) {
                return
            }
            if (navigationUseCase.currentRoute != PageRouteNames.ConversationDetailsPage) {
                nowSpaceContentUseCase.onNewImMessage(session, imMessage)
            }
        } else {
            nowSpaceContentUseCase.onNewImMessage(session, imMessage)
        }
    }

    private fun getNotificationPusher(): NotificationPusher {
        if (mNotificationPusher == null) {
            mNotificationPusher = NotificationPusher()
        }
        return mNotificationPusher!!
    }

    private suspend fun saveMessageToLocalDB(imMessage: ImMessage) {
        if (imMessage.fromInfo.roles?.contains(UserRole.ROLE_ADMIN) == true) {
            return
        }
        runCatching {
            FlatImMessageRepository.getInstance()
                .saveImMessage(imMessage)
        }.onFailure {
            PurpleLogger.current.e(TAG, "saveMessageToLocalDB, failed, ${it.message}")
        }
    }

    fun onSendMessage(context: Context, inputSelector: Int, content: Any) {
        if (!LocalAccountManager.isLogged()) {
            PurpleLogger.current.d(TAG, "onSendMessage, user not logged! return")
            return
        }
        val currentSessionState = currentSessionState.value
        if (currentSessionState is SessionState.None) {
            PurpleLogger.current.d(TAG, "onSendMessage, current session object is null! return")
            return
        }
        val session = (currentSessionState as? SessionState.Normal)?.session ?: return
        coroutineScope.launch {
            if (InputSelector.isComplex(inputSelector)) {
                complexContentSendingState.value = true
            }

            val imMessage =
                runCatching {
                    ImMessageGenerator.generateBySend(
                        context,
                        session,
                        inputSelector,
                        content
                    )
                }.onFailure {
                    PurpleLogger.current.d(TAG, "onSendMessage, ImMessageGenerator generate error!")
                    it.printStackTrace()
                }.getOrNull()

            if (InputSelector.isComplex(inputSelector)) {
                delay(1000)
                complexContentSendingState.value = false
            }
            if (imMessage == null) {
                return@launch
            }

            onMessage(context, imMessage, true)
        }
    }

    fun currentTabSessions(): MutableList<Session> {
        return sessionsMap[currentTab.value]!!
    }

    fun updateCurrentTab(tab: String) {
        currentTab.value = tab
    }

    fun onUserLogout() {
        coroutineScope.launch {
            currentSessionState.value = SessionState.None
            sessionsMap.values.forEach(MutableCollection<*>::clear)
            currentTab.value = USER
        }
    }

    fun initSessionsIfNeeded(force: Boolean = true) {
        PurpleLogger.current.d(TAG, "initSessionsIfNeeded")
        if (force || sessionsInitTimes == 0) {
            initSessions()
        }
    }

    /**
     * 登录成功以及本地状态为已经登录即触发初始化会话列表
     */
    private fun initSessions() {
        PurpleLogger.current.d(TAG, "initSessions")
        coroutineScope.launch {
            sessionsMap.values.forEach(MutableCollection<*>::clear)
            runCatching {
                //User sessions
                val userSessions = sessionsMap[USER]!!
                val userInfoRepository = UserInfoRepository.getInstance()
                userInfoRepository.getRelatedUserList().let { users ->
                    PurpleLogger.current.d(
                        TAG,
                        "initSessions, RelatedUsers:${users}"
                    )
                    fillImMessageToSessions(userSessions, users)
                }
                userInfoRepository.getUnRelatedUserList().let { users ->
                    PurpleLogger.current.d(
                        TAG,
                        "initSessions, UnRelatedUsers:${users}"
                    )
                    fillImMessageToSessions(userSessions, users)
                }

                //Group sessions
                val groupSessions = sessionsMap[GROUP]!!
                val groupsRoomRepository = GroupInfoRepository.getInstance()
                groupsRoomRepository.getRelatedGroupList().let { groups ->
                    PurpleLogger.current.d(
                        TAG,
                        "initSessions, relatedGroups:${groups}"
                    )
                    fillImMessageToSessions(groupSessions, groups)
                }

                val unRelatedGroupList =
                    groupsRoomRepository.getUnRelatedGroupList()
                val predicate: (GroupInfo) -> Boolean =
                    { it.id.startsWith(Application.BIO_ID_PREFIX) }

                unRelatedGroupList.filterNot(predicate).let { filteredGroups ->
                    PurpleLogger.current.d(
                        TAG,
                        "initSessions, UnRelatedGroups[Group]:${filteredGroups}"
                    )
                    fillImMessageToSessions(groupSessions, filteredGroups)
                }

                //Application sessions
                unRelatedGroupList.filter(predicate).let { filteredGroups ->
                    PurpleLogger.current.d(
                        TAG,
                        "initSessions, UnRelatedGroups[Application]:${filteredGroups}"
                    )
                    val applications = filteredGroups.map { group ->
                        Application.basic(
                            group.id.substringAfter(Application.BIO_ID_PREFIX),
                            group.name,
                            group.iconUrl
                        )
                    }
                    fillImMessageToSessions(groupSessions, applications)
                }
            }.onSuccess {
                PurpleLogger.current.d(TAG, "initSessions list successful!")
                sessionsInitTimes += 1
                LocalMessager.post(KEY_SESSIONS_INIT_RESULT, true)
            }.onFailure {
                LocalMessager.post(KEY_SESSIONS_INIT_RESULT, false)
                PurpleLogger.current.d(
                    TAG,
                    "initSessions list failed, ${it.message}"
                )
            }
        }
    }

    private suspend fun fillImMessageToSessions(
        sessionList: MutableList<Session>,
        bioList: List<Bio>,
    ) {
        if (bioList.isEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "fillImMessageToSessions, bioList isEmpty, return"
            )
            return
        }
        bioList.mapTo(sessionList) { bio ->
            val imObj = ImObj.fromBio(bio)
            val session = Session(imObj, ConversationState())
            fillPagedImMessageToSession(bio, session, page = 1, pageSize = 100)
            session
        }
    }

    private suspend fun fillPagedImMessageToSession(
        bio: Bio,
        session: Session,
        page: Int,
        pageSize: Int,
    ) {
        val imMessageList = when (bio) {
            is UserInfo -> {
                FlatImMessageRepository.getInstance()
                    .getImMessageListByUser(bio, page, pageSize)
            }

            is GroupInfo -> {
                FlatImMessageRepository.getInstance()
                    .getImMessageListByGroup(bio, page, pageSize)
            }

            is Application -> {
                FlatImMessageRepository.getInstance()
                    .getImMessageListByApplication(bio, page, pageSize)
            }

            else -> null
        }
        PurpleLogger.current.d(
            TAG,
            "fillPageImMessageToSessions, " +
                    "bio:$bio, " +
                    "page:$page, " +
                    "pageSize:$pageSize, " +
                    "imMessageList size:${imMessageList?.size}"
        )
        if (imMessageList.isNullOrEmpty()) {
            return
        }
        session.conversationState.addMessages(imMessageList)
    }

    fun getAllSimpleSessionsCount(): Int {
        return (sessionsMap[AI]?.size ?: 0) + (sessionsMap[GROUP]?.size
            ?: 0) + (sessionsMap[GROUP]?.size ?: 0)
    }

    fun getAllSimpleSessionsByKeywords(keywords: String? = null): List<Session> {
        val overrideKeywords = if (getAllSimpleSessionsCount() > 2) {
            keywords
        } else {
            null
        }
        PurpleLogger.current.d(TAG, "getAllSimpleSessionsByKeywords, keywords:$keywords")
        val sessions = mutableListOf<Session>()
        sessionsMap.forEach {
            if (it.key != SYSTEM) {
                sessions.addAll(it.value)
            }
        }
        sessions.removeIf {
            if (it == currentSessionState.value) {
                true
            } else if (!overrideKeywords.isNullOrEmpty()) {
                !it.imObj.name.contains(overrideKeywords)
            } else {
                false
            }
        }
        return sessions
    }

    fun toggleShowAddActions() {
        isShowActions.value = !isShowActions.value
    }

    @SuppressLint("MissingPermission")
    fun handleSystemNotificationForReplyImMessage(context: Context, intent: Intent) {
        PurpleLogger.current.d(TAG, "handleSystemNotificationForReplyImMessage")
        val imMessageId = intent.getStringExtra(ImMessage.KEY_IM_MESSAGE_ID)
        val sessionId = intent.getStringExtra(ImMessage.KEY_SESSION_ID)
        val imMessageNotificationId =
            intent.getIntExtra(ImMessage.KEY_IM_MESSAGE_NOTIFICATION_ID, -1)
        if (imMessageId.isNullOrEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "handleSystemNotificationForReplyImMessage, imMessageId is null or empty, return!"
            )
            return
        }
        if (sessionId.isNullOrEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "handleSystemNotificationForReplyImMessage, sessionId is null or empty, return!"
            )
            return
        }
        if (imMessageNotificationId == -1) {
            PurpleLogger.current.d(
                TAG,
                "handleSystemNotificationForReplyImMessage, imMessageNotificationId is null or empty, return!"
            )
            return
        }
        PurpleLogger.current.d(
            TAG,
            "handleSystemNotificationForReplyImMessage, (imMessageId:${imMessageId}, sessionId:$sessionId," +
                    " imMessageNotificationId:$imMessageNotificationId)"
        )
        val session: Session? = getSessionBySessionId(sessionId)
        if (session == null) {
            PurpleLogger.current.d(
                TAG,
                "handleSystemNotificationForReplyImMessage, session is null for sessionId:$sessionId, return!"
            )
            return
        }
        updateCurrentSessionBySession(session)
        val resultsBundle = RemoteInput.getResultsFromIntent(intent)
        val userInputContent =
            resultsBundle?.getCharSequence(NotificationPusher.REMOTE_BUILDER_KEY_IM_INPUT)
        PurpleLogger.current.d(
            TAG,
            "handleSystemNotificationForReplyImMessage, resultsBundle:$resultsBundle, userInputContent:$userInputContent"
        )
        if (userInputContent.isNullOrEmpty() || userInputContent.isBlank()) {
            PurpleLogger.current.d(
                TAG,
                "handleSystemNotificationForReplyImMessage, userInputContent is null or empty, or blank, return!"
            )
            return
        }
        getNotificationPusher().cancelNotification(context, imMessageNotificationId)
        onSendMessage(context, InputSelector.TEXT, userInputContent)
    }

    suspend fun pushNotificationIfNeeded(
        context: Context,
        notificationPusher: NotificationPusher,
        session: Session,
        imMessage: ImMessage,
    ) {
        PurpleLogger.current.d(TAG, "pushNotificationIfNeeded")
        notificationPusher.pushConversionNotification(context, session, imMessage)
    }

    fun findCurrentSessionAllImMessageOfImage(): List<ImageMessage> {
        val currentSessionState = currentSessionState.value
        if (currentSessionState is SessionState.Normal) {
            val session = currentSessionState.session
            return session.conversationState.messages.mapNotNull { it as? ImageMessage }
        }
        return emptyList()
    }

    override fun onComposeDispose(by: String?) {
        PurpleLogger.current.d(TAG, "onComposeDispose, by:$by")
    }

    fun addAIGCSessionIfAbsent(session: Session) {
        val aiSessions = sessionsMap[AI]
        if (aiSessions == null) {
            return
        }
        aiSessions.add(0, session)
    }

}