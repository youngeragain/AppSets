//
//  LocalSyncWorker.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

import Foundation

class LocalSyncWorker : ListenableWorker {
    
    private static let TAG = "LocalSyncWorker"
    
    override func doWork() async -> Result {
        PurpleLogger.current.d(LocalSyncWorker.TAG, "doWork")
        await MainActor.run{
            RelationsUseCase.Instance.initRelationFromLocalDB()
        }
        
        return Result.Success
    }
    
}
