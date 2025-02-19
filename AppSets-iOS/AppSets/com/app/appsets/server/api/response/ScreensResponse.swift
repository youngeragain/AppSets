//
//  ScreensResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

struct ScreensResponse : DesignResponse {
    var code: Int
    
    var info: String?
    
    var data: [ScreenInfo]?
    
    
    typealias D = [ScreenInfo]
    
}
