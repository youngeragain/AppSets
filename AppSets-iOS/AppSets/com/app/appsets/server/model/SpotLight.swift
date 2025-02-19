//
//  SpotLight.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

struct SpotLight : Codable {
    
    let holiday: Holiday?
    
    let popularSearches: PopularSearches?
    
    let todayInHistoryList: [TodayInHistory]?
    
    let wordOfTheDayList: [WordOfTheDay]?
    
    let baiduHotData: BaiduHotData?
    
    let microsoftBingWallpaperList: [MicrosoftBingWallpaper]?
    
}

struct Holiday :Codable {
    
    let infoUrl: String?
    
    let moreUrl: String?
    
    let name: String?
    
    let picUrl: String?
    
}

struct PopularSearches: Codable {
    
    let keywords: [String]?
    
    let url: String?
    
}

struct TodayInHistory : Codable {
    
    let date: String?
    
    let event: String?
    
    let infoUrl: String?
    
    let picUrl: String?
    
    let title: String?
    
}

struct WordOfTheDay : Codable {
    
    let author: String?
    
    let authorInfo: String?
    
    let infoUrl: String?
    
    let word: String?
    
    let picUrl: String?
    
}


struct Hotsearch : Codable {
    
    let cardTitle: String?
    
    let heatScore: String?
    
    let hotTags: String?
    
    let index: String?
    
    let isNew: String?
    
    let isViewed: String?
    
    let linkurl: String?
    
    let preTag: String?
    
    let views: String?
    
}

struct BaiduHotData : Codable {
    
    let hotsearch : [Hotsearch]?
    
}

struct BingWallpaperImage : Codable {
    let bot: Int?
    let copyright: String?
    let copyrightlink: String?
    let drk: Int?
    let enddate: String?
    let fullstartdate: String?
    let hs: [String]? = nil
    let hsh: String?
    let quiz: String?
    let startdate: String?
    let title: String?
    let top: Int?
    let url: String?
    let urlbase: String?
    let wp: Bool?
    
    enum CodingKeys: CodingKey {
        case bot
        case copyright
        case copyrightlink
        case drk
        case enddate
        case fullstartdate
        case hsh
        case quiz
        case startdate
        case title
        case top
        case url
        case urlbase
        case wp
    }
    
}

struct Tooltips: Codable {
    let loading: String?
    let next: String?
    let previous: String?
    let walle: String?
    let walls: String?
}

struct MicrosoftBingWallpaper : Codable {
    
    let images: [BingWallpaperImage]?
    
    let tooltips: Tooltips?
    
    var url: String
    {
        get{
            if images == nil || images!.isEmpty {
                return ""
            }
            let image = images![0]
            let urlSuffix = image.url?.replacing("/", with:"") ?? ""
            
            return "https://www.bing.com/\(urlSuffix)"
        }
        }
    var whereText: String {
        get{
            if images == nil || images!.isEmpty {
                return ""
            }
            let image = images![0]
           
            return image.copyright ?? "一个美好的地方"
        }
    }
    var whereBlowText: String {
        get{
            if images == nil || images!.isEmpty {
                return ""
            }
            let image = images![0]
            return image.title ?? "每日一题"
        }
    }
    
    enum CodingKeys: CodingKey {
        case images
        case tooltips
    }
    
}
