//
//  MainService.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

class MainService : Service {

    public static let TAG = "MainService"
    
    public static let WHAT_TO_DO = "what_to_do"
    public static let DO_TO_SYNC_USER_FRIENDS_FROM_SERVER = "to_sync_user_friends_from_server"
    public static let DO_TO_SYNC_USER_GROUPS_FROM_SERVER = "to_sync_user_groups_from_server"
    public static let DO_TO_SYNC_USER_DATA_FROM_SERVER = "to_sync_user_data_from_server"
    public static let DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL = "to_sync_user_friends_from_local"
    public static let DO_TO_SYNC_USER_GROUPS_FROM_LOCAL = "to_sync_user_groups_from_local"
    public static let DO_TO_SYNC_USER_DATA_FROM_LOCAL = "to_sync_user_data_from_local"
    
    required init() {
        super.init()
        PurpleLogger.current.d(MainService.TAG, "init")
    }
    
    deinit {
        PurpleLogger.current.d(MainService.TAG, "deinit")
    }
    
    override func onStartCommand(_ intent: Intent, flags: Int?, startId: Int) -> Int {
        let t = Thread.current
        PurpleLogger.current.d(MainService.TAG, "onStartCommand, thread:\(t.description)")
        if let whatToDo = intent.getString(MainService.WHAT_TO_DO) {
            switch whatToDo {
            case MainService.DO_TO_SYNC_USER_DATA_FROM_SERVER:
                toSyncUserDataFromServer("friends,groups")
                
            case MainService.DO_TO_SYNC_USER_FRIENDS_FROM_SERVER:
                toSyncUserDataFromServer("friends")
                
            case MainService.DO_TO_SYNC_USER_GROUPS_FROM_SERVER:
                toSyncUserDataFromServer("groups")
                
            case MainService.DO_TO_SYNC_USER_DATA_FROM_LOCAL:
                toSyncUserDataFromLocal("friends,groups")
                
            case MainService.DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL:
                toSyncUserDataFromLocal("friends")
                
            case MainService.DO_TO_SYNC_USER_GROUPS_FROM_LOCAL:
                toSyncUserDataFromLocal("groups")
                
            default:
                PurpleLogger.current.d(MainService.TAG, "onStartCommand, whatToDo nothing!")
            }
        }
        
        return super.onStartCommand(intent, flags: flags, startId: startId)
    }
    
    private func toSyncUserDataFromServer(_ conditions: String) {
        PurpleLogger.current.d(MainService.TAG, "toSyncUserDataFromServer, conditions:\(conditions)")
        let data = WorkerData(["conditions": conditions])
        let workerParams = WorkerParameters(mId: UUID(), mInputData: data)
        let serverSyncWroker = ServerSyncWorker(workerParams)
        let lastSyncWorker = LastSyncWorker(workerParams)
        Task{
            _ = await serverSyncWroker.doWork()
            _ = await lastSyncWorker.doWork()
        }
        
    }
    
    private func toSyncUserDataFromLocal(_ conditions: String) {
        PurpleLogger.current.d(MainService.TAG, "toSyncUserDataFromLocal, conditions:\(conditions)")
        let data = WorkerData(["conditions": conditions])
        let workerParams = WorkerParameters(mId: UUID(), mInputData: data)
        let localSyncWroker = LocalSyncWorker(workerParams)
        let lastSyncWorker = LastSyncWorker(workerParams)
        Task{
            _ = await localSyncWroker.doWork()
            _ = await lastSyncWorker.doWork()
        }
        
    }
    
}
