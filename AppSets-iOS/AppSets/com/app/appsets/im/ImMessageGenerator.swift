//
//  ImMessageGenerator.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation
import RMQClient.RMQMessage


class ImMessageGenerator {
    
    private static let TAG = "ImMessageGenerator"
    
    let decoder = JSONDecoder()
    let encoder = JSONEncoder()
    
    public static let Instance = ImMessageGenerator()
    
    private init() {
        
    }
    
    func generateBySend(
        context: Context,
        session: Session,
        inputSelector: InputSelector,
        content: Any
    ) -> any ImMessage {
        PurpleLogger.current.d(ImMessageGenerator.TAG, "generateBySend")
        let messageId = UUID().uuidString
        let timestamp = Date()
        let fromUserInfo = LocalAccountManager.Instance.userInfo
        let messageFromInfo = MessageFromInfo(
            uid: fromUserInfo.uid,
            name: fromUserInfo.name,
            avatarUrl: fromUserInfo.avatarUrl
        )
        
        let messageToInfo = MessageToInfo.fromImObj(session.imObj)
        
    
        var messageContent = ""
        if content is String {
            messageContent = content as! String
        }
        var metadata: StringMessageMetadata = StringMessageMetadata(
            description: "", size: 0, compressed: false, encode: "none", data: messageContent, contentType: ContentType.APPLICATION_TEXT
        )
        
        let imMessage = IM_Text(
            id: messageId,
            timestamp: timestamp,
            fromInfo: messageFromInfo,
            toInfo: messageToInfo,
            messageGroupTag: nil,
            metadata: metadata
        )
        return imMessage
    }
    
    func generateByReceived(
        _ message: RMQMessage
    ) -> (any ImMessage)? {
        let readableContent = String(data: message.body, encoding: .utf8) ?? ""
        PurpleLogger.current.d(
            ImMessageGenerator.TAG,
            "generateByReceived, message:\(readableContent)"
        )
        
        let headers = message.headers()
        
        let imMessageType = message.messageType()
        
        let imMessageTimestamp = message.timestamp()
        
        guard let fromUid = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_UID
        }) else {
            return nil
        }
        
        guard let messageId = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_ID
        }) else {
            return nil
        }
        
        guard let toType = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_TO_TYPE
        }) else {
            return nil
        }
        let fromName = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_NAME
        })
        
        let fromNameBase64 = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_NAME_BASE64
        })
        
        let fromAvatarUrl = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_AVATAR_URL
        })
        
        let fromRoles = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_ROLES
        })

        let groupMessageTag = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_MESSAGE_GROUP_TAG
        })
        
        let toId = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_TO_ID
        })
        
        let toName = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_TO_NAME
        })
        
        let toNameBase64 = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_TO_NAME_BASE64
        })
        
        let toIconUrl = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_TO_ICON_URL
        })
        
        let toRoles = headers?.first(where: { (key: String, value: NSObject) in
            key == ImMessageConstant.HEADER_MESSAGE_TO_ROLES
        })
        
        let messageFromInfo = MessageFromInfo(
            uid: getDicStringValue(fromUid),
            name: getDicStringValueUnWrappeBase64(fromNameBase64),
            avatarUrl: getDicStringValue(fromAvatarUrl),
            roles: getDicStringValue(fromRoles)
        )
        
        let messageToInfo = MessageToInfo(
            toType: getDicStringValue(toType),
            id: getDicStringValue(toId),
            name: getDicStringValueUnWrappeBase64(toNameBase64),
            iconUrl: getDicStringValue(toIconUrl),
            roles: getDicStringValue(toRoles)
        )
        
        let timestamp = imMessageTimestamp ?? Date()
        
        let metadataJsonString = String(data: message.body, encoding: .utf8) ?? ""
        
        let metadataJsonStringData = metadataJsonString.data(using: .utf8)!
        let metadata = try! decoder.decode(StringMessageMetadata.self, from: metadataJsonStringData)
        
        let imMessage = IM_Text(
            id: getDicStringValue(messageId),
            timestamp: timestamp,
            fromInfo: messageFromInfo,
            toInfo: messageToInfo,
            messageGroupTag: getDicStringValue(groupMessageTag),
            metadata: metadata
        )
        
        return imMessage
    }
    
    func generateByLocalDB(
    ) -> (any ImMessage)? {
        PurpleLogger.current.d(ImMessageGenerator.TAG, "generateByLocalDB")
        return nil
    }
    
    private func getDicStringValueUnWrappeBase64(_ element: Dictionary<String, NSObject>.Element?) -> String {
        let base64String = getDicStringValue(element)
        let decodedString = String.unWrappeBase64(base64String) ?? base64String
        return decodedString
    }
    
    private func getDicStringValue(_ element: Dictionary<String, NSObject>.Element?) -> String {
        PurpleLogger.current.d(ImMessageGenerator.TAG, "getDicStringValue, key:\(String(describing: element?.key))")
        if element == nil {
            return ""
        }
        return getNSObjStringValue(element!.value)
    }
    
    private func getNSObjStringValue(_ obj: NSObject) -> String {
        PurpleLogger.current.d(ImMessageGenerator.TAG, "getNSObjStringValue, obj:\(obj)")
        if obj is RMQLongstr {
            let valueLongString = (obj as! RMQLongstr)
            return valueLongString.stringValue
        }
        if obj is RMQVoid {
            return ""
        }
        if obj is NSString {
            return (obj as! NSString).description
        }
        if obj is Foundation.Data {
            return String(data:(obj as! Foundation.Data), encoding: .utf8) ?? ""
        }
        
        do{
            let value = try (obj.value(forKey: "stringValue") as? String) ?? ""
            return value
        }catch let error {
            return ""
        }
    }
    
    func makeMessageMetadataAsJsonStringData(message: any ImMessage)-> Data? {
        PurpleLogger.current.d(ImMessageGenerator.TAG, "makeMessageMetadataAsJsonString, for messageId:\(message.id)")
        return try? encoder.encode(message.metadata)
    }
    
}
