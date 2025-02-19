//
//  DeviceInfoProvider.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct DeviceInfoProvider {
    
    static func provideInfo() -> [String: String] {
        let info:[String: String] = [
            "platform": "ios",
            "screenResolution": "2210*1920",
            "screenSize": "5.0",
            "version": "17",
            "vendor": "Apple",
            "model": "iPhone 16",
            "modelCode": "phone16",
            "ip": "0.0.0.0"
        ]
        return info
    }
    
}
