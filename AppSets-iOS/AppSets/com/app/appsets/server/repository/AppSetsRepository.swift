//
//  AppSetsRepository.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class AppSetsRepository {
    
    private static let TAG = "AppSetsRepository"
    
    public static let Instance = AppSetsRepository()
    
    private let appSestApi: AppSetsApi = AppSetsApiImpl.Instance
    
    func getAppToken() async -> Bool {
        let response = await appSestApi.getAppToken()
        
        let appToken = response.data
        
        if String.isNullOrEmpty(appToken) {
            PurpleLogger.current.d(AppSetsRepository.TAG, "getAppToken, token isNullOrEmpty")
            return false
        }
        LocalAccountManager.Instance.saveAppToken(appToken)
        
        PurpleLogger.current.d(AppSetsRepository.TAG, "getAppToken, token is:\((appToken ?? ""))")
        
        return true
    }
    
    func getIMBrokerProperties() async -> String? {
        let response = await appSestApi.getIMBrokerProperties()
        
        let properties = response.data
        
    
        PurpleLogger.current.d(AppSetsRepository.TAG, "getIMBrokerProperties, properties is:\((properties ?? ""))")
        
        return properties
    }
    
    func getIndexApplications(_ context: Context) async -> [AppWithCategory]? {
        let response = await appSestApi.getIndexApplications()
        
        let appWithCategoryList = response.data
        
        return appWithCategoryList
    }
    
    func getSpotLight() async -> SpotLight? {
        let response = await appSestApi.getSpotLight()
        
        let spotLight = response.data
        
        return spotLight
    }
    
}
