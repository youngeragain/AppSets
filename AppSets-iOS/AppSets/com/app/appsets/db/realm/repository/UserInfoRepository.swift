//
//  UserInfoRepository.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

class UserInfoRepository {
    
    private static let TAG = "UserInfoRepository"
    
    private let userInfoDao: UserInfoDao = UserInfoDaoImpl()
    
    func addRelatedUserInfoList(_ userInfoList: [UserInfo]) {
        PurpleLogger.current.d(UserInfoRepository.TAG, "addRelatedUserInfoList")
        userInfoDao.addUserInfo(userInfoList)
    }
    
    func getRelatedUserList() -> [UserInfo] {
        PurpleLogger.current.d(UserInfoRepository.TAG, "getRelatedUserList")
        let uids = RelationsUseCase.Instance.getRelatedUserIds()
        return userInfoDao.getUserInfoByUids(uids)
    }
    
    func getUnRelatedUserList() -> [UserInfo] {
        PurpleLogger.current.d(UserInfoRepository.TAG, "getUnRelatedUserList")
        let uids = RelationsUseCase.Instance.getUnRelatedUserIds()
        return userInfoDao.getUserInfoByUids(uids)
    }
    
}
