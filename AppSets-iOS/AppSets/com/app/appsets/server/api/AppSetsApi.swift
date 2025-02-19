//
//  AppSetsApi.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/8.
//

import Foundation

protocol AppSetsApi {
    
    func getAppToken() async -> StringResponse
    
    func getIndexApplications() async -> ApplicationsResponse
    
    func getSpotLight() async -> SpotLightResponse
    
    func getIMBrokerProperties() async -> StringResponse
    
}

