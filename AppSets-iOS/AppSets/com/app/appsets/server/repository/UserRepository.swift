//
//  File.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class UserRepository {
    
    private static let TAG = "UserRepository"
    
    private let userApi: UserApi = UserApiImpl.Instance
    
    func getLoggedUseInfo() async -> UserInfo? {
        PurpleLogger.current.d(UserRepository.TAG, "getLoggedUseInfo")
        
        let response = await userApi.getLoggedUseInfo()
        
        let userInfo = response.data
        
        PurpleLogger.current.d(UserRepository.TAG, "getLoggedUseInfo, userInfo is:\(userInfo)")
        
        return userInfo
    }
    
    func getFriends() async -> [UserInfo]? {
        PurpleLogger.current.d(UserRepository.TAG, "getFriends")
        
        let response = await userApi.getFriends()
        
        let friends = response.data
        
        PurpleLogger.current.d(UserRepository.TAG, "getFriends, friends is:\(friends)")
        
        return friends
    }
    
    func getChatGrups() async -> [GroupInfo]? {
        PurpleLogger.current.d(UserRepository.TAG, "getChatGrups")
        
        let response = await userApi.getChatGroupInfoList()
        
        let chatGroups = response.data
        
        PurpleLogger.current.d(UserRepository.TAG, "getChatGrups, chatGroups is:\(chatGroups)")
        
        return chatGroups
    }
    
}
