//
//  MockDatabase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//
import Foundation
import RealmSwift

protocol DatabaseDaos:UserInfoDao, GroupInfoDao, UserRelationDao{
    
}

class MockDatabase: Mockable {
    private static let TAG = "MockDatabase"

    public static let Instance = MockDatabase()

    private let mockTables = MockTables()

    private let realmQueue = DispatchQueue(label: "appsets.realm", qos: .default)

    private var realm: Realm? = nil

    deinit {
       
    }
    

    private init() {
        do {
            self.realm = try Realm()
            PurpleLogger.current.d(MockDatabase.TAG, "init:\(Thread.current.description)")
            printRealmPath()
        } catch {
            PurpleLogger.current.d(MockDatabase.TAG, "init, Error opening Realm: \(error)")
        }
    }
    
    
    func addUserInfo(_ userInfoList: [UserInfo]) {
        PurpleLogger.current.d(MockDatabase.TAG, "addUserInfo")
        
        guard let realm = self.realm else {
            return
        }
        try? realm.write {
            for userInfo in userInfoList {
                realm.add(userInfo, update: Realm.UpdatePolicy.modified)
            }
        }
        // for userInfo in userInfoList {
        //  await mockTables.userInfo.insert(userInfo)
        // }
    }

    func getUserInfoByUid(_ uid: String) -> UserInfo? {
        PurpleLogger.current.d(MockDatabase.TAG, "getUserInfoByUid")
        
        guard let realm = self.realm else {
            return nil
        }
        let find:UserInfo? = realm.objects(UserInfo.self).filter("uid = '\(uid)'").first
        return find

        // return await mockTables.userInfo.findById(uid)
    }

    func getUserInfoByUids(_ uids: [String]) -> [UserInfo] {
        PurpleLogger.current.d(MockDatabase.TAG, "getUserInfoByUids")
        
        guard let realm = self.realm else {
            return []
        }
        let find:[UserInfo] = realm.objects(UserInfo.self)
            .filter {
                uids.contains($0.uid)
            }.shuffled()

        return find

        // var results: [UserInfo] = []
        // for uid in uids {
        //    if let userInfo = await mockTables.userInfo.findById(uid) {
        //        results.append(userInfo)
        //    }
        // }
        // return results
    }

    func addGroup(_ groupInfoList: [GroupInfo]) {
        PurpleLogger.current.d(MockDatabase.TAG, "addGroup")
        guard let realm = self.realm else {
            return
        }
        try? realm.write{
            for groupInfo in groupInfoList {
                realm.add(groupInfo, update: Realm.UpdatePolicy.modified)
            }
        }

        // for groupInfo in groupInfoList {
        //    await mockTables.groupInfo.insert(groupInfo)
        // }
    }

    func getGroups(_ groupIdList: [String]) -> [GroupInfo] {
        PurpleLogger.current.d(MockDatabase.TAG, "getGroups")
        
        guard let realm = self.realm else {
            return []
        }
        let find:[GroupInfo] = realm.objects(GroupInfo.self)
            .filter {
                groupIdList.contains($0.groupId)
            }.shuffled()

        return find

        // var results: [GroupInfo] = []
        // for groupId in groupIdList {
        //    if let groupInfo = await mockTables.groupInfo.findById(groupId) {
        //        results.append(groupInfo)
        //    }
        // }
        // return results
    }

    func getRelationList() -> [UserRelation] {
        PurpleLogger.current.d(MockDatabase.TAG, "getRelationList")
        
        guard let realm = self.realm else {
            return []
        }
        let find:[UserRelation] = realm.objects(UserRelation.self)
            .shuffled()

        return find

        // return await mockTables.userRelation.findAll()
    }

    func addUserRelation(_ realations: [UserRelation]) {
        PurpleLogger.current.d(MockDatabase.TAG, "addUserRelation")
        guard let realm = self.realm else {
            return
        }
       
        try? realm.write {
            for relation in realations {
                realm.add(relation, update: Realm.UpdatePolicy.modified)
            }
        }
        // for userRelation in realations {
        //    await mockTables.userRelation.insert(userRelation)
        // }
    }

    func clearAllTables() {
        PurpleLogger.current.d(MockDatabase.TAG, "clearAllTables")
        Task{
            await MainActor.run{
                guard let realm = self.realm else {
                    return
                }
                try? realm.write {
                    realm.deleteAll()
                }
            }
        }
        // mockTables.clear()
    }

    private func getDefaultRealmPath() -> URL? {
        guard let url = Realm.Configuration.defaultConfiguration.fileURL else {
            return nil
        }
        return url
    }

    private func printRealmPath() {
        //file:///var/mobile/Containers/Data/Application/94C198B5-E413-4A51-BA4A-F20A848BDF8C/Documents/default.realm

         let defaultPath = getDefaultRealmPath()
         PurpleLogger.current.d(MockDatabase.TAG, "printRealmPath, path:\(defaultPath)")
    }
}
