//
//  BrokerTest.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

@Observable
class BrokerTest {
    
    private static let TAG = "BrokerTest"
    
    public static let Instance = BrokerTest()
    
    private let broker: RabbitMQBroker = RabbitMQBroker()
    
    var isOnline: Bool = false
    
    func changeOnline(_ new:Bool){
        DispatchQueue.main.async {
            self.isOnline = new
        }
    }
    
    private init(){
        
    }
    
    func start() {
        PurpleLogger.current.d(BrokerTest.TAG, "start")
        Task{
            try await Task.sleep(nanoseconds: 2_000_000_000)
            if !LocalAccountManager.Instance.isLogged() {
                PurpleLogger.current.d(BrokerTest.TAG, "start, failed! because of use not login! return")
                return
            }
            
            let appConfig = AppConfig.Instance.appConfiguration
            if (String.isNullOrEmpty(appConfig.imBrokerProperties)){
                PurpleLogger.current.d(BrokerTest.TAG, "start, failed! because of imBrokerProperties isNullOrEmpty, return")
                return
            }
            PurpleLogger.current.d(BrokerTest.TAG, "start, parse config:\(appConfig.imBrokerProperties)")
            let decodedConfig = String.unWrappeBase64(appConfig.imBrokerProperties)
            PurpleLogger.current.d(BrokerTest.TAG, "start, decodedConfig:\(decodedConfig)")
            guard let configMap = String.decodeJSONFromString(jsonString: decodedConfig!) as? [String:Any] else{
                return
            }
        
            let userInfo = LocalAccountManager.Instance.userInfo
            let property = RabbitMqBrokerProperty(
                uid: userInfo.uid,
                userExchangeGroups: "G7775",
                host: configMap["rabbit-host"] as? String ?? "",
                port: configMap["rabbit-port"] as? Int ?? 5672,
                username: configMap["rabbit-admin-username"] as? String ?? "",
                password: configMap["rabbit-admin-password"] as? String ?? "",
                virtualHost: configMap["rabbit-virtual-host"] as? String ?? "",
                queuePrefix: configMap["queue-prefix"] as? String ?? "",
                routingKeyPrefix: configMap["routing-key-prefix"] as? String ?? "",
                groupExchangePrefix: "one2many-fanout-",
                groupExchangeParent: "one2many-topic",
                groupRootExchange: "one2many-fanout-root",
                groupSubRootExchange: "one2many-fanout-subroot"
            )
            let config = RabbitMQBrokerConfig(property)
            
            broker.bootstrap(config)
        }
       
    }
    
    func sendMessage(_ imObj:ImObj, _ imMessage: any ImMessage) -> Bool {
        PurpleLogger.current.d(BrokerTest.TAG, "sendMessage")
        return broker.sendMessage(imObj, imMessage)
    }
    
    func close() {
        broker.close()
    }
    
    func updateImGroupBindIfNeeded() {
        broker.updateImGroupBindIfNeeded()
    }
}
