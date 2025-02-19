//
//  AppSetsApiImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation
import Alamofire

class AppSetsApiImpl : BaseApiImpl, AppSetsApi {
    
    private static let TAG = "AppSetsApiImpl"
    
    public static let Instance:AppSetsApi = AppSetsApiImpl()
    
    private override init(){
        
    }
    
    func getAppToken() async -> StringResponse {
        PurpleLogger.current.d(AppSetsApiImpl.TAG, "getAppToken")
        let appConfig = AppConfig.Instance.appConfiguration
        let params:[String:String] = [
            "appSetsAppId" : appConfig.appSetsAppId
        ]
        return await getResponse(
            StringResponse.self,
            url: APISuffix.fullUrl(APISuffix.API_GET_APP_TOKEN),
            method: .post,
            params: params
        )
    }
    
    func getIMBrokerProperties() async -> StringResponse {
        PurpleLogger.current.d(AppSetsApiImpl.TAG, "getIMBrokerProperties")
        return await getResponse(
            StringResponse.self,
            url: APISuffix.fullUrl(APISuffix.API_GET_APPSETS_APP_IM_BROKER_PROPERTIES),
            method: .get
        )
    }
    
    func getIndexApplications() async -> ApplicationsResponse {
        PurpleLogger.current.d(AppSetsApiImpl.TAG, "getIndexApplications")
        return await getResponse(
            ApplicationsResponse.self,
            url: APISuffix.fullUrl(APISuffix.API_GET_INDEX_APPLICATIONS),
            method: .post
        )
    }
    
    func getSpotLight() async -> SpotLightResponse {
        PurpleLogger.current.d(AppSetsApiImpl.TAG, "getSpotLight")
        return await getResponse(
            SpotLightResponse.self,
            url: APISuffix.fullUrl(APISuffix.API_GET_SPOTLIGHT)
        )
        
    }
    
}
