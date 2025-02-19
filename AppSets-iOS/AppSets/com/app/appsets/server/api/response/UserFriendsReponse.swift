//
//  UserFriendsReponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

import Foundation

struct UserFriendsReponse : DesignResponse {
    
    typealias D = Array<UserInfo>
    
    var code: Int
    
    var info: String?
    
    var data: Array<UserInfo>?
    
    
}
