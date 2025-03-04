//
//  AppConfig.swift
//  AppSets"
//
//  Created by caiju Xu on 2024/12/20.
//

class AppConfiguration{
    var canSignUp:Bool
    var apiSchema:String
    var apiHost:String
    var apiPort:Int
    var apiUrl:String
    var appSetsAppId:String
    var imBrokerProperties:String
    
    init(canSignUp: Bool, apiSchema: String, apiHost: String, apiPort: Int, apiUrl: String, appSetsAppId: String, imBrokerProperties: String) {
        self.canSignUp = canSignUp
        self.apiSchema = apiSchema
        self.apiHost = apiHost
        self.apiPort = apiPort
        self.apiUrl = apiUrl
        self.appSetsAppId = appSetsAppId
        self.imBrokerProperties = imBrokerProperties
    }
    
}

class AppConfig {
    
    private static let TAG = "AppConfig"
    
    public static let Instance: AppConfig = AppConfig()
    
    private var updateTime: Int = 0
    
    private var updating: Bool = false
    
    var appConfiguration: AppConfiguration = AppConfiguration(
        canSignUp: true,
        apiSchema: "https",
        apiHost: "192.168.15.184",
        apiPort: 8084,
        apiUrl: "",
        appSetsAppId: "APPSETS2023071579019880338529",
        imBrokerProperties: ""
    )
    
    private init(){
        
    }
    
    
    func updateIMBrokerProperties(properties:String){
        PurpleLogger.current.d(AppConfig.TAG, "updateIMBrokerProperties")
        appConfiguration.imBrokerProperties = properties
    }
}
