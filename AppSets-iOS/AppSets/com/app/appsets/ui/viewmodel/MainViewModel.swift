//
//  MainViewModel.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/28.
//

import Foundation

@Observable
class MainViewModel {
    
    private static let TAG = "MainViewModel"
    
    let systemUseCase: SystemUseCase = SystemUseCase()
    
    let navigationUseCase: NavigationUseCase = NavigationUseCase()
    
    let mediaPlaybackUseCase: MediaPlaybackUseCase = MediaPlaybackUseCase()
    
    let userLoginUseCase: UserLoginUserCase = UserLoginUserCase()
    
    let screenUseCase: ScreenUseCase = ScreenUseCase()
    
    let userInfoUseCase: UserInfoUseCase = UserInfoUseCase()
    
    let searchUseCase: SearchUseCase = SearchUseCase()
    
    let appsUseCase: AppsUseCase = AppsUseCase()
    
    let startUseCase: StartUseCase = StartUseCase()
    
    
    let nowSpaceContentUseCase: NowSpaceContentUseCase
    
    let conversationUseCase: ConversationUseCase
    
    init() {
        PurpleLogger.current.d(MainViewModel.TAG, "init")
        self.nowSpaceContentUseCase = NowSpaceContentUseCase()
        conversationUseCase = ConversationUseCase(topSpaceContentUseCase: nowSpaceContentUseCase)
        prepare()
    }
    
    private func prepare() {
        PurpleLogger.current.d(MainViewModel.TAG, "prepare")
        observeSomeThings()
        doActionsOnCreated()
    }
    
    private func observeSomeThings() {
        PurpleLogger.current.d(MainViewModel.TAG, "observeSomeThings")
        observeAppTokenStatus()
        observeLoginStatus()
        observeDataSyncStatus()
        observeImMessages()
    }
    
    private func observeAppTokenStatus() {
        PurpleLogger.current.d(MainViewModel.TAG, "observeAppTokenStatus")
        LocalMessager.observe(LocalAccountManager.MESSAGE_KEY_ON_APP_TOKEN_GOT){ message in
            PurpleLogger.current.d(MainViewModel.TAG, "observeAppTokenStatus, on_app_token_got, message:\(String(describing: message.obj))")
            self.doActionsWhenAppTokenGot(LocalContext.current)
        }
    }
    
    private func observeLoginStatus() {
        PurpleLogger.current.d(MainViewModel.TAG, "observeLoginStatus")
        LocalMessager.observe(LocalAccountManager.MESSAGE_KEY_ON_LOGIN_ACTION) { message in
            PurpleLogger.current.d(MainViewModel.TAG, "observeLoginStatus, onUserLoginActoin, message:\(String(describing: message.obj))")
            let loginActionBy = message.obj as? String ?? ""
            PurpleLogger.current.d(MainViewModel.TAG, "observeLoginStatus, onUserLoginActoin, by:\(loginActionBy)")
            let context = LocalContext.current
            if loginActionBy == LocalAccountManager.LOGIN_BY_NEW {
                self.navigationUseCase.navigationUp()
                SystemUseCase.startServiceToSyncAllFromServer(context)
            } else if (loginActionBy == LocalAccountManager.LOGIN_BY_RESTORE) {
                SystemUseCase.startServiceToSyncAllFromLocal(context)
            }
        }
        
        LocalMessager.observe(LocalAccountManager.MESSAGE_KEY_ON_LOGOUT_ACTION) { message in
            self.conversationUseCase.onUserLogout()
        }
    }
    
    private func observeDataSyncStatus() {
        PurpleLogger.current.d(MainViewModel.TAG, "observeDataSyncStatus")
        LocalMessager.observe(LastSyncWorker.DATA_SYNC_FINISH) { message in
            let t = Thread.current
            PurpleLogger.current.d(MainViewModel.TAG, "observeDataSyncStatus, DATA_SYNC_FINISH, thread:\(t.description)")
            self.conversationUseCase.initSessions()
            BrokerTest.Instance.start()
        }
    }
    
    
    private func observeImMessages() {
        PurpleLogger.current.d(MainViewModel.TAG, "observeImMessages")
        LocalMessager.observe(RabbitMQBroker.MESSAGE_KEY_ON_IM_MESSAGE) { message in
            guard let imMessage = (message.obj as? any ImMessage) else {
                PurpleLogger.current.d(MainViewModel.TAG, "observeImMessages, received data is not imMessage, return")
                return
            }
            let context = LocalContext.current
            self.conversationUseCase.onMessage(context: context, imMessage: imMessage, isLocal: false)
        }
    }
    
    private func doActionsOnCreated(){
        PurpleLogger.current.d(MainViewModel.TAG, "doActionsOnCreated")
        LocalAccountManager.Instance.restoreLoginStatusStateIfNeeded()
    }
    
    private func doActionsWhenAppTokenGot(_ context: Context) {
        PurpleLogger.current.d(MainViewModel.TAG, "doActionsWhenAppTokenGot")
        systemUseCase.updateIMBrokerProperties()
        startUseCase.doInitData(context)
    }
    
}
