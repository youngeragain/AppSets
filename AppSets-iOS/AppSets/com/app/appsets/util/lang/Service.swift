//
//  Service.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

class Service: ContextWrapper {
    
    private static let TAG = "Service"
    
    required init() {
        super.init()
        PurpleLogger.current.d(Service.TAG, "init")
    }
    
    deinit {
        PurpleLogger.current.d(Service.TAG, "deinit")
    }
    
    func onCreate() {
        
    }
    
    func onStart(_ intent:Intent, startId:Int) {
        
    }
    
    func onBind() -> IBinder? {
        return nil
    }
    
    func onUnBind() -> Bool {
        return false
    }
    
    func onStartCommand(_ intent:Intent, flags:Int?, startId:Int) -> Int {
        return 0
    }
    
    func onDestroy() {
        
    }
    
}
