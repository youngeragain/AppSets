//
//  NavigationBarUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/16.
//

import Foundation
import SwiftUI

@Observable
class NavigationUseCase {
    
    private static let TAG = "NavigationUseCase"
    
    private static let STARTER_ROUTE = PageRouteNameProvider.AppsCenterPage
    
    var tabItems: Array<TabItem> = []
    
    private var routes: [String] = [STARTER_ROUTE]
    
    var route: String = STARTER_ROUTE
    
    var visible: Bool = true
    
    init() {
        initTabItems()
    }
    
    func getRawRoute() -> String {
        return route
    }
    
    func initTabItems(){
        PurpleLogger.current.d(NavigationUseCase.TAG, "initTabItems")
     
        var tab2 = TabItem(icon:"drawable/shopping_bag-shopping_bag_symbol", route: PageRouteNameProvider.AppsCenterPage)
        var tab2Actions: [TabAction] = []
        let tab2Action1 = TabAction(icon: "drawable/architecture_architecture_symbol", route: PageRouteNameProvider.AppToolsPage)
        let tab2Action2 = TabAction(icon: "drawable/ic_appsets_plus", route: PageRouteNameProvider.CreateAppPage)
        tab2Actions.append(tab2Action1)
        tab2Actions.append(tab2Action2)
        tab2.setActions(tab2Actions)
        
        var tab3 = TabItem(icon:"drawable/explore-explore_symbol", route: PageRouteNameProvider.OutSidePage)
        var tab3Actions: [TabAction] = []
        let tab3Action1 = TabAction(icon: "drawable/slow_motion_video-slow_motion_video_symbol", route: PageRouteNameProvider.MeidaPage)
        let tab3Action2 = TabAction(icon: "drawable/refresh-refresh_symbol", action: TabAction.ACTION_REFRESH)
        let tab3Action3 = TabAction(icon: "drawable/ic_appsets_plus", route: PageRouteNameProvider.CreateScreenPage)
        tab3Actions.append(tab3Action1)
        tab3Actions.append(tab3Action2)
        tab3Actions.append(tab3Action3)
        tab3.setActions(tab3Actions)
        
        var tab4 = TabItem(icon:"drawable/bubble_chart-bubble_chart_symbol", route: PageRouteNameProvider.ConversationOverviewPage)
        var tab4Actions: [TabAction] = []
        let tab4Action1 = TabAction(icon: "drawable/ic_appsets_plus", action: TabAction.ACTION_ADD)
        tab4Actions.append(tab4Action1)
        tab4.setActions(tab4Actions)
        
        tabItems.append(tab2)
        tabItems.append(tab3)
        tabItems.append(tab4)
    }
    
    func updateRoute(_ route: String, pushToStack: Bool){
        PurpleLogger.current.d(NavigationUseCase.TAG, "updateRoute, route:\(route), pushToStack:\(pushToStack)")
      
        if !pushToStack {
            routes.removeLast()
        }
        routes.append(route)
        PurpleLogger.current.d(NavigationUseCase.TAG, "updateRoute, current route stack,\(routes)")
        withTransaction(.init(animation: .bouncy)){
            self.route = route
        }
    }
    
    func onTabItemClick(tabItem: TabItem){
        PurpleLogger.current.d(NavigationUseCase.TAG, "onTabItemClick, tabItem:\(tabItem)")
        let pushToStack = tabItem.route == PageRouteNameProvider.ConversationDetailsPage
        self.updateRoute(tabItem.route, pushToStack: pushToStack)
    }
    
    func navigationUp(_ by: String = "click", force: Bool = false) {
        PurpleLogger.current.d(NavigationUseCase.TAG, "navigationUp, by:\(by), force:\(force)")
        if force {
            updateRoute(NavigationUseCase.STARTER_ROUTE, pushToStack: false)
        }
        if routes.isEmpty {
            PurpleLogger.current.d(NavigationUseCase.TAG, "navigationUp, routes isEmpty, return")
            return
        }
        if routes.count == 1 {
            PurpleLogger.current.d(NavigationUseCase.TAG, "navigationUp, routes size == 1")
            updateRoute(routes.last!, pushToStack: false)
            return
        }
        routes.removeLast()
        guard let stackTopRoute = routes.last else {
            PurpleLogger.current.d(NavigationUseCase.TAG, "navigationUp, routes stackTopRoute isNullOrEmpty, return")
            return
        }
        updateRoute(stackTopRoute, pushToStack: false)
    }
    
    func navigateTo(_ route: String) {
        PurpleLogger.current.d(NavigationUseCase.TAG, "navigateTo, route:\(route)")
        updateRoute(route, pushToStack: true)
    }
}
