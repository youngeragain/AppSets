//
//  LocalDataProvider.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/5.
//

import Foundation

class LocalDataProvider {
    private static let TAG = "LocalDataProvider"

    static func clear() {
        PurpleLogger.current.d(LocalDataProvider.TAG, "clear")
        UserDefaults.standard.removeObject(forKey: LocalAccountManager.SPK_USER_INFO)
        UserDefaults.standard.removeObject(forKey: ApiDesignEncodeStr.tokenStrToMd5)
    }

    static func save<D: Codable>(_ key: String, any: D) -> Bool {
        PurpleLogger.current.d(LocalDataProvider.TAG, "save, key:\(key), any:\(any)")
        do {
            let encoder = JSONEncoder()
            let jsonData = try encoder.encode(any)
            if let jsonString = String(data: jsonData, encoding: .utf8) {
                UserDefaults.standard.set(jsonString, forKey: key)
            }
        } catch let error {
            PurpleLogger.current.d(LocalDataProvider.TAG, "save, key:\(key), any:\(any), encode failed \(error)")
            return false
        }
        return true
    }

    static func get<D: Codable>(_ key: String, componentType: D.Type = D.self) -> D? {
        PurpleLogger.current.d(LocalDataProvider.TAG, "get, key:\(key), compoentType:\(componentType)")
        let rawString = get(key)
        PurpleLogger.current.d(LocalDataProvider.TAG, "get, key:\(key), compoentType:\(componentType), rawString;\(String(describing: rawString))")
        var value: D? = nil
        if componentType is String.Type {
            value = rawString as? D
        } else if componentType is Int.Type {
            value = Int(rawString ?? "") as? D
        } else if componentType is Double.Type {
            value = Double(rawString ?? "") as? D
        } else if componentType is Float.Type {
            value = Float(rawString ?? "") as? D
        } else if componentType is Bool.Type {
            value = Bool(rawString ?? "false") as? D
        }
        if let data = rawString?.data(using: .utf8) {
            do {
                let decoder = JSONDecoder()
                value = try decoder.decode(componentType, from: data)
            } catch let error {
                PurpleLogger.current.d(LocalDataProvider.TAG, "get, key:\(key), compoentType:\(componentType), decode from json failed, \(error)")
            }
        }
        PurpleLogger.current.d(LocalDataProvider.TAG, "get, key:\(key), compoentType:\(componentType), value:\(String(describing: value))")
        return value
    }

    static func get(_ key: String) -> String? {
        PurpleLogger.current.d(LocalDataProvider.TAG, "get, key:\(key)")
        return UserDefaults.standard.string(forKey: key)
    }
}
