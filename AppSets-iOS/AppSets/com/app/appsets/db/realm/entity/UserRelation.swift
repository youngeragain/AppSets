//
//  UserRelation.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation
import RealmSwift

class UserRelation : Object, Identifiable {
    
    @Persisted(primaryKey: true)
    var id: String
    @Persisted
    var type: String
    @Persisted
    var isRelate: Int
    
    public static let TYPE_GROUP = "group"
    
    public static let TYPE_USER = "user"
}
