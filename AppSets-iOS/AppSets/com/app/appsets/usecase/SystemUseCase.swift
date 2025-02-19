//
//  SystemUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class SystemUseCase {
    
    private static let TAG = "SystemUseCase"
    
    init() {
        initAppToken()
    }
    
    func initAppToken() {
        PurpleLogger.current.d(SystemUseCase.TAG, "initAppToken")
        Task{
            let isSuccess = await AppSetsRepository.Instance.getAppToken()
            if isSuccess {
                PurpleLogger.current.d(SystemUseCase.TAG, "initAppToken, success")
            }else{
                PurpleLogger.current.d(SystemUseCase.TAG, "initAppToken, failed")
            }
        }
    }
    
    func updateIMBrokerProperties() {
        Task{
            let properties = await AppSetsRepository.Instance.getIMBrokerProperties()
            if(String.isNullOrEmpty(properties)){
                return
            }
            AppConfig.Instance.updateIMBrokerProperties(properties: properties!)
        }
    }
    
    public static func startServiceToSyncAllFromServer(_ context: Context) {
        PurpleLogger.current.d(SystemUseCase.TAG, "startServiceToSyncAllFromServer")
        var intent = Intent(context: context, componentType: MainService.self)
        intent.putString(MainService.WHAT_TO_DO, MainService.DO_TO_SYNC_USER_DATA_FROM_SERVER)
        context.startService(intent)
    }
    
    public static func startServiceToSyncAllFromLocal(_ context: Context) {
        PurpleLogger.current.d(SystemUseCase.TAG, "startServiceToSyncAllFromLocal")
        var intent = Intent(context: context, componentType: MainService.self)
        intent.putString(MainService.WHAT_TO_DO, MainService.DO_TO_SYNC_USER_DATA_FROM_LOCAL)
        context.startService(intent)
    }
    
}
