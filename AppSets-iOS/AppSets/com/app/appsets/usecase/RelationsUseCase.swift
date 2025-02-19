//
//  UserRelationUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

class RelationsUseCase {
    private static let TAG = "RelationsUseCase"

    public static let Instance = RelationsUseCase()

    private let userRelationDao: UserRelationDao = UserRelationDaoImpl()

    private var relatedUids: Set<String> = []
    private var unRelateUids: Set<String> = []

    private var relatedGroupIdMap: [String: Set<String>] = [:]
    private var unRelateGroupIds: Set<String> = []

    private init() {
    }

    func getRelatedUserIds() -> [String] {
        return Array(relatedUids)
    }

    func getUnRelatedUserIds() -> [String] {
        return Array(unRelateUids)
    }

    func getRelatedGroupIds() -> [String] {
        var ids: [String] = []
        for id in relatedGroupIdMap.keys {
            ids.append(id)
        }
        return ids
    }

    func getUnRelatedGroupIds() -> [String] {
        return Array(unRelateGroupIds)
    }

    func getGroupIds() -> [String] {
        var ids: [String] = []
        for id in relatedGroupIdMap.keys {
            ids.append(id)
        }
        for id in unRelateGroupIds {
            ids.append(id)
        }
        PurpleLogger.current.d(RelationsUseCase.TAG, "getGroupIds, ids:\(ids)")
        return ids
    }

    func addUnRelatedUid(_ uid:String)->Bool{
        unRelateUids.insert(uid)
        return true
    }
    
    func addUnRelatedGroupId(_ groupId: String) -> Bool {
        if unRelateGroupIds.contains(groupId) {
            return false
        }
        unRelateGroupIds.insert(groupId)
        return true
    }

    func hasGroupRelated(_ groupId: String) -> Bool {
        let contains = relatedGroupIdMap.keys.contains(groupId)
        if !contains {
            _ = addUnRelatedGroupId(groupId)
        }
        return contains
    }

    func initRelatedUsersFromServer(_ idList: [String]) {
        relatedUids = Set(idList)
        PurpleLogger.current.d(RelationsUseCase.TAG, "initRelatedUsersFromServer, relatedUids:\(relatedUids)")
        let userRelations:[UserRelation] = idList.map{ uid in
            let userRelation = UserRelation()
            userRelation.id = uid
            userRelation.type = UserRelation.TYPE_USER
            userRelation.isRelate = 1
            return userRelation
        }
        userRelationDao.addUserRelation(userRelations)
    }

    func initRelatedGroupsFromServer(_ groupIdMap: [String: Set<String>]) {
        relatedGroupIdMap = groupIdMap
        PurpleLogger.current.d(RelationsUseCase.TAG, "initRelatedGroupsFromServer, relatedGroupIdMap:\(relatedGroupIdMap)")
        let userRelations:[UserRelation] = groupIdMap.map{ groupId, groupUserIds in
            let userRelation = UserRelation()
            userRelation.id = groupId
            userRelation.type = UserRelation.TYPE_GROUP
            userRelation.isRelate = 1
            return userRelation
        }
        userRelationDao.addUserRelation(userRelations)
    }

    func updateRelatedGroupIfNeeded(_ bio: any Bio) -> Bool {
        PurpleLogger.current.d(RelationsUseCase.TAG, "updateRelatedGroupIfNeeded")
        if bio is UserInfo {
            return false
        }

        if hasGroupRelated(bio.id) {
            return false
        }
        _ = addUnRelatedGroupId(bio.id)
        return true
    }
    
    func hasUserRelated(_ uid:String)->Bool {
        let contains = relatedUids.contains(uid)
        if(!contains){
            _ = addUnRelatedUid(uid)
        }
        return contains
    }

    func initRelationFromLocalDB() {
        let relationList = userRelationDao.getRelationList()
        PurpleLogger.current.d(RelationsUseCase.TAG, "initRelationFromLocalDB, relationList:\(relationList.description)}")
        if relationList.isEmpty {
            return
        }
        var relatedGroupIds:Set<String> = []
        var unRelateGroupIds:Set<String> = unRelateUids
        relationList.forEach { it in
            if it.type == UserRelation.TYPE_USER {
                if it.isRelate == 1 {
                    relatedUids.insert(it.id)
                } else if it.isRelate == 0 {
                    unRelateUids.insert(it.id)
                }
            } else if it.type == UserRelation.TYPE_GROUP {
                if it.isRelate == 1 {
                    relatedGroupIds.insert(it.id)
                } else if it.isRelate == 0 {
                    unRelateGroupIds.insert(it.id)
                }
            }
        }
        relatedGroupIds.forEach({ groupId in
            self.relatedGroupIdMap[groupId] = []
        })
    }
}
