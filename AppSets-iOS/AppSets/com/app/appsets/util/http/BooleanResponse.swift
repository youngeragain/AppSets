//
//  StringResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct BooleanResponse: DesignResponse {
    
    typealias D = Bool
    
    var code: Int
    
    var info: String?
    
    var data: Bool?
    
}
