//
//  GroupInfoRepository.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation

class GroupInfoRepository {
    
    private static let TAG = "GroupInfoRepository"
    
    private let groupInfoDao: GroupInfoDao = GroupInfoDaoImpl()
    
    func getRelatedGroupList() -> [GroupInfo] {
        PurpleLogger.current.d(GroupInfoRepository.TAG,"getRelatedGroupList")
        let groupIds = RelationsUseCase.Instance.getRelatedGroupIds()
        return groupInfoDao.getGroups(groupIds)
    }
    
    func getUnRelatedGroupList() -> [GroupInfo] {
        PurpleLogger.current.d(GroupInfoRepository.TAG,"getUnRelatedGroupList")
        let groupIds = RelationsUseCase.Instance.getUnRelatedGroupIds()
        return groupInfoDao.getGroups(groupIds)
    }
    
    func addRelatedGroupInfoList(_ groupInfoList: [GroupInfo]) {
        PurpleLogger.current.d(GroupInfoRepository.TAG,"addRelatedGroupInfoList")
        return groupInfoDao.addGroup(groupInfoList)
    }
    
}
