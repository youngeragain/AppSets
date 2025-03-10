//
//  ScreenRepository.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

class ScreenRepository {
    private static let TAG = "ScreenRepository"

    private let userApi: UserApi = UserApiImpl(ApiResponseProvider.INSTANCE, ApiBaseUrlProvider.INSTANCE)

    func getIndexRecommendScreens(page: Int = 1, pageSize: Int = 20) async -> [ScreenInfo]? {
        let response = await userApi.getIndexRecommendScreens(page: page, pageSize: pageSize)
        let screens = response.data
        return screens
    }

    func getScreenByUid(_ uid: String, page: Int = 1, pageSize: Int = 20) async -> [ScreenInfo]? {
        let response = await userApi.getScreensByUid(uid, page: page, pageSize: pageSize)
        let screens = response.data
        return screens
    }
}
