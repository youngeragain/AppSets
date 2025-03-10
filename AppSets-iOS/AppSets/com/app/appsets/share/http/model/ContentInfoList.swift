//
//  ContentInfoListWrapper.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct ContentInfoList: Codable {
    let uri: String
    let count: Int
    let infoList: [ContentInfo]
}
