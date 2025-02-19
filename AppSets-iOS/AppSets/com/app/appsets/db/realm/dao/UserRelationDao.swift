//
//  UserRelationDao.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

protocol UserRelationDao {
    
    func getRelationList() -> [UserRelation]
    
    func addUserRelation(_ realations: [UserRelation])
    
}
