//
//  UserInfoDao.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

protocol GroupInfoDao {
    
    func addGroup(_ groupInfoList: [GroupInfo])
    
    func getGroups(_ groupIdList: [String]) -> [GroupInfo]
    
}
