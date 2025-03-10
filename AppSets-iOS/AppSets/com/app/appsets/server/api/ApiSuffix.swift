//
//  APISuffix.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct APISuffix {
    
    public static let API_GET_APP_TOKEN = "/appsets/apptoken/get"
    public static let API_GET_APPSETS_APP_IM_BROKER_PROPERTIES = "/appsets/client_settings/im_broker_properties"
    public static let API_USER_LOGIN = "/user/login"
    public static let API_USER_INFO = "/user/info/get"
    public static let API_USER_FRIENDS = "/user/friends"
    public static let API_USER_CHAT_GROUPS = "/user/chatgroups"
    public static let API_GET_INDEX_APPLICATIONS = "/appsets/apps/index/recommend"
    public static let API_GET_SPOTLIGHT = "/appsets/spotlight"
    public static let API_APP_GET_UPDATE = "/appsets/client/update"
    public static let API_SCREEN_INDEX_RECOMMEND = "/user/screens/index/recommend"
    public static let API_SCREEN_USER = "/user/screens/"
    
    
    public static func apiBaseUrl() -> String {
        let appConfig = AppConfig.Instance.appConfiguration
        if(String.isNullOrEmpty(appConfig.apiUrl)){
            return "\(appConfig.apiSchema)://\(appConfig.apiHost):\(appConfig.apiPort)"
        }else{
            return "\(appConfig.apiSchema)://\(appConfig.apiUrl)"
        }
        
    }
}
