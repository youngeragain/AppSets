//
//  UserInfoUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/10.
//

import Foundation

class UserInfoUseCase {
    
    var currentUserInfo: UserInfo = LocalAccountManager.Instance.userInfo
    
    func updateCurrentUserInfoByUid(_ uid: String, requestOnlyUserInfo: Bool = false) {
        currentUserInfo = LocalAccountManager.Instance.userInfo
    }
    
    func updateCurrentUserInfo(_ userInfo: UserInfo) {
        self.currentUserInfo = userInfo
    }
    
}
