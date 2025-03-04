//
//  DeviceName.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct DeviceName: Codable {
    let rawName: String
    let nickName: String?

    public static let NONE = DeviceName(rawName: "", nickName: nil)

    public static var RANDOM: DeviceName {
        let rawName = VendorUtil.getVendorDeviceName()
        let nickName = NameGenerator.randomNikeName()
        return DeviceName(rawName: rawName, nickName: nickName)
    }
}
