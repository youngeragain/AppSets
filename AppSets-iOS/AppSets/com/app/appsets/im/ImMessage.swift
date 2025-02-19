//
//  ImMessage.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct ImMessageConstant {
    public static let TYPE_O2O = "one2one"
    public static let TYPE_O2M = "one2many"
    
    public static let KEY_IM_NOTIFICATION_ID = "im_notification_id"
    public static let KEY_IM_MESSAGE_ID = "im_message_id"

    public static let HEADER_MESSAGE_MESSAGE_DELIVERY_TYPE = "a1"
    public static let HEADER_MESSAGE_ID = "a2"
    public static let HEADER_MESSAGE_UID = "a3"
    public static let HEADER_MESSAGE_NAME = "a4"
    public static let HEADER_MESSAGE_NAME_BASE64 = "a5"
    public static let HEADER_MESSAGE_AVATAR_URL = "a6"
    public static let HEADER_MESSAGE_ROLES = "a7"
    public static let HEADER_MESSAGE_MESSAGE_GROUP_TAG = "a8"
    public static let HEADER_MESSAGE_TO_ID = "a9"
    public static let HEADER_MESSAGE_TO_NAME = "a10"
    public static let HEADER_MESSAGE_TO_NAME_BASE64 = "a11"
    public static let HEADER_MESSAGE_TO_TYPE = "a12"
    public static let HEADER_MESSAGE_TO_ICON_URL = "a13"
    public static let HEADER_MESSAGE_TO_ROLES = "a14"

    public static var messageDateFormatTemplate: String = "MM/dd HH:mm"
}

protocol MessageMetadata<D> : Equatable, Codable{
    associatedtype D
    
    var description:String { get set }
    var size:Int { get set }
    var compressed:Bool { get set }
    var encode:String { get set }
    var contentType:String { get set }
    var data:D { get set }
}

struct StringMessageMetadata: MessageMetadata{
   
    typealias D = String
    
    var description: String
    
    var size: Int
    
    var compressed: Bool
    
    var encode: String
    
    var data: String
    
    var contentType: String
}


protocol ImMessage: Equatable, Identifiable {
    
    var id:String { get set }
    
    var timestamp:Date { get set }
    
    var fromInfo:MessageFromInfo { get set }
    
    var toInfo:MessageToInfo { get set }
    
    var messageGroupTag:String? { get set }
    
    var metadata: any MessageMetadata { get set }
    
    var messageType:String  { get set }
    
    var isSent: Bool { get set }
    
    var readableDate: String? { get set}
    
}

struct ImMessageStatic{
    public static func readableContent(_ imMessage:(any ImMessage)?) -> String? {
        if(imMessage==nil){
            return nil
        }
        if(imMessage is IM_Text){
            if(imMessage?.metadata is StringMessageMetadata){
                return (imMessage?.metadata as! StringMessageMetadata).data
            }
        }
        return nil
    }
}

extension ImMessage {
    
    func parseFromImObj() -> (any ImObj)? {
        PurpleLogger.current.d("ImMessageEXT", "parseFromImObj, messageToType:\(toInfo.toType)")
        if toInfo.toType == ImMessageConstant.TYPE_O2M {
            return ImGroup(toInfo)
        }
        if toInfo.toType == ImMessageConstant.TYPE_O2O {
            return ImSingle(fromInfo, userRoles: fromInfo.roles)
        }
        return nil
    }
    
    func parseToImObj() -> (any ImObj)? {
        PurpleLogger.current.d("ImMessageEXT", "parseFromImObj, parseToImObj:\(toInfo.toType)")
        if toInfo.toType == ImMessageConstant.TYPE_O2M {
            return ImGroup(toInfo)
        }
        if toInfo.toType == ImMessageConstant.TYPE_O2O {
            return ImSingle(toInfo, userRoles: toInfo.roles)
        }
        return nil
    }
}

struct IM_Text : ImMessage {
    
    static func == (lhs: IM_Text, rhs: IM_Text) -> Bool {
        return lhs.id == rhs.id &&
        lhs.toInfo == rhs.toInfo
    }
    
    var id: String
    var metadata: any MessageMetadata
    var toInfo: MessageToInfo
    var timestamp: Date
    var fromInfo: MessageFromInfo
    var messageGroupTag: String? = nil
    var messageType: String = ImMessageDesignType.TYPE_TEXT
    
    var isSent: Bool = false
    
    var readableDate: String? = nil
    
    init(id: String, timestamp: Date, fromInfo: MessageFromInfo, toInfo: MessageToInfo, messageGroupTag: String? = nil, metadata: any MessageMetadata) {
        self.id = id
        self.metadata = metadata
        self.toInfo = toInfo
        self.timestamp = timestamp
        self.fromInfo = fromInfo
        self.messageGroupTag = messageGroupTag
    }
    
}

protocol SystemContentInterface {
    
}


struct RequestFeedbackJson: SystemContentInterface {
    let requestId: String
    let isAccept: Bool
}


struct SystemContentJson {
    
    let type: String
    
    let content: String?
    
    var systemContentInterface: SystemContentInterface?
    
}


struct IM_System : ImMessage {
    
    static func == (lhs: IM_System, rhs: IM_System) -> Bool {
        return lhs.id == rhs.id &&
        lhs.toInfo == rhs.toInfo
    }
    
    var id: String
    
    var metadata: any MessageMetadata
    
    var fromInfo: MessageFromInfo
    
    var toInfo: MessageToInfo
    
    var timestamp: Date
    
    var messageGroupTag: String?
    
    var messageType: String
    
    var isSent: Bool
    
    var readableDate: String?
    
    var systemContentJson: SystemContentJson? = nil
    
    
}
