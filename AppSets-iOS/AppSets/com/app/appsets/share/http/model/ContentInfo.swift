//
//  ContentInfo.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//
import Foundation

class ContentInfo: Codable {
    public static let TYPE_STRING = 0
    public static let TYPE_FILE = 1
    public static let TYPE_URI = 2
    public static let TYPE_BYTES = 3

    var id: String
    var name: String
    var size: CLong
    var type: Int
    
    init(id: String, name: String, size: CLong, type: Int) {
        self.id = id
        self.name = name
        self.size = size
        self.type = type
    }
    
    enum CodingKeys: CodingKey {
        case id
        case name
        case size
        case type
    }

    func encode() -> ContentInfo {
        guard let nameData = name.data(using: .utf8) else {
            return self
        }
        name = nameData.base64EncodedString()
        return self
    }

    func decode() -> ContentInfo {
        guard let nameData = Data.init(base64Encoded: name) else {
            return self
        }
        guard let decodeName = String(data: nameData, encoding: .utf8) else {
            return self
        }
        name = decodeName

        return self
    }
}
