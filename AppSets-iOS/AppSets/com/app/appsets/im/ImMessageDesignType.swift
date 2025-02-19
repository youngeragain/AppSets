//
//  MQMessageDesignType.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/14.
//

import Foundation

struct ImMessageDesignType {
    public static let TYPE_TEXT = "im.text"
    public static let TYPE_IMAGE = "im.image"
    public static let TYPE_VIDEO = "im.video"
    public static let TYPE_MUSIC = "im.music"
    public static let TYPE_VOICE = "im.voice"
    public static let TYPE_LOCATION = "im.location"
    public static let TYPE_FILE = "im.file"
    public static let TYPE_HTML = "im.html"
    public static let TYPE_AD = "im.ad"
    public static let TYPE_SYSTEM = "im.system"
    public static let TYPE_CUSTOM = "im.custom.*"
    
    public static func getType(_ imMessage: any ImMessage) -> String {
        switch imMessage{
        case is IM_Text:
            return TYPE_TEXT
            
        case is IM_System:
            return TYPE_SYSTEM
        default:
            return TYPE_TEXT
        }
    }
}
