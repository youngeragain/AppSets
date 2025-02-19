//
//  ContextImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

class ContextImpl : Context {
    
    private static let TAG = "ContextImpl"
    
    internal var resouces: Resources = Resources()
    
    init() {
        PurpleLogger.current.d(ContextImpl.TAG, "init")
    }
    
    deinit {
        PurpleLogger.current.d(ContextImpl.TAG, "deinit")
    }
}
