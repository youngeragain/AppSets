//
//  ContentInfoListWrapper.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//
import Foundation

class ContentInfoList: Codable {
    var uri: String
    var count: Int
    var infoList: [ContentInfo]
    
    init(uri: String, count: Int, infoList: [ContentInfo]) {
        self.uri = uri
        self.count = count
        self.infoList = infoList
    }
    
    enum CodingKeys: CodingKey {
        case uri
        case count
        case infoList
    }
  
    func decode() -> ContentInfoList {
        infoList.forEach { info in
            _ = info.decode()
        }
        return self
    }

    func encode() -> ContentInfoList {
        infoList.forEach { info in
            _ = info.encode()
        }
        return self
    }
    
}
