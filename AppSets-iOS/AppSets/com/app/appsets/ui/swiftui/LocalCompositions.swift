//
//  LocalStatic.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

let LocalPageRouteNameNeedLoggedProvider:StaticProvider<Array<String>> = staticProvider {
    [
        PageRouteNameProvider.ConversationOverviewPage,
        PageRouteNameProvider.ConversationDetailsPage,
        PageRouteNameProvider.UserProfilePage
    ]
}
