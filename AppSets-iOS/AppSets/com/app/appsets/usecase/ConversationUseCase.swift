//
//  ConversationUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation
import SwiftUI

struct Tab {
    let name: LocalizedStringKey
    let type: String
}

class ConversationUseCase: ObservableObject {
    private static let TAG = "ConversationUseCase"

    public static let USER = "user"
    public static let GROUP = "group"
    public static let SYSTEM = "system"
    public static let AI = "ai"

    let topSpaceContentUseCase: NowSpaceContentUseCase

    let tabs: [Tab] = [
        Tab(name: "personal", type: ConversationUseCase.USER),
        Tab(name: "group", type: ConversationUseCase.GROUP),
        Tab(name: "system", type: ConversationUseCase.SYSTEM),
        Tab(name: "generative_ai", type: ConversationUseCase.AI),
    ]

    init(topSpaceContentUseCase: NowSpaceContentUseCase) {
        self.topSpaceContentUseCase = topSpaceContentUseCase
    }

    @Published public var sessionsMap: [String: ObservableList<Session>] = [
        ConversationUseCase.USER: ObservableList<Session>(),
        ConversationUseCase.GROUP: ObservableList<Session>(),
        ConversationUseCase.SYSTEM: ObservableList<Session>(),
        ConversationUseCase.AI: ObservableList<Session>(),
    ]

    @Published public var currentSession: Session? = nil

    @Published public var currentTab: String = USER

    @Published public var isShowAddActions: Bool = false

    func toggleShowAddActions() {
        withAnimation {
            isShowAddActions = !isShowAddActions
        }
    }

    func onTabActionClick(_ tabAction: TabAction) {
        if tabAction.action == TabAction.ACTION_ADD {
            toggleShowAddActions()
        }
    }

    func onUserLogout() {
        DispatchQueue.main.async {
            self.currentSession = nil
            self.sessionsMap = [
                ConversationUseCase.USER: ObservableList<Session>(),
                ConversationUseCase.GROUP: ObservableList<Session>(),
                ConversationUseCase.SYSTEM: ObservableList<Session>(),
                ConversationUseCase.AI: ObservableList<Session>(),
            ]
            self.currentTab = ConversationUseCase.USER
        }
    }

    func initSessions() {
        let t = Thread.current
        PurpleLogger.current.d(ConversationUseCase.TAG, "initSessions, thread:\(t.description)")
        DispatchQueue.main.async{
            let userSessions = self.sessionsMap[ConversationUseCase.USER]!
            let userInfoRepository = UserInfoRepository()
            let relatedUsers = userInfoRepository.getRelatedUserList()
            PurpleLogger.current.d(
                ConversationUseCase.TAG,
                "initSessions, RelatedUsers:\(relatedUsers.map({user in Pair(user.uid, user.name) }))"
            )
            self.fillImMessageToSessions(userSessions, relatedUsers)

            let unRelatedUsers = userInfoRepository.getUnRelatedUserList()
            PurpleLogger.current.d(
                ConversationUseCase.TAG,
                "initSessions, unRelatedUsers:\(unRelatedUsers.map({user in Pair(user.uid, user.name) })))"
            )
            self.fillImMessageToSessions(userSessions, unRelatedUsers)

            // Group sessions
            let groupSessions = self.sessionsMap[ConversationUseCase.GROUP]!
            let groupInfoRepository = GroupInfoRepository()
            let relatedGroups = groupInfoRepository.getRelatedGroupList()
            PurpleLogger.current.d(
                ConversationUseCase.TAG,
                "initSessions, relatedGroups:\(relatedGroups.map({group in Pair(group.groupId, group.name) })))"
            )
            self.fillImMessageToSessions(groupSessions, relatedGroups)

            let unRelatedGroups = groupInfoRepository.getUnRelatedGroupList()
            PurpleLogger.current.d(
                ConversationUseCase.TAG,
                "initSessions, unRelatedGroups:\(unRelatedGroups.map({group in Pair(group.groupId, group.name) }))"
            )
            self.fillImMessageToSessions(groupSessions, unRelatedGroups)
            
            self.updateSessionMapForPublish()
        }
    }

    private func fillImMessageToSessions(_ sessionList: ObservableList<Session>, _ sessionHolderList: [ImSessionHolder]) {
        if sessionHolderList.isEmpty {
            PurpleLogger.current.d(
                ConversationUseCase.TAG,
                "fillImMessageToSessions, imSessionHolderList isNullOrEmpty, return"
            )
            return
        }
        sessionHolderList.forEach { sessionHolder in
            
            if let bio = sessionHolder as? any Bio {
                let imObj = ImObjStatic.fromBio(bio)
                let conversationState = ConversationState()
                let session = Session(imObj: imObj, conversation: conversationState)
                ImSessionHolderStatic.updateSession(sessionHolder, session)
                //sessionHolder.session = session
                fillPageImMessageToSessions(sessionHolder, page: 1, pageSize: 20)
                _ = sessionList.add(session)
            }
        }
    }

    private func fillPageImMessageToSessions(_ sessionHolder: ImSessionHolder, page: Int, pageSize: Int) {
        var imMessageList: [any ImMessage]?
        switch sessionHolder {
        case is UserInfo:
            imMessageList = FlatImMessageRepository.Instance.getImMessageListByUser(sessionHolder as! UserInfo)

        case is GroupInfo:
            imMessageList = FlatImMessageRepository.Instance.getImMessageListByGroup(sessionHolder as! GroupInfo)

        case is Application:
            imMessageList = FlatImMessageRepository.Instance.getImMessageListByApplication(sessionHolder as! Application)

        default:
            imMessageList = nil
        }

        PurpleLogger.current.d(
            ConversationUseCase.TAG,
            "fillPageImMessageToSessions, sessionHolder:\(sessionHolder), page:\(page), pageSize:\(pageSize), imMessageList size:\(imMessageList?.count ?? 0)"
        )

        guard let messages = imMessageList else {
            return
        }
        sessionHolder.session?.conversationState.addMessages(messages)
    }

    func updateCurrentSessionBySession(_ session: Session) {
        PurpleLogger.current.d(
            ConversationUseCase.TAG,
            "updateCurrentSessionBySession, session:\(session)"
        )
        updateCurrentSession(session)
    }

    public func updateCurrentSessionByBio(_ bio: any Bio) {
        PurpleLogger.current.d(
            ConversationUseCase.TAG,
            "updateCurrentSessionByBio, bio:\(bio)"
        )
        let imObj = ImObjStatic.fromBio(bio)
        guard let session = findSessionByImObj(imObj) else {
            PurpleLogger.current.d(
                ConversationUseCase.TAG,
                "updateCurrentSessionByBio, session is not found, return"
            )
            return
        }

        updateCurrentSession(session)

        let ifNeeded = RelationsUseCase.Instance.updateRelatedGroupIfNeeded(bio)

        if ifNeeded {
            BrokerTest.Instance.updateImGroupBindIfNeeded()
        }
    }

    private func updateCurrentSession(_ session: Session) {
        PurpleLogger.current.d(
            ConversationUseCase.TAG,
            "updateCurrentSession"
        )
        currentSession = session
        topSpaceContentUseCase.removeContentIf { topSpaceObjectState in
            topSpaceObjectState is NewImMessage && (topSpaceObjectState as! NewImMessage).session.id != session.id
        }
        updateSessionMapForPublish()
    }

    private func updateSessionMapBySession(_ session: Session) {
        var foundedSessionsType: String?
        var foundedSessionIndex: Int?
        var foundedSessions: ObservableList<Session>?
        out: for (_, sessionsKV) in sessionsMap.enumerated() {
            for (i, _) in sessionsKV.value.elements.enumerated() {
                foundedSessionsType = sessionsKV.key
                foundedSessionIndex = i
                foundedSessions = sessionsKV.value
                break out
            }
        }
        guard
            let _ = foundedSessionsType,
            let sessions = foundedSessions,
            let sessionIndex = foundedSessionIndex
        else {
            return
        }
        sessions.updateByIndex(sessionIndex, t: session)
        updateSessionMapForPublish()
    }

    private func updateSessionMapForPublish() {
        DispatchQueue.main.async {
            withAnimation {
                self.sessionsMap = self.sessionsMap
                self.currentSession = self.currentSession
                PurpleLogger.current.d(
                    ConversationUseCase.TAG,
                    "updateSessionMapForPublish, currentSession messageSize:\(String(describing: self.currentSession?.conversationState.messages.elements.count))")
            }
        }
    }

    func onMessage(context: Context, imMessage: any ImMessage, isLocal: Bool) {
        PurpleLogger.current.d(
            ConversationUseCase.TAG,
            "onMessage, context:\(context), isLocal:\(isLocal), currentSession:\(String(describing: currentSession))"
        )
        DispatchQueue.main.async {
            if isLocal {
                // 发送消息
                guard let session = self.currentSession else {
                    return
                }
                _ = BrokerTest.Instance.sendMessage(session.imObj, imMessage)
            } else {
                // 收到消息
                guard let session: Session = self.findSessionByImMessage(context, imMessage) else {
                    PurpleLogger.current.d(
                        ConversationUseCase.TAG,
                        "onMessage, context:\(context), session not found!, return"
                    )
                    return
                }

                self.saveMessageToLocalDB(imMessage)
                self.showNotificationForImMessages(context, session, imMessage)

                self.updateSessionMapForPublish()
            }
        }
        
    }

    private func findSessionByImObj(_ imObj: ImObj? = nil) -> Session? {
        PurpleLogger.current.d(ConversationUseCase.TAG, "findSessionByImObj, imObjc:\(String(describing: imObj))")
        guard let imObj = imObj else {
            let sessoin = currentSession
            if sessoin == nil {
                PurpleLogger.current.d(ConversationUseCase.TAG, "findSessionByImObj, imObj is null and session is null, return")
            }
            return sessoin
        }
        switch imObj {
        case is ImSingle:
            let imSingle = imObj as! ImSingle

            if imSingle.userRoles?.contains(UserRole.ROLE_ADMIN) == true {
                return getOrCreateSession(container: sessionsMap, type: ConversationUseCase.SYSTEM, imObj: imObj)
            }

            return getOrCreateSession(container: sessionsMap, type: ConversationUseCase.USER, imObj: imObj)

        case is ImGroup:
            return getOrCreateSession(container: sessionsMap, type: ConversationUseCase.GROUP, imObj: imObj)

        default:
            return nil
        }
    }

    private func getOrCreateSession(container: [String: ObservableList<Session>], type: String, imObj: ImObj) -> Session? {
        if !container.keys.contains(type) {
            return nil
        }
        guard let sessions = container[type] else {
            return nil
        }
        for session in sessions.elements {
            if session.id == imObj.id {
                return session
            }
        }
        let conversationState = ConversationState()
        let session = Session(imObj: imObj, conversation: conversationState)
        _ = sessions.add(session)
        return session
    }

    private func findSessionByImMessage(_ context: Context, _ imMessage: any ImMessage) -> Session? {
        PurpleLogger.current.d(ConversationUseCase.TAG, "findSessionByImMessage, imMessage:\(imMessage)")

        guard let fromImObj = imMessage.parseFromImObj() else {
            PurpleLogger.current.d(
                ConversationUseCase.TAG,
                "findSessionByImMessage, parseFromImObj return null, return"
            )
            return nil
        }
        PurpleLogger.current.d(
            ConversationUseCase.TAG,
            "findSessionByImMessage, fromObj:\(fromImObj)"
        )

        switch fromImObj {
        case is ImSingle:
            if fromImObj.id == LocalAccountManager.Instance.userInfo.uid {
                if let toImObj = imMessage.parseToImObj() {
                    guard let session = findSessionByImObj(toImObj) else {
                        PurpleLogger.current.d(
                            ConversationUseCase.TAG,
                            "findSessionByImMessage, session is not found 1, return"
                        )
                        return nil
                    }
                    session.conversationState.addMessage(imMessage)
                    return session
                }
            } else {
                guard let session = findSessionByImObj(fromImObj) else {
                    PurpleLogger.current.d(
                        ConversationUseCase.TAG,
                        "findSessionByImMessage, session is not found 2, return"
                    )
                    return nil
                }
                var shouldAddToSession = true

                let isSysetmMessage = toHandleSystemMessageIfNeeded(context, imMessage, session: session)

                if isSysetmMessage {
                    shouldAddToSession = false
                }

                PurpleLogger.current.d(ConversationUseCase.TAG, "findSessionByImMessage, shouldAddToSession:\(shouldAddToSession)")

                if shouldAddToSession {
                    session.conversationState.addMessage(imMessage)
                }

                return session
            }
        case is ImGroup:
            guard let session = findSessionByImObj(fromImObj) else {
                PurpleLogger.current.d(
                    ConversationUseCase.TAG,
                    "findSessionByImMessage, session is not found 3, return"
                )
                return nil
            }
            session.conversationState.addMessage(imMessage)
            return session
        default:
            PurpleLogger.current.d(
                ConversationUseCase.TAG,
                "findSessionByImMessage, session is not found 4, return"
            )
        }
        return nil
    }

    private func toHandleSystemMessageIfNeeded(_ context: Context, _ imMessage: any ImMessage, session: Session) -> Bool {
        guard let imSystem = (imMessage as? IM_System) else {
            return false
        }
        convertAdminImMessageIfNeeded(context, imSystem)

        if imSystem.systemContentJson?.systemContentInterface is RequestFeedbackJson {
            return true
        }

        return true
    }

    private func convertAdminImMessageIfNeeded(_ context: Context, _ imSystem: IM_System) {
    }

    private func getCurrentSessionForTest(_ context: Context, _ imMessage: any ImMessage) -> Session {
        if currentSession == nil {
            let userSessions = sessionsMap[ConversationUseCase.USER]!
            let bio = LocalAccountManager.Instance.userInfo
            let imObj = ImSingle(bio)
            let conversation = ConversationState()
            let session = Session(imObj: imObj, conversation: conversation)
            _ = userSessions.add(session)
            sessionsMap[ConversationUseCase.USER] = userSessions
            currentSession = session
        }

        return currentSession!
    }

    private func saveMessageToLocalDB(_ imMessage: any ImMessage) {
        PurpleLogger.current.d(ConversationUseCase.TAG, "saveMessageToLocalDB")
    }

    private func showNotificationForImMessages(_ context: Context, _ session: Session, _ imMessage: any ImMessage) {
        PurpleLogger.current.d(ConversationUseCase.TAG, "showNotificationForImMessages")
        if imMessage.fromInfo.uid == LocalAccountManager.Instance.userInfo.uid {
            return
        }
        if session.id == currentSession?.id {
            return
        }
        topSpaceContentUseCase.onNewImMessage(session: session, imMessage: imMessage)
    }

    public func currentTabSessions() -> ObservableList<Session> {
        let sessions = sessionsMap[currentTab]!
        PurpleLogger.current.d(ConversationUseCase.TAG, "currentTabSessions(\(currentTab), \(sessions.count()))")
        return sessions
    }

    public func onSendMessage(_ context: Context, _ inputSelector: InputSelector, _ content: Any) {
        if !LocalAccountManager.Instance.isLogged() {
            PurpleLogger.current.d(ConversationUseCase.TAG, "onSendMessage, use not logged! return")
            return
        }

        guard let session = currentSession else {
            PurpleLogger.current.d(ConversationUseCase.TAG, "onSendMessage, current session object is null! return")
            return
        }

        let imMessage = ImMessageGenerator.Instance.generateBySend(
            context: context,
            session: session,
            inputSelector: inputSelector,
            content: content
        )
        onMessage(context: context, imMessage: imMessage, isLocal: true)
    }

    func getAllSimpleSessionsCount() -> Int {
        return (sessionsMap[ConversationUseCase.USER]?.elements.count ?? 0) + (sessionsMap[ConversationUseCase.GROUP]?.elements.count ?? 0)
    }

    func getAllSimpleSessionsByKeywords(_ keywords: String? = nil) -> [Session] {
        let overrideKeywords: String? = if getAllSimpleSessionsCount() > 2 {
            keywords
        } else {
            nil
        }
        var sessions: [Session] = []

        if let userSessions = sessionsMap[ConversationUseCase.USER] {
            for session in userSessions.elements {
                sessions.append(session)
            }
        }

        if let groupSessions = sessionsMap[ConversationUseCase.GROUP] {
            for session in groupSessions.elements {
                sessions.append(session)
            }
        }

        PurpleLogger.current.d(ConversationUseCase.TAG, "getAllSimpleSessionsByKeywords, keywords:\(String(describing: keywords)), overrideKeywords:\(String(describing: overrideKeywords)), before, sessions count:\(sessions.count)")
        sessions.removeAll { session in
            if session == currentSession {
                true
            } else if !String.isNullOrEmpty(overrideKeywords) {
                !session.imObj.name.contains(overrideKeywords!)
            } else {
                false
            }
        }
        PurpleLogger.current.d(ConversationUseCase.TAG, "getAllSimpleSessionsByKeywords, after, sessions count:\(sessions.count)")
        return sessions
    }

    func onDispose() {
        currentSession = nil
    }
}
