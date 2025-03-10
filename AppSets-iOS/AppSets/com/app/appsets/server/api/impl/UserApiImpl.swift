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
    
    override init(_ defaultReponseProvider: any DefaultResponseProvider, _ baseUrlProvider: any BaseUrlProvider) {
        super.init(defaultReponseProvider, baseUrlProvider)
    }
    
    
    func login(account: String, password: String) async -> DesignResponse<String> {
        let params:[String: Any] = [
            "account": account,
            "password": password,
            "signInDeviceInfo": DeviceInfoProvider.provideInfo(),
            "signInLocation": "Si Chuan"
        ]
        return await getResponse(
            api: APISuffix.API_USER_LOGIN,
            method: .post,
            params: params
        )
    }
    
    func getLoggedUseInfo() async -> DesignResponse<UserInfo> {
        PurpleLogger.current.d(UserApiImpl.TAG, "getLoggedUseInfo")
        return await getResponse(
            api: APISuffix.API_USER_INFO
        )
    }
    
    func getFriends() async -> DesignResponse<[UserInfo]> {
        PurpleLogger.current.d(UserApiImpl.TAG, "getFriends")
        return await getResponse(
            api: APISuffix.API_USER_FRIENDS
        )
    }
    
    func getChatGroupInfoList() async -> DesignResponse<[GroupInfo]> {
        PurpleLogger.current.d(UserApiImpl.TAG, "getChatGrups")
        return await getResponse(
            api: APISuffix.API_USER_CHAT_GROUPS
        )
    }
    
    func getIndexRecommendScreens(page: Int, pageSize: Int) async -> DesignResponse<[ScreenInfo]> {
        PurpleLogger.current.d(UserApiImpl.TAG, "getIndexRecommendScreens")
        return await getResponse(
            api: APISuffix.API_SCREEN_INDEX_RECOMMEND
        )
    }
    
    func getScreensByUid(_ uid: String, page: Int, pageSize: Int) async -> DesignResponse<[ScreenInfo]> {
        PurpleLogger.current.d(UserApiImpl.TAG, "getScreensByUid")
        return await getResponse(
            api: "\(APISuffix.API_SCREEN_USER)/\(uid)"
        )
    }
    
}
