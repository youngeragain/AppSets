//
//  GroupInfo.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation
import RealmSwift

final class GroupInfo: Object, ImSessionHolder, Bio, Codable {
   
    
    @Persisted(primaryKey: true)
    var groupId:String
    @Persisted
    var name:String? = nil
    @Persisted
    var currentOwnerUid:String? = nil
    @Persisted
    var type:Int? = nil
    @Persisted
    var iconUrl:String? = nil
    @Persisted
    var instroduction:String? = nil
    @Persisted
    var isPublic:Int? = nil
    @Persisted
    var maxMembers:Int? = nil
    
    var userInfoList:[UserInfo]? = nil
    
    var id: String {
        return groupId
    }
    
    var bioUrl: String? {
        get{
            return iconUrl
        }
        set{
            
        }
    }
    
    var session: Session? = nil
    
    enum CodingKeys: String, CodingKey {
        case groupId
        case name
        case currentOwnerUid
        case type
        case iconUrl
        case instroduction
        case isPublic = "public"
        case maxMembers
        case userInfoList
    }
}

struct UserRole {
    public static let ROLE_ADMIN = "amdin"
    public static let ROLE_UNDEFINED = "undefined"
}
