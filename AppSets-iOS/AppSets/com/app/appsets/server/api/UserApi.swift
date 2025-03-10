//
//  UserApi.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

protocol UserApi {
    
    func login(account:String, password:String) async -> DesignResponse<String>
    
    func getLoggedUseInfo() async -> DesignResponse<UserInfo>
    
    func getFriends() async -> DesignResponse<[UserInfo]>
    
    func getChatGroupInfoList() async -> DesignResponse<[GroupInfo]>
    
    func getIndexRecommendScreens(page: Int, pageSize: Int) async -> DesignResponse<[ScreenInfo]>
    
    func getScreensByUid(_ uid: String, page: Int, pageSize: Int) async-> DesignResponse<[ScreenInfo]>
    
}
