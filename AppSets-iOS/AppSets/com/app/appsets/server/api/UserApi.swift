//
//  UserApi.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

protocol UserApi {
    
    func login(account:String, password:String) async -> StringResponse
    
    func getLoggedUseInfo() async -> UserInfoResponse
    
    func getFriends() async -> UserFriendsReponse
    
    func getChatGroupInfoList() async -> UserChatGroupInfosResponse
    
    func getIndexRecommendScreens(page: Int, pageSize: Int) async -> ScreensResponse
    
    func getScreensByUid(_ uid: String, page: Int, pageSize: Int) async-> ScreensResponse
    
}
