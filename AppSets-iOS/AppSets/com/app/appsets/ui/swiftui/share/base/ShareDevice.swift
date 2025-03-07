//
//  ShareDevice.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

import Foundation

class ShareDevice: Codable, Identifiable {
    
    public static let DEVICE_TYPE_PHONE = 0
    public static let DEVICE_TYPE_TABLET = 1
    public static let DEVICE_TYPE_COMPUTER = 2
    public static let DEVICE_TYPE_TV = 3
    public static let DEVICE_TYPE_WEB_DEVICE = 4
    public static let DEVICE_TYPE = "deviceType"
    public static let RAW_NAME = "rawName"
    public static let NICK_NAME = "nickName"
    
    var id:String = UUID().uuidString
   
    var deviceName: DeviceName

    var deviceAddress: DevcieAddress
    
    var deviceType: Int
    
    init(deviceName: DeviceName, deviceAddress: DevcieAddress, deviceType: Int) {
        self.deviceName = deviceName
        self.deviceAddress = deviceAddress
        self.deviceType = deviceType
    }
    
    enum CodingKeys: CodingKey {
        case deviceName
        case deviceAddress
        case deviceType
    }
}
