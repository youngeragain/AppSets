//
//  BasicShareDevice.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//

import Foundation

class BasicShareDevice: ShareDevice {
    override init(deviceName: DeviceName = DeviceName.NONE, deviceAddress: DevcieAddress = DevcieAddress.NONE, deviceType: Int = ShareDevice.DEVICE_TYPE_PHONE) {
        super.init(deviceName: deviceName, deviceAddress: deviceAddress, deviceType: deviceType)
    }

    required init(from decoder: any Decoder) throws {
        let container = try decoder.container(keyedBy: ShareDevice.CodingKeys.self)
        let deviceName = try container.decode(DeviceName.self, forKey: ShareDevice.CodingKeys.deviceName)
        let deviceAddress = try container.decode(DevcieAddress.self, forKey: ShareDevice.CodingKeys.deviceAddress)
        let deviceType = try container.decode(Int.self, forKey: ShareDevice.CodingKeys.deviceType)

        super.init(deviceName: deviceName, deviceAddress: deviceAddress, deviceType: deviceType)
    }
}
