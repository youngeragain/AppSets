//
//  LocalAccountManager.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/4.
//

import Foundation


enum LoginStatusState{
    
    case Logged(userInfo:UserInfo, token:String)
    
    case TempLogged(userInfo:UserInfo, token:String)
    
    case NotLogged(UserInfo:UserInfo)
    
    case Expired(UserInfo:UserInfo)
    
    case LoggingIn(useInfo:UserInfo)
    
}

class LocalAccountManager: ObservableObject {
    
    private static let TAG = "LocalAccountManager"
    
    public static let Instance = LocalAccountManager()
    
    public static let MESSAGE_KEY_ON_APP_TOKEN_GOT = "on_app_token_got"
    
    //当用户登录后
    static let MESSAGE_KEY_ON_LOGIN_ACTION = "LOGIN_ACTION"
    //当用户退出登录后
    static let MESSAGE_KEY_ON_LOGOUT_ACTION = "LOGOUT_ACTION"
    
    static let SPK_USER_INFO = "USER_INFO"
    
    static let LOGIN_BY_NEW = "by_new"
    static let LOGIN_BY_RESTORE = "by_restore_status"
    static let LOGOUT_BY_MANUALLY = "by_manually"
    static let LOGOUT_BY_TOKEN_EXPIRE = "by_token_expire"
    
    private init(){
        
    }

    @Published var loginStatusState:LoginStatusState = LoginStatusState.NotLogged(UserInfo:UserInfo.defaultUser())
    
    var userInfo:UserInfo {
        get{
            switch loginStatusState{
            case .Logged(let info, _):
                return info
            case .TempLogged(let info, _):
                return info
            case .NotLogged(let info):
                return info
            case .Expired(let info):
                return info
            case .LoggingIn(let info):
                return info
            }
        }
    }
    
    public var token:String? {
        get{
            switch loginStatusState{
            case .Logged(_, let token):
                return token
            case .TempLogged(_, let token):
                return token
            default:
                return nil
            }
        }
    }
    
    private var appToken:String? = nil
    
    func provideAppToken() -> String? {
        return appToken
    }
    
    func saveAppToken(_ token:String?) {
        self.appToken = token
        if !String.isNullOrEmpty(token){
            LocalMessager.post(LocalAccountManager.MESSAGE_KEY_ON_APP_TOKEN_GOT, nil)
        }
    }
    
    func isLogged() -> Bool {
        switch loginStatusState{
        case .Logged:
            return true
        default:
            return false
        }
    }
    
    private func saveUserInfo(_ userInfo:UserInfo) {
        PurpleLogger.current.d(LocalAccountManager.TAG, "saveUserInfo")
        _ = LocalDataProvider.save(LocalAccountManager.SPK_USER_INFO, any: userInfo)
    }
    
    private func saveToken(_ token:String) {
        PurpleLogger.current.d(LocalAccountManager.TAG, "saveToken")
        _ = LocalDataProvider.save(ApiDesignEncodeStr.tokenStrToMd5, any: token)
        
    }
    
    func restoreLoginStatusStateIfNeeded(){
        switch loginStatusState
        {
        case .Logged:
            PurpleLogger.current.d(LocalAccountManager.TAG, "restoreLoginStateIfNeeded, already logged, return")
            return
        default:
            PurpleLogger.current.d(LocalAccountManager.TAG, "restoreLoginStatusStateIfNeeded")
        }
        
        let token:String? = LocalDataProvider.get(ApiDesignEncodeStr.tokenStrToMd5)
        if token == nil || token?.isEmpty  == true {
            PurpleLogger.current.d(LocalAccountManager.TAG, "restoreLoginStateIfNeeded, token is nullOrEmpty, return")
            return
        }
        
        PurpleLogger.current.d(LocalAccountManager.TAG, "restoreLoginStateIfNeeded, token:\(token!)")
        
        let userInfo:UserInfo? = LocalDataProvider.get(LocalAccountManager.SPK_USER_INFO)
        
        if userInfo == nil {
            PurpleLogger.current.d(LocalAccountManager.TAG, "restoreLoginStateIfNeeded, userInfo is null, return")
            return
        }
        onUserLogged(userInfo: userInfo!, token: token!, isTemp: false, isFromLocal: true)
    }
    
    
    func onUserLogged(userInfo:UserInfo, token:String, isTemp:Bool, isFromLocal:Bool = false) {
        PurpleLogger.current.d(LocalAccountManager.TAG, "onUserLogged")
        
        if(isTemp){
            loginStatusState = LoginStatusState.TempLogged(userInfo: userInfo, token: token)
            return
        }
        
        loginStatusState = LoginStatusState.Logged(userInfo: userInfo, token: token)
        	
        if(isFromLocal){
            LocalMessager.post(LocalAccountManager.MESSAGE_KEY_ON_LOGIN_ACTION, LocalAccountManager.LOGIN_BY_RESTORE)
        }else{
            saveUserInfo(userInfo)
            saveToken(token)
            LocalMessager.post(LocalAccountManager.MESSAGE_KEY_ON_LOGIN_ACTION, LocalAccountManager.LOGIN_BY_NEW)
        }
        
    }
    
    func onUserLogout(by: String = LOGOUT_BY_MANUALLY){
        PurpleLogger.current.d(LocalAccountManager.TAG, "onUseLogout, by:\(by)")
        loginStatusState = LoginStatusState.NotLogged(UserInfo: UserInfo.defaultUser())
        LocalMessager.post(LocalAccountManager.MESSAGE_KEY_ON_LOGOUT_ACTION, by)
        LocalDataProvider.clear()
        MockDatabase.Instance.clearAllTables()
    }
    
}
