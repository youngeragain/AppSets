//
//  UserInfoDaoImpl.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

class UserInfoDaoImpl : UserInfoDao {
    
    private static let TAG = "UserInfoDaoImpl"
    
    func addUserInfo(_ userInfoList: [UserInfo]) {
        MockDatabase.Instance.addUserInfo(userInfoList)
    }
    
    func getUserInfoByUid(_ uid: String) -> UserInfo? {
        return MockDatabase.Instance.getUserInfoByUid(uid)
    }
    
    func getUserInfoByUids(_ uids: [String]) -> [UserInfo] {
        return MockDatabase.Instance.getUserInfoByUids(uids)
    }
    
}
