//
//  ListenableWorker.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

import Foundation

class ListenableWorker : BaseWorker {
    
    private var mParameters: WorkerParameters
    
    init(_ mParameters: WorkerParameters) {
        self.mParameters = mParameters
    }
    
    func getInputData() -> WorkerData {
        return mParameters.mInputData
    }
    
}
