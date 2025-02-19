//
//  GroupInfoDao.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

protocol UserInfoDao {
    
    func addUserInfo(_ userInfoList: [UserInfo])
    
    func getUserInfoByUid(_ uid: String) -> UserInfo?
    
    func getUserInfoByUids(_ uids: [String]) -> [UserInfo]
    
}
