//
//  Worker.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

import Foundation

protocol Worker {
    
    func doWork() async -> Result
    
}

class BaseWorker: Worker{
    func doWork() async -> Result {
        return Result.Success
    }
}
