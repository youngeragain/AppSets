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

    var discoeryEndpoint: DiscoveryEndpoint? = nil

    enum CodingKeys: CodingKey {
        case deviceTyp
        case deviceName
        case deviceAddress
    }
}
