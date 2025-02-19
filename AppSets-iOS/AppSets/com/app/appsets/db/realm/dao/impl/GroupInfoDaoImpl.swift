//
//  GroupInfoDaoImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

class GroupInfoDaoImpl : GroupInfoDao {
    
    private static let TAG = "GroupInfoDaoImpl"
    
    func addGroup(_ groupInfoList: [GroupInfo]) {
        MockDatabase.Instance.addGroup(groupInfoList)
    }
    
    func getGroups(_ groupIdList: [String]) -> [GroupInfo] {
        return MockDatabase.Instance.getGroups(groupIdList)
    }
    
}
