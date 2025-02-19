//
//  UseInfoResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

struct UserInfoResponse : DesignResponse, Codable {
    
    typealias D = UserInfo
    
    var code: Int
    
    var info: String?
    
    var data: UserInfo?
    
}
