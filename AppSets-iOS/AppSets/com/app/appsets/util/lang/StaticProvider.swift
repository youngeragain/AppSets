//
//  StaticProvider.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/4.
//

import Foundation


class StaticProvider<T> {
    
    private var key:String? = nil
    
    var t:T? = nil
    
    var current: T {
        get {
            if t == nil {
                Exception.IllegalStateException(message: "current is null, nothing can provide!")
            }
            return t!
            
        }
    }
    
    func provide(provider:()->T?) {
        t = provider()
    }
    
    func provide(t:T?) {
        provide(provider: { t })
    }
    
    
}

func staticProvider<T>(_ t:T? = nil) -> StaticProvider<T> {
    let staticProvider = StaticProvider<T>()
    staticProvider.provide(t: t)
    return staticProvider
}


func staticProvider<T>(_ provider:()->T?) -> StaticProvider<T> {
    let staticProvider = StaticProvider<T>()
    staticProvider.provide(provider:provider)
    return staticProvider
}


