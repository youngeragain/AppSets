//
//  HttpShareMethod.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/2.
//


class HttpShareMethod : ShareMethod {
    
    private static let TAG = "HttpShareMethod"
    
    private var serverBootStrap : ServerBootStrap? = nil
    
    override func initMethod() {
        open()
    }
    
    func open(){
        PurpleLogger.current.d(HttpShareMethod.TAG, "open")
        if(serverBootStrap != nil){
            return
        }
       
        deviceName = DeviceName.RANDOM
        struct Listener : ServerBootStrap.ActionListener {
            let deviceName:DeviceName
            func onSuccess() {
                PurpleLogger.current.d(HttpShareMethod.TAG, "open, success")
                startServiceDiscovery()
            }
            func onFailure(reason: String?) {
                PurpleLogger.current.d(HttpShareMethod.TAG, "open, failure")
            }
            
            func startServiceDiscovery(){
                let discoery = Discovery()
                discoery.start(deviceName: deviceName)
            }
        }
        let actionListener = Listener(deviceName: deviceName)
        serverBootStrap = ServerBootStrap()
        serverBootStrap?.main(actionListenr: actionListener)
    }
    
   
    
    override func destroy() {
        struct Listener : ServerBootStrap.ActionListener {
            func onSuccess() {
                PurpleLogger.current.d(HttpShareMethod.TAG, "destroy, success")
            }
            func onFailure(reason: String?) {
                PurpleLogger.current.d(HttpShareMethod.TAG, "destroy, failure")
            }
        }
        let actionListener = Listener()
        serverBootStrap?.close(actionListenr: actionListener)
    }
    
}
