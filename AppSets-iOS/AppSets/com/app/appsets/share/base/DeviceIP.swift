//
//  DeviceIP.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct DeviceIP: Codable {
    public static let IP_4 = 4
    public static let IP_6 = 6

    let ip: String

    var type: Int = IP_4
}
