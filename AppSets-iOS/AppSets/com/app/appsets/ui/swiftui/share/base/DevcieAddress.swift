//
//  DevcieAddress.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct DevcieAddress: Codable {
    var ips: [DeviceIP] = []
    
    public static let NONE = DevcieAddress()
}
