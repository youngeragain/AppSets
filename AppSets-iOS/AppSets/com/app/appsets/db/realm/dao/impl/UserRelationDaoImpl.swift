//
//  UserRelationDaoImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

class UserRelationDaoImpl : UserRelationDao {
    
    private static let TAG = "UserRelationDaoImpl"
    
    func getRelationList() -> [UserRelation] {
        return MockDatabase.Instance.getRelationList()
    }
    
    func addUserRelation(_ realations: [UserRelation]) {
        MockDatabase.Instance.addUserRelation(realations)
    }
    
}
