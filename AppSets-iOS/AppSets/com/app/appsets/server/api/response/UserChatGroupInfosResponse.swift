//
//  UserChatGroupInfosResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

import Foundation

struct UserChatGroupInfosResponse : DesignResponse {
    
    typealias D = Array<GroupInfo>
    
    var code: Int
    
    var info: String?
    
    var data: Array<GroupInfo>?
    
}
