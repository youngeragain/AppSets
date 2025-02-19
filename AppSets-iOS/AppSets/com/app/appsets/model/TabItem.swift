//
//  TabItem.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/16.
//

import Foundation

struct TabItem {
    let icon: String
    let route: String
    let name: String?
    
    var tabActions: [TabAction]? = nil

    init(icon: String, route: String, name:String? = nil) {
        self.icon = icon
        self.route = route
        self.name = name
    }
    
     mutating func addTabAction(_ tabAction: TabAction) {
        tabActions?.append(tabAction)
    }
    
    mutating func setActions(_ actions: [TabAction]) {
        self.tabActions = actions
    }
}

struct TabAction {
    
    public static let ACTION_REFRESH = "refresh"
    public static let ACTION_ADD = "add_action"
    
    let icon: String
    let route: String?
    let name: String?
    let action:String?
    
    init(icon: String, route: String? = nil, name:String? = nil, action:String? = nil) {
        self.icon = icon
        self.route = route
        self.name = name
        self.action = action
    }
}
