//
//  StringEXT.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

extension String {
    
    static let TAG = "StringEXT"
    
    static func isNullOrEmpty(_ str:String?) -> Bool {
        return str == nil || str!.isEmpty
    }
    
    static func unWrappeBase64(_ base64String: String) -> String? {
        guard let decodedData = Foundation.Data(base64Encoded: base64String, options: []) else {
            PurpleLogger.current.d(String.TAG, "unWrappeBase64, 无效的 Base64 字符串")
            return nil
        }
        guard let decodedString = String(data: decodedData, encoding: .utf8) else {
            PurpleLogger.current.d(String.TAG, "unWrappeBase64, 无法使用 UTF-8 解码数据")
            return nil
        }
        PurpleLogger.current.d(String.TAG, "unWrappeBase64, decodedString:\(decodedString)")
        return decodedString
    }
    
    static func decodeJSONFromString(jsonString: String) -> Any? {
        guard let jsonData = jsonString.data(using: .utf8) else {
            PurpleLogger.current.d(String.TAG, "decodeJSONFromString, 无法将字符串转换为 Data")
          
            return nil
        }

        do {
            let jsonObject = try JSONSerialization.jsonObject(with: jsonData, options: [])
            return jsonObject
        } catch {
            PurpleLogger.current.d(String.TAG, "decodeJSONFromString, SON 解析失败: \(error)")
        
            return nil
        }
    }
    
}
