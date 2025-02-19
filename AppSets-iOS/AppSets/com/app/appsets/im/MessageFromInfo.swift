//
//  MessageFromInfo.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct MessageFromInfo : Bio {
    
    let uid:String
    var name: String? = nil
    var avatarUrl:String? = nil
    var roles:String? = nil
    
    var id: String{
        get{
            return uid
        }
    }
    
    var bioUrl: String? = nil
    
    init(uid: String, name: String?, avatarUrl: String?, roles: String? = nil) {
        self.uid = uid
        self.name = name
        self.avatarUrl = avatarUrl
        self.roles = roles
    }
}
