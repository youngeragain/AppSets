//
//  UserApiImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation
import Alamofire

class UserApiImpl : BaseApiImpl, UserApi {
    
    private static let TAG = "UserApiImpl"
    
    public static let Instance = UserApiImpl()
    
    private override init(){}
    
    
    func login(account: String, password: String) async -> StringResponse {
        let params:[String: Any] = [
            "account": account,
            "password": password,
            "signInDeviceInfo": DeviceInfoProvider.provideInfo(),
            "signInLocation": "Si Chuan"
        ]
        return await getResponse(
            StringResponse.self,
            url: APISuffix.fullUrl(APISuffix.API_USER_LOGIN),
            method: .post,
            params: params
        )
    }
    
    func getLoggedUseInfo() async -> UserInfoResponse {
        PurpleLogger.current.d(UserApiImpl.TAG, "getLoggedUseInfo")
        return await getResponse(
            UserInfoResponse.self,
            url: APISuffix.fullUrl(APISuffix.API_USER_INFO)
        )
    }
    
    func getFriends() async -> UserFriendsReponse {
        PurpleLogger.current.d(UserApiImpl.TAG, "getFriends")
        return await getResponse(
            UserFriendsReponse.self,
            url: APISuffix.fullUrl(APISuffix.API_USER_FRIENDS)
        )
    }
    
    func getChatGroupInfoList() async -> UserChatGroupInfosResponse {
        PurpleLogger.current.d(UserApiImpl.TAG, "getChatGrups")
        return await getResponse(
            UserChatGroupInfosResponse.self,
            url: APISuffix.fullUrl(APISuffix.API_USER_CHAT_GROUPS)
        )
    }
    
    func getIndexRecommendScreens(page: Int, pageSize: Int) async -> ScreensResponse {
        PurpleLogger.current.d(UserApiImpl.TAG, "getIndexRecommendScreens")
        return await getResponse(
            ScreensResponse.self,
            url: APISuffix.fullUrl(APISuffix.API_SCREEN_INDEX_RECOMMEND)
        )
    }
    
    func getScreensByUid(_ uid: String, page: Int, pageSize: Int) async -> ScreensResponse {
        PurpleLogger.current.d(UserApiImpl.TAG, "getScreensByUid")
        return await getResponse(
            ScreensResponse.self,
            url: "\(APISuffix.fullUrl(APISuffix.API_SCREEN_USER))/\(uid)"
        )
    }
    
}
