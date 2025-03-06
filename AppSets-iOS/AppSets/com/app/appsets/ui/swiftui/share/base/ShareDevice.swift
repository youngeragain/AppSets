//
//  ShareDevice.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct ShareDeviceStatic {
    public static let DEVICE_TYPE_PHONE = 0
    public static let DEVICE_TYPE_TABLET = 1
    public static let DEVICE_TYPE_COMPUTER = 2
    public static let DEVICE_TYPE_TV = 3
    public static let DEVICE_TYPE_WEB_DEVICE = 4
}

protocol ShareDevice: Codable {
    var deviceTyp: Int {
        get
        set
    }
    var deviceName: DeviceName {
        get
        set
    }

    var deviceAddress: DevcieAddress {
        get
        set
    }
}
