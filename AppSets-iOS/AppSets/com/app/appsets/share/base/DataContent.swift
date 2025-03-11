//
//  DataContent.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/7.
//

import Foundation

public protocol DataContent: Identifiable {
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

struct UriDataContent: DataContent {
    var id: String = UUID().uuidString

    var name: String {
        return uri.lastPathComponent
    }

    var uri: URL

    init(uri: URL) {
        self.uri = uri
    }
}
