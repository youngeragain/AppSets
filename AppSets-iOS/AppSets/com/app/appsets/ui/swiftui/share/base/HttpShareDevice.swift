//
//  HttpShareDevice.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct HttpShareDevice: ShareDevice {
    var deviceTyp: Int
    
    var deviceName: DeviceName
    
    var deviceAddress: DevcieAddress
    
    enum CodingKeys: CodingKey {
        case deviceTyp
        case deviceName
        case deviceAddress
    }
    
}
