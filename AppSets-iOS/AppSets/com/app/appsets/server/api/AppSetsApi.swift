//
//  AppSetsApi.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/8.
//

import Foundation

protocol AppSetsApi {
    
    func getAppToken() async -> DesignResponse<String>
    
    func getIndexApplications() async -> DesignResponse<[AppWithCategory]>
    
    func getSpotLight() async -> DesignResponse<SpotLight>
    
    func getIMBrokerProperties() async -> DesignResponse<String>
    
}

