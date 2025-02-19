//
//  UserScreenInfo.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import Foundation


struct ScreenInfo : Bio, Codable {
    let associateTopics: String?
    let associateUsers: String?
    let dislikeTimes: Int?
    let editTime: String?
    let editTimes: Int?
    let likeTimes: Int?
    let mediaFileUrls: [ScreenMediaFileUrl]?
    let postTime: String?
    let screenContent: String?
    let screenId: String?
    let isPublic: Int?
    let systemReviewResult: Int?
    let uid: String?
    let userInfo: UserInfo?
    
    var id: String {
        get{
            return "SCREEN_\(screenId ?? UUID().uuidString)"
        }
    }
    
    var bioUrl: String? = nil
    
    var name: String? {
        get{
            return "\(userInfo?.name ?? "")-\(screenContent ?? id)"
        }
    }
    
    enum CodingKeys: CodingKey {
        case associateTopics
        case associateUsers
        case dislikeTimes
        case editTime
        case editTimes
        case likeTimes
        case mediaFileUrls
        case postTime
        case screenContent
        case screenId
        case isPublic
        case systemReviewResult
        case uid
        case userInfo
    }
    
}


struct ScreenMediaFileUrl: Codable {
    
    let mediaFileUrl: String
    
    let mediaFileCompanionUrl: String?
    
    let mediaType: String
    
    let mediaDescription: String
    
    let x18Content: Int?
    
    
    static let MEDIA_TYPE_IMAGE = "image/*"
    static let MEDIA_TYPE_VIDEO = "video/*"
    
}
