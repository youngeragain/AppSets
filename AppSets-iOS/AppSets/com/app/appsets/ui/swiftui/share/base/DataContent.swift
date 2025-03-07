//
//  DataContent.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/7.
//

import Foundation

protocol DataContent: Identifiable {
    var id: String {
        get
    }
    var name: String {
        get
    }
}

struct StringDataContent: DataContent {
    var id: String = UUID().uuidString

    var name: String {
        return content
    }

    var content: String

    init(content: String) {
        self.content = content
    }
}
