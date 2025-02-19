//
//  LocalMessager.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/5.
//

import Foundation
import SwiftEventBus

class Looper {
    
}

class Message {
    
    let what: Int
    
    let target: Handler?
    
    var obj: Any? = nil
    
    var next:Message? = nil
    
    init(what: Int, target: Handler? = nil) {
        self.what = what
        self.target = target
    }
    
}

class Handler {
    
    protocol Callback {
        
        func handleMessage(message: Message)
        
    }
    
    func handleMessage(_ message:Message) {
        
    }
    
}

class LocalMessager {
    
    private static let TAG = "LocalMessager"
    
    public static let MAIN = 0
    
    public static let OTHER = 1

    private static let Instance:LocalMessager = LocalMessager()
    
    public static func post(_ key: String, _ value: Any?, delay: Int = 0, thread: Int = OTHER) {
        PurpleLogger.current.d(LocalMessager.TAG, "post, key:\(key), value:\(String(describing: value))")
        let message = Message(what: 0)
        message.obj = value
        if thread == MAIN {
            SwiftEventBus.postToMainThread(key, sender: message)
        }else{
            SwiftEventBus.post(key, sender: message)
        }
    }
    
    public static func observe(_ key: String, _ thread: Int = OTHER, _ handlerCallback: @escaping ((Message) -> Void)){
        PurpleLogger.current.d(LocalMessager.TAG, "observe, key:\(key)")
        if(thread == MAIN) {
            SwiftEventBus.onMainThread(self, name: key) { res in
                guard let message = res?.object as? Message else{
                    PurpleLogger.current.d(LocalMessager.TAG, "observe, key:\(key), res.object is not Message, return!")
                    return
                }
                handlerCallback(message)
            }
        }else{
            SwiftEventBus.onBackgroundThread(self, name: key) { res in
                guard let message = res?.object as? Message else{
                    PurpleLogger.current.d(LocalMessager.TAG, "observe, key:\(key), res.object is not Message, return!")
                    return
                }
                handlerCallback(message)
            }
        }
        
    }
    
    private init(){
        
    }
    
}
