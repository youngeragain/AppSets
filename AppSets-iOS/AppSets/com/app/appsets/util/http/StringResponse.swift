//
//  StringResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct StringResponse: DesignResponse {
    
    typealias D = String
    
    var code: Int
    
    var info: String?
    
    var data: String?
    
}
