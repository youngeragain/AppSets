//
//  HttpShareDevice.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//
import Foundation

class HttpShareDevice: ShareDevice {
    var discoveryEndpoint: DiscoveryEndpoint? = nil
    
    var isNeedPin: Bool = false
    
    var pin: String? = nil
    
    var token: String? = nil
    
    var isPaired: Bool {
        get{
            return token?.isEmpty == false
        }
    }

    override init(deviceName: DeviceName, deviceAddress: DevcieAddress, deviceType: Int) {
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
                                                                                                                                   
