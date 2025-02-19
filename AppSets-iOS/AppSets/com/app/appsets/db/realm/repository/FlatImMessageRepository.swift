//
//  FlatImMessageRepository.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

class FlatImMessageRepository {
    
    private static let TAG = "FlatImMessageRepository"
    
    public static let Instance = FlatImMessageRepository()
    
    func getImMessageListByUser(
        _ user: UserInfo,
        page: Int = 1,
        pageSize: Int = 20
    ) -> [any ImMessage]? {
        PurpleLogger.current.d(FlatImMessageRepository.TAG, "getImMessageListByUser, user\(user)")
        return nil
        
    }
    
    func getImMessageListByGroup(
        _ group: GroupInfo,
        page: Int = 1,
        pageSize: Int = 20
    ) -> [any ImMessage]? {
        PurpleLogger.current.d(FlatImMessageRepository.TAG, "getImMessageListByGroup, group\(group)")
        return nil
        
    }
    
    func getImMessageListByApplication(
        _ application: Application,
        page: Int = 1,
        pageSize: Int = 20
    ) -> [any ImMessage]? {
        PurpleLogger.current.d(FlatImMessageRepository.TAG, "getImMessageListByApplication, application\(application)")
        return nil
        
    }
    
}
