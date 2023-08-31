package xcj.app.appsets.usecase

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.media3.common.util.UnstableApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.db.room.repository.FlatImMessageRoomRepository
import xcj.app.appsets.db.room.repository.UserGroupsRoomRepository
import xcj.app.appsets.db.room.repository.UserInfoRoomRepository
import xcj.app.appsets.im.CommonURLJson
import xcj.app.appsets.im.ConversationUiState
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.im.ImObj
import xcj.app.appsets.im.ImSessionHolder
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.RabbitMqBroker
import xcj.app.appsets.im.Session
import xcj.app.appsets.im.SystemContentInterface
import xcj.app.appsets.im.parseFromImObj
import xcj.app.appsets.im.parseToImObj
import xcj.app.appsets.im.toToInfo
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.ui.compose.conversation.InputSelector
import xcj.app.appsets.ui.nonecompose.base.NotificationPusherInterface
import xcj.app.appsets.util.NotificationPusher
import xcj.app.core.foundation.usecase.NoConfigUseCase
import xcj.app.io.components.SimpleFileIO
import java.io.File
import java.util.Calendar
import java.util.UUID

@UnstableApi
class ConversationUseCase(
    private val coroutineScope: CoroutineScope
) : NoConfigUseCase() {
    private val TAG = "ConversationUseCase"

    private var notificationPusher: NotificationPusher? = null
    val currentTab: MutableState<Int> = mutableStateOf(0)

    private val systemSessions: MutableList<Session> = mutableStateListOf()

    private val userSessions: MutableList<Session> = mutableStateListOf()

    private val groupSessions: MutableList<Session> = mutableStateListOf()

    private var lastSession: Session? = null
    var currentSession: Session? = null
        private set

    /**
     * @param imObj 用户或群组imObj,没有找到对应的session，则创建一个session
     */
    private fun getSessionByImObj(imObj: ImObj? = null): Session {
        if (imObj == null)
            return currentSession ?: throw Exception("无效聊天对象!")
        synchronized(this) {
            if (imObj is ImObj.ImSingle) {
                if (imObj.userRoles?.contains("admin") == true) {
                    for (systemSession in systemSessions) {
                        if (systemSession.imObj.id == imObj.id)
                            return systemSession
                    }
                    val session = Session(imObj, ConversationUiState(emptyList()))
                    systemSessions.add(session)
                    return session
                }
                for (userSession in userSessions) {
                    if (userSession.imObj.id == imObj.id)
                        return userSession
                }
                val session = Session(imObj, ConversationUiState(emptyList()))
                val hasUserRelated = UserRelationsCase.getInstance().hasUserRelated(imObj.uid)
                if (hasUserRelated) {
                    //放到朋友title下第一个
                    userSessions.add(1, session)
                } else {
                    //放到临时title下第一个
                    for (i in userSessions.indices) {
                        val session1 = userSessions.get(i)
                        if (session1.isTitle && session1.imObj.name == "临时") {
                            userSessions.add(i + 1, session)
                            break
                        }
                    }
                }

                return session
            } else if (imObj is ImObj.ImGroup) {
                for (groupSession in groupSessions) {
                    if (groupSession.imObj.id == imObj.id)
                        return groupSession
                }
                val session = Session(imObj, ConversationUiState(emptyList()))
                val hasGroupRelated = UserRelationsCase.getInstance().hasGroupRelated(imObj.groupId)
                if (hasGroupRelated) {
                    //放到已加title下第一个
                    groupSessions.add(session)
                } else {
                    //放到临时title下第一个
                    for (i in groupSessions.indices) {
                        val session1 = groupSessions.get(i)
                        if (session1.isTitle && session1.imObj.name == "临时") {
                            groupSessions.add(i + 1, session)
                            break
                        }
                    }
                }
                groupSessions.add(session)
                return session
            }
            throw Exception("无效聊天对象!")
        }
    }

    private fun checkSessionsTitle() {
        fun common(sessions: MutableList<Session>, title1: String, title2: String) {
            if (sessions.isEmpty()) {
                sessions.add(Session(ImObj.ImTitle(title1), null))
                sessions.add(Session(ImObj.ImTitle(title2), null))
            } else {
                var hasTitle1 = false
                var hasTitle2 = false
                for (userSession in sessions) {
                    if (userSession.isTitle) {
                        if (userSession.imObj.name == title1)
                            hasTitle1 = true
                        if (userSession.imObj.name == title2)
                            hasTitle2 = true
                    }
                    if (hasTitle1 && hasTitle2)
                        break
                }
                if (!hasTitle1)
                    sessions.add(0, Session(ImObj.ImTitle(title1), null))
                if (!hasTitle2)
                    sessions.add(Session(ImObj.ImTitle(title2), null))
            }
        }
        common(userSessions, "朋友", "临时")
        common(groupSessions, "已加", "临时")
    }

    fun updateCurrentSessionBySession(session: Session) {
        Log.e(TAG, "updateCurrentSessionBySession, session:${session}")
        currentSession = session
    }

    fun updateCurrentSessionByUserInfo(userInfo: UserInfo) {
        Log.e(TAG, "changeCurrentSession, userinfo:${userInfo}")
        val session = getSessionByImObj(userInfo.asImSingle())
        currentSession = session
    }

    fun updateCurrentSessionByGroupInfo(groupInfo: GroupInfo) {
        Log.e(TAG, "changeCurrentSession, groupInfo:${groupInfo}")
        val session = getSessionByImObj(groupInfo.asImGroup())
        currentSession = session
    }


    fun removeCurrentSession() {
        lastSession = currentSession
        currentSession = null
    }

    fun getSessionBySessionId(sessionId: String): Session {
        for (userSession in userSessions) {
            if (userSession.id == sessionId)
                return userSession
        }
        for (groupSession in groupSessions) {
            if (groupSession.id == sessionId)
                return groupSession
        }
        throw Exception()
    }

    /**
     * @param isLocal msg是从本机发送
     */
    fun onMessage(context: Context, imMessage: ImMessage, isLocal: Boolean) {
        Log.e(
            TAG,
            "onMessage::currentSession:${currentSession}"
        )
        coroutineScope.launch {
            if (isLocal) {//发送消息
                if (currentSession == null)
                    return@launch
                RabbitMqBroker.sendMessage(currentSession!!.imObj, imMessage)
                return@launch
            }
            //收到消息
            var session: Session? = null
            val fromImObj = imMessage.parseFromImObj() ?: return@launch
            if (fromImObj is ImObj.ImSingle) {
                if (fromImObj.uid == LocalAccountManager._userInfo.value.uid) {
                    //自己的其他设备发送的单聊消息,需要解析toImObj信息对应到相应的Session
                    val toImObj = imMessage.parseToImObj()
                    if (toImObj != null) {
                        session = getSessionByImObj(toImObj)
                        session.conversionState?.addMessage(imMessage)
                    }
                } else {
                    var shouldAddToSession = true
                    //来自他人的单聊消息
                    session = getSessionByImObj(fromImObj)//获取消息From信息对应的Session
                    if (imMessage is ImMessage.System) {
                        convertAdminImMessageIfNeeded(context, imMessage)
                        if (imMessage.systemContentJson.contentObject is SystemContentInterface.RequestFeedbackJson) {
                            shouldAddToSession = false
                        }
                    }
                    if (shouldAddToSession)
                        session.conversionState?.addMessage(imMessage)
                }
            } else if (fromImObj is ImObj.ImGroup) {
                //群组消息直接添加
                session = getSessionByImObj(fromImObj)//获取消息From信息对应的Session
                session.conversionState?.addMessage(imMessage)
            }
            saveMessageToLocalDB(imMessage)
            if (imMessage.msgFromInfo.uid == LocalAccountManager._userInfo.value.uid)
                return@launch
            if (context is NotificationPusherInterface) {
                if (notificationPusher == null) {
                    notificationPusher = NotificationPusher()
                }
                context.pushNotificationIfNeeded(notificationPusher!!, session?.id!!, imMessage)
            }
        }
    }

    private fun convertAdminImMessageIfNeeded(context: Context, imMessage: ImMessage.System) {
        when (val contentObject = imMessage.systemContentJson.contentObject) {
            is SystemContentInterface.FriendRequestFeedbackJson -> {
                Log.e(TAG, "convertAdminImMessageIfNeeded:${contentObject.isAccept}")
                if (contentObject.isAccept) {
                    SystemUseCase.startServiceToSyncFriendsFromServer(context)
                }
            }

            is SystemContentInterface.GroupJoinRequestFeedbackJson -> {
                Log.e(TAG, "convertAdminImMessageIfNeeded:${contentObject.isAccept}")
                if (contentObject.isAccept) {
                    SystemUseCase.startServiceToSyncGroupsFromServer(context)
                }
            }

            else -> {

            }
        }
    }


    private suspend fun saveMessageToLocalDB(imMessage: ImMessage) {
        if (imMessage.msgFromInfo.roles?.contains("admin") == true)
            return
        FlatImMessageRoomRepository.getInstance().addFlatImMessage(imMessage)
    }

    val thereHasSomeNewestImMessages: MutableState<Boolean> = mutableStateOf(false)
    val someNewestImMessages: MutableMap<String, MutableList<Pair<ImObj, ImMessage>>> =
        mutableStateMapOf()

    //当前对话处于特定单聊场景，并且新消息的发送者不是该特定单聊场景的对话用户时，在界面上显示一个提示
    private fun notifyThereHasNewestImMessages(imMessage: ImMessage) {
        /*val keyGen = imMessage.msgFromInfo.name ?: imMessage.msgFromInfo.uid
        var successAdd = false
        if (someNewestImMessages.containsKey(keyGen)) {
            val messagesExist = someNewestImMessages[keyGen]
            val imObj = imMessage.parseImObj()
            if(imObj!=null){
                successAdd = true
                messagesExist!!.add(imObj to imMessage)
            }
        } else {
            val imObj = imMessage.parseImObj()
            if (imObj != null) {
                successAdd = true
                someNewestImMessages[keyGen] = mutableListOf(imObj to imMessage)
            }
        }
        if (successAdd)
            thereHasSomeNewestImMessages.value = true*/
    }

    /**
     * 发送的内容为本地文件时，需要先上传到文件服务器获取其Url
     * @return first为文件或Uri的path, second为文件的Url
     *
     */
    private fun getContentPair(context: Context, content: Any): Pair<String, String> {
        val contentUrlMarker: String = UUID.randomUUID().toString()
        return when (content) {
            is File -> {
                SimpleFileIO.getInstance().uploadWithFile(context, content, contentUrlMarker, null)
                content.path to contentUrlMarker
            }

            is Uri -> {
                content.path ?: throw Exception("uri is null")
                SimpleFileIO.getInstance().uploadWithUri(context, content, contentUrlMarker, null)
                content.path!! to contentUrlMarker
            }

            else -> throw Exception("not support!")
        }
    }

    fun onSendMessage(context: Context, inputSelector: InputSelector, content: Any) {
        val currentImObj = currentSession?.imObj ?: return
        val userInfo = LocalAccountManager.provideState<UserInfo>().value
        val file = when (content) {
            is Uri -> {
                File(content.path!!)
            }

            is File -> {
                content
            }

            else -> {
                File(content as String)
            }
        }
        val imMessage = when (inputSelector) {
            InputSelector.IMAGE -> {
                val (content1, contentUrl1) = getContentPair(context, content)
                ImMessage.Image(
                    UUID.randomUUID().toString(),
                    contentUrl1,
                    content1,
                    MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                    Calendar.getInstance().time,
                    currentImObj.toToInfo(),
                    null
                )
            }

            InputSelector.VIDEO -> {
                val (content1, contentUrl1) = getContentPair(context, content)
                ImMessage.Video(
                    UUID.randomUUID().toString(),
                    CommonURLJson.VideoURLJson(contentUrl1, file.name),
                    content1,
                    MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                    Calendar.getInstance().time,
                    currentImObj.toToInfo(),
                    null
                )
            }

            InputSelector.MUSIC -> {
                val (content1, contentUrl1) = getContentPair(context, content)
                ImMessage.Music(
                    UUID.randomUUID().toString(),
                    CommonURLJson.MusicURLJson(contentUrl1, file.name),
                    content1,
                    MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                    Calendar.getInstance().time,
                    currentImObj.toToInfo(),
                    null
                )
            }

            InputSelector.FILE -> {
                val (content1, contentUrl1) = getContentPair(context, content)
                ImMessage.File(
                    UUID.randomUUID().toString(),
                    CommonURLJson.FileURLJson(contentUrl1, file.name),
                    content1,
                    MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                    Calendar.getInstance().time,
                    currentImObj.toToInfo(),
                    "application/file",
                    null
                )
            }

            InputSelector.VOICE -> {
                val (content1, contentUrl1) = getContentPair(context, content)
                ImMessage.Voice(
                    UUID.randomUUID().toString(),
                    CommonURLJson.VoiceURLJson(contentUrl1, file.name),
                    content1,
                    MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                    Calendar.getInstance().time,
                    currentImObj.toToInfo(),
                    null
                )
            }

            InputSelector.LOCATION -> {
                ImMessage.Location(
                    UUID.randomUUID().toString(),
                    content as String,
                    MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                    Calendar.getInstance().time,
                    currentImObj.toToInfo(),
                    null
                )
            }

            InputSelector.HTML -> {
                ImMessage.HTML(
                    UUID.randomUUID().toString(),
                    content as String,
                    MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                    Calendar.getInstance().time,
                    currentImObj.toToInfo(),
                    null
                )
            }

            InputSelector.AD -> {
                ImMessage.Ad(
                    UUID.randomUUID().toString(),
                    content as String,
                    MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                    Calendar.getInstance().time,
                    currentImObj.toToInfo(),
                    null
                )
            }

            else -> ImMessage.Text(
                UUID.randomUUID().toString(),
                content as String,
                MessageFromInfo(userInfo.uid, userInfo.name, userInfo.avatarUrl),
                Calendar.getInstance().time,
                currentImObj.toToInfo(),
                null
            )
        }
        onMessage(context, imMessage, true)
    }

    fun currentSessions(): MutableList<Session> {
        return when (currentTab.value) {
            0 -> userSessions
            1 -> groupSessions
            else -> systemSessions
        }
    }

    fun onChipClick(chip: Int) {
        when (chip) {
            0 -> {
                if (currentTab.value != 0) {
                    currentTab.value = 0
                }
            }

            1 -> {
                if (currentTab.value != 1) {
                    currentTab.value = 1
                }
            }

            else -> {
                if (currentTab.value != 2) {
                    currentTab.value = 2
                }
            }
        }

    }

    fun clean() {
        coroutineScope.launch {
            lastSession = null
            currentSession = null
            userSessions.clear()
            groupSessions.clear()
            systemSessions.clear()
            currentTab.value = 0
        }
    }

    /**
     * 登录成功以及本地状态为已经登录即触发初始化会话列表
     */
    fun initSessions() {
        coroutineScope.launch((Dispatchers.IO)) {
            if (userSessions.isNotEmpty())
                userSessions.clear()
            if (groupSessions.isNotEmpty())
                groupSessions.clear()
            checkSessionsTitle()
            kotlin.runCatching {
                val relatedUserList = UserInfoRoomRepository.getInstance().getRelatedUserList()
                relatedUserList?.let { users ->
                    Log.e("ConversationUC", "initSessions, RelatedUsers:${users}")
                    commonCollect(userSessions, users, 1)
                }
                UserInfoRoomRepository.getInstance().getUnRelatedUserList()?.let { users ->
                    Log.e("ConversationUC", "initSessions, UnRelatedUsers:${users}")
                    commonCollect(userSessions, users, (relatedUserList?.size ?: 0) + 2)
                }
                val relatedGroupList = UserGroupsRoomRepository.getInstance().getRelatedGroupList()
                relatedGroupList?.let { groups ->
                    commonCollect(groupSessions, groups, 1)
                }
                UserGroupsRoomRepository.getInstance().getUnRelatedGroupList()?.let { groups ->
                    commonCollect(groupSessions, groups, (relatedGroupList?.size ?: 0) + 2)
                }
            }.onSuccess {
                Log.e(TAG, "init sessions list successful!")
            }.onFailure {
                Log.e(TAG, "init sessions list failed!:${it.message}")
            }
        }
    }


    private suspend fun commonCollect(
        sessionsListHolder: MutableList<Session>,
        imSessionHolderList: List<ImSessionHolder>?,
        insertPosition: Int
    ) {
        if (imSessionHolderList.isNullOrEmpty())
            return
        val sessions =
            imSessionHolderList.mapNotNull { sessionHolder ->
                val imObj = when (sessionHolder) {
                    is UserInfo -> sessionHolder.asImSingle()

                    is GroupInfo -> sessionHolder.asImGroup()

                    else -> return@mapNotNull null
                }
                val session = Session(imObj, ConversationUiState(emptyList()))
                sessionHolder.imSession = session
                session
            }

        if (sessions.isEmpty()) {
            return
        }
        addSessionIfNeeded(sessionsListHolder, sessions, insertPosition)
        imSessionHolderList.forEach { sessionHolder ->
            val imMessageListOfPage1 = if (sessionHolder is UserInfo) {
                FlatImMessageRoomRepository
                    .getInstance().getImMessageListByUser(sessionHolder)
            } else {
                FlatImMessageRoomRepository
                    .getInstance().getImMessageListByGroup(sessionHolder as GroupInfo)
            }
            if (sessionHolder.imSession != null) {
                sessionHolder.imSession!!.conversionState?.addMessages(imMessageListOfPage1)
            }
        }
    }

    /**
     * 初始有2个元素
     */
    private fun addSessionIfNeeded(
        sessionsListHolder: MutableList<Session>,
        newSessionList: List<Session>,
        insertPosition: Int
    ) {
        val toAddSessions = mutableListOf<Session>()
        for (newSession in newSessionList) {
            var toAdd = true
            for (existSession in sessionsListHolder) {
                if (existSession.id == newSession.id) {
                    toAdd = false
                    break
                }
            }
            if (toAdd) {
                toAddSessions.add(newSession)
            }
        }
        if (toAddSessions.isNotEmpty())
            sessionsListHolder.addAll(insertPosition, toAddSessions)
    }


}