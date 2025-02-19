//
//  Application.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class Application : ImSessionHolder, Bio, Codable {
    
    public static let BIO_ID_PREFIX = "APP-"
    var appId:String? = nil
    var iconUrl:String? = nil
    var website:String? = nil
    var updateTime:String? = nil
    var createTime:String? = nil
    var developerInfo:String? = nil
    var price:String? = nil
    var priceUnit:String? = nil
    var bannerUrl:String? = nil
    var createUid:String? = nil
    var updateUid:String? = nil
    var name:String? = nil
    var category:String? = nil
    var platforms:[Platform]? = nil
    
    var id: String {
        get{
            return "\(Application.BIO_ID_PREFIX)\(appId ?? UUID().uuidString)"
        }
    }
    
    var bioUrl: String? = nil
    
    var session: Session? = nil
    
    enum CodingKeys: CodingKey {
        case appId
        case iconUrl
        case website
        case updateTime
        case createTime
        case developerInfo
        case price
        case priceUnit
        case bannerUrl
        case createUid
        case updateUid
        case name
        case category
        case platforms
    }
    
}

struct Platform: Codable {
    var id:String? = nil
    var name:String? = nil
    var packageName:String? = nil
    var introduction:String? = nil
    var versionInfos:[VersionInfo]? = nil
}

struct VersionInfo: Codable {
    var id:String? = nil
    var versionIconUrl:String? = nil
    var versionBannerUrl:String? = nil
    var version:String? = nil
    var versionCode:String? = nil
    var changes:String? = nil
    var packageSize:String? = nil
    var privacyUrl:String? = nil
    var screenshotInfos:[ScreenshotInfo]? = nil
    var downloadInfo:[DownloadInfo]? = nil
}

struct ScreenshotInfo: Codable {
    var id:String? = nil
    var createUid:String? = nil
    var updateTime:String? = nil
    var createTime:String? = nil
    var type:String? = nil
    var contentType:String? = nil
    var url:String? = nil
}

struct DownloadInfo: Codable {
    var id:String? = nil
    var createUid:String? = nil
    var updateUid:String? = nil
    var createTime:String? = nil
    var updateTime:String? = nil
    var downloadTimes:String? = nil
    var url:String? = nil
}
