//
//  SpotLightResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

struct SpotLightResponse : DesignResponse {
    var code: Int
    
    var info: String?
    
    var data: SpotLight?
    
    
    typealias D = SpotLight
    
}
