//
//  File.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class UserRepository {
    private static let TAG = "UserRepository"

    private let userApi: UserApi = UserApiImpl(ApiResponseProvider.INSTANCE, ApiBaseUrlProvider.INSTANCE)

    func getLoggedUseInfo() async -> UserInfo? {
        PurpleLogger.current.d(UserRepository.TAG, "getLoggedUseInfo")

        let response = await userApi.getLoggedUseInfo()

        let userInfo = response.data

        PurpleLogger.current.d(UserRepository.TAG, "getLoggedUseInfo, userInfo is:\(String(describing: userInfo))")

        return userInfo
    }

    func getFriends() async -> [UserInfo]? {
        PurpleLogger.current.d(UserRepository.TAG, "getFriends")

        let response = await userApi.getFriends()

        let friends = response.data

        PurpleLogger.current.d(UserRepository.TAG, "getFriends, friends is:\(String(describing: friends))")

        return friends
    }

    func getChatGrups() async -> [GroupInfo]? {
        PurpleLogger.current.d(UserRepository.TAG, "getChatGrups")

        let response = await userApi.getChatGroupInfoList()

        let chatGroups = response.data

        PurpleLogger.current.d(UserRepository.TAG, "getChatGrups, chatGroups is:\(String(describing: chatGroups))")

        return chatGroups
    }
}
