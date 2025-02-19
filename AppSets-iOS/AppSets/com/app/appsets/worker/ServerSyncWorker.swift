//
//  ServerSyncWorker.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

import Foundation

class ServerSyncWorker : ListenableWorker {
    
    private static let TAG = "ServerSyncWorker"
    
    override func doWork() async -> Result {
        PurpleLogger.current.d(ServerSyncWorker.TAG, "doWork")
        
        guard let conditions = getInputData().getString("conditions") else {
            
            return Result.Failed
        }
        
        PurpleLogger.current.d(ServerSyncWorker.TAG, "doWork, conditions:\(conditions)")
        
        
        var hasSupportConditions = false
        
        if conditions.contains("friends") {
            hasSupportConditions = true
            let friends:[UserInfo]? = await UserRepository().getFriends()
            PurpleLogger.current.d(ServerSyncWorker.TAG, "doWork, friends:\(String(describing: friends))")
           
            await MainActor.run{
                if let userInfoList = friends {
                    var uids: [String] = []
                    userInfoList.forEach{ userInfo in
                        if !String.isNullOrEmpty(userInfo.uid) {
                            uids.append(userInfo.uid)
                        }
                    }
                    UserInfoRepository().addRelatedUserInfoList(userInfoList)
                    RelationsUseCase.Instance.initRelatedUsersFromServer(uids)
                }
            }
        }
        
        
        if conditions.contains("groups") {
            hasSupportConditions = true
            let groups:[GroupInfo]? = await UserRepository().getChatGrups()
            PurpleLogger.current.d(ServerSyncWorker.TAG, "doWork, groups:\(String(describing: groups))")
            await MainActor.run {
                if let groupInfoList = groups {
                    var groupIdMap: [String: Set<String>] = [String: Set<String>] ()
                    groups?.forEach{ groupInfo in
                        var uids: Set<String> = []
                        groupInfo.userInfoList?.forEach({ userInfo in
                            if !String.isNullOrEmpty(userInfo.uid) {
                                uids.insert(userInfo.uid)
                            }
                        })
                        if !uids.isEmpty {
                            groupIdMap[groupInfo.groupId] = uids
                        }
                    }
                    GroupInfoRepository().addRelatedGroupInfoList(groupInfoList)
                    
                    RelationsUseCase.Instance.initRelatedGroupsFromServer(groupIdMap)
                   
                  
                }
            }
        }
        
        if hasSupportConditions {
            return Result.Success
        }
        
        return Result.Failed
        
    }
    
}
