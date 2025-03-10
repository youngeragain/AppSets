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
    
    override init(_ defaultReponseProvider: any DefaultResponseProvider, _ baseUrlProvider: any BaseUrlProvider) {
        super.init(defaultReponseProvider, baseUrlProvider)
    }
    
    func getAppToken() async -> DesignResponse<String> {
        PurpleLogger.current.d(AppSetsApiImpl.TAG, "getAppToken")
        let appConfig = AppConfig.Instance.appConfiguration
        let params:[String:String] = [
            "appSetsAppId" : appConfig.appSetsAppId
        ]
        return await getResponse(
            api: APISuffix.API_GET_APP_TOKEN,
            method: .post,
            params: params
        )
    }
    
    func getIMBrokerProperties() async -> DesignResponse<String> {
        PurpleLogger.current.d(AppSetsApiImpl.TAG, "getIMBrokerProperties")
        return await getResponse(
            api: APISuffix.API_GET_APPSETS_APP_IM_BROKER_PROPERTIES,
            method: .get
        )
    }
    
    func getIndexApplications() async -> DesignResponse<[AppWithCategory]> {
        PurpleLogger.current.d(AppSetsApiImpl.TAG, "getIndexApplications")
        return await getResponse(
            api: APISuffix.API_GET_INDEX_APPLICATIONS,
            method: .post
        )
    }
    
    func getSpotLight() async -> DesignResponse<SpotLight> {
        PurpleLogger.current.d(AppSetsApiImpl.TAG, "getSpotLight")
        return await getResponse(
            api: APISuffix.API_GET_SPOTLIGHT
        )
        
    }
    
}
