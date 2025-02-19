//
//  MockTables.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

class MockTables : Mockable {
    
    let userInfo = MockTable<UserInfo>()
    
    let groupInfo = MockTable<GroupInfo>()
    
    let userRelation = MockTable<UserRelation>()
    
    func clear() {
        userInfo.clear()
        groupInfo.clear()
        userRelation.clear()
    }
    
}
