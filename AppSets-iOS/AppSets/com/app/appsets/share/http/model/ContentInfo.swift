//
//  ContentInfo.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct ContentInfo: Codable {
    
    public static let TYPE_STRING = 0
    public static let TYPE_FILE = 1
    public static let TYPE_URI = 2
    public static let TYPE_BYTES = 3
    
    let id: String
    let name: String
    let size: CLong
    let type: Int
}
