//
//  MockIOS.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation


class ClientCommand : NSObject {
    
    var intent: Intent
    
    var commandRunnable: (() -> Void)?
    
    init(intent: Intent, commandRunnable: ( () -> Void)?) {
        self.intent = intent
        self.commandRunnable = commandRunnable
    }
    
}

class ServerCommand : NSObject {
    
    let intent: Intent
    
    init(intent: Intent) {
        self.intent = intent
    }
    
}

class MockIOS : MockSystem {
    
    public static let TAG = "MockIOS"
    
    private var componentsInstanceMap: [String: Any] = [String: Any]()
    
    override init() {
        PurpleLogger.current.d(MockIOS.TAG, "init")
    }
    
    deinit {
        PurpleLogger.current.d(MockIOS.TAG, "deinit")
    }
    
    @objc func bootstrap() {
        let currentThread = Thread.current
        PurpleLogger.current.d(MockIOS.TAG, "bootstrap, thread:\(currentThread.debugDescription), running...")
    }
    
    func sendCommand(_ command: ServerCommand) {
        PurpleLogger.current.d(MockIOS.TAG, "sendCommand, command:\(command)")
    }
    
    @objc func onClientCommand(_ command: ClientCommand) {
        let currentThread = Thread.current
        PurpleLogger.current.d(MockIOS.TAG, "onClientCommand, thread:\(currentThread.debugDescription), command:\(command)")
        
        handleComponent(command.intent)
        
        exec(command)
        
    }
    
    func handleComponent(_ intent: Intent) {
        if(objectAddress(intent) == objectAddress(Intent.Empty)){
            PurpleLogger.current.d(MockIOS.TAG, "handleComponentType, intent is Intent.Empty, return")
            return
        }
        
        guard let componentType = intent.getComponentType() else {
            PurpleLogger.current.d(MockIOS.TAG, "handleComponent, intent's component is null, return")
            return
        }
        
        if componentType is Service.Type {
            PurpleLogger.current.d(MockIOS.TAG, "handleComponentType, intent componentType is [Service], start it")
            performStartService(intent, (componentType as? Service.Type)!)
        }
    }
    
    func performStartService<S : Service>( _ intent:Intent, _ componentType: S.Type) {
        let mirror = Mirror(reflecting: componentType)
        let description = mirror.description
        PurpleLogger.current.d(MockIOS.TAG, "performStartService, componentType mirror descrption:\(description)")
        let exist = componentsInstanceMap[description] as? S
        if let instance = exist {
            PurpleLogger.current.d(MockIOS.TAG, "performStartService, cached [Service] is not null, use it, then call instance's callback")
            let startId = Int.randomIntNumber()
            _  = instance.onStartCommand(intent, flags: nil, startId: startId)
            return
        }
        
        var serviceInstance = createComponent(componentType)
        
        if serviceInstance == nil {
            PurpleLogger.current.d(MockIOS.TAG, "performStartService, createComponent return null, use standard init")
            serviceInstance = componentType.init() as S
        }
        
        guard let instance = serviceInstance else {
            PurpleLogger.current.d(MockIOS.TAG, "performStartService, instance is null, return")
            return
        }
        
        PurpleLogger.current.d(MockIOS.TAG, "performStartService, cache the [Service] instance, then call instance's callback")
        componentsInstanceMap[description] = instance
        
        let startId = Int.randomIntNumber()
        instance.onCreate()
        instance.onStart(intent, startId: startId)
        _ = instance.onStartCommand(intent, flags: nil, startId: startId)
    }
    
    func createComponent<S>(_ componentType: S.Type) -> S? {
        PurpleLogger.current.d(MockIOS.TAG, "createComponent, componentType:\(componentType)")
        let mirror = Mirror(reflecting: componentType)
        let initializer = mirror.descendant("init") as? (() -> S?)
        if initializer != nil {
            if let component = initializer!() as? S {
                PurpleLogger.current.d(MockIOS.TAG, "createComponent, instance from initializer")
                return component as S
            }
        }
        return nil
    }
    
    @objc func exec(_ clientCommand: ClientCommand) {
        let currentThread = Thread.current
        PurpleLogger.current.d(MockIOS.TAG, "exec, thread:\(currentThread.debugDescription)")
        if let cr = clientCommand.commandRunnable {
            PurpleLogger.current.d(MockIOS.TAG, "exec, clientCommand is not null, run it!")
            cr()
        }
    }
    
}
