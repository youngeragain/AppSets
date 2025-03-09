//
//  AppSetsContext.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/4.
//

import Foundation

protocol Context {
    var resources: Resources { get }
}

extension Context {
    func startService(_ intent: Intent) {
        PurpleLogger.current.d("ContextEXT", "startService, context:\(self) intent:\(intent)")
        MockIOS.startService(intent)
    }
}

let LocalContext: StaticProvider<Context> = staticProvider<Context>(nil)
