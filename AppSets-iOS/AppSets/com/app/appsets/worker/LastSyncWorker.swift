//
//  LastDataSyncWorker.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

import Foundation

class LastSyncWorker : ListenableWorker {
    
    public static let TAG = "LastSyncWorker"
    
    public static let DATA_SYNC_FINISH = "data_sync_finish"
    
    override func doWork() async -> Result {
        PurpleLogger.current.d(LastSyncWorker.TAG, "doWork")
        LocalMessager.post(LastSyncWorker.DATA_SYNC_FINISH, nil)
        return Result.Success
    }
}
