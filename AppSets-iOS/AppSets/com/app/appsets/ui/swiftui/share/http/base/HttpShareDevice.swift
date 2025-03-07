//
//  HttpShareDevice.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//
import Foundation

class HttpShareDevice: ShareDevice {
    var discoveryEndpoint: DiscoveryEndpoint?

    override init(deviceName: DeviceName, deviceAddress: DevcieAddress, deviceType: Int) {
        super.init(deviceName: deviceName, deviceAddress: deviceAddress, deviceType: deviceType)
    }

    init(deviceName: DeviceName, deviceAddress: DevcieAddress, deviceType: Int, discoeryEndpoint: DiscoveryEndpoint? = nil) {
        super.init(deviceName: deviceName, deviceAddress: deviceAddress, deviceType: deviceType)
        self.discoveryEndpoint = discoeryEndpoint
    }

    required init(from decoder: any Decoder) throws {
        let container = try decoder.container(keyedBy: ShareDevice.CodingKeys.self)
        let deviceName = try container.decode(DeviceName.self, forKey: ShareDevice.CodingKeys.deviceName)
        let deviceAddress = try container.decode(DevcieAddress.self, forKey: ShareDevice.CodingKeys.deviceAddress)
        let deviceType = try container.decode(Int.self, forKey: ShareDevice.CodingKeys.deviceType)
        super.init(deviceName: deviceName, deviceAddress: deviceAddress, deviceType: deviceType)
    }

}
