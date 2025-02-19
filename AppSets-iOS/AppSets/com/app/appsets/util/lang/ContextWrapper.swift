//
//  ContextWrapper.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

class ContextWrapper : Context {
    
    private var wrapped: Context
    
    var resouces: Resources {
        return wrapped.resouces
    }
    
    required init(){
        wrapped = ContextImpl()
    }
    
    init(_ wrapped: Context) {
        self.wrapped = wrapped
    }
    
    func setWrapped(_ context: Context) {
        self.wrapped = context
    }
    
    func startService(_ intent: Intent) {
        wrapped.startService(intent)
    }
    
}
