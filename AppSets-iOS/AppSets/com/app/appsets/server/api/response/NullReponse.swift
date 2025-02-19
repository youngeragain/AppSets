//
//  NullReponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

struct NullReponse : DesignResponse {
    var code: Int
    
    var info: String?
    
    var data: Nothing?
    
    typealias D = Nothing
}
