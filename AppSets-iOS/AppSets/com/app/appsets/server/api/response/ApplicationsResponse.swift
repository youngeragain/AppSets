//
//  ApplicationsResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

struct ApplicationsResponse : DesignResponse {
    
    var code: Int
    
    var info: String?
    
    var data: [AppWithCategory]?
    
 
    typealias  D = [AppWithCategory]
    
}
