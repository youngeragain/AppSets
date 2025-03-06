//
//  BasicShareDevice.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//

struct BasicShareDevice: ShareDevice {
    var deviceTyp: Int = ShareDeviceStatic.DEVICE_TYPE_PHONE

    var deviceName: DeviceName = DeviceName.NONE

    var deviceAddress: DevcieAddress = DevcieAddress.NONE
}
