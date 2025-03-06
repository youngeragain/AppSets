//
//  ShareViewModel.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//
import SwiftUI

class ShareViewModel: ObservableObject {
    
    public static var INSTANCE = ShareViewModel()
    
    @Published var mShareDevice: ShareDevice = BasicShareDevice()
    @Published var shareDeviceList: [ShareDevice] = []

    func updateShareDevice(_ shareDevice: ShareDevice) {
        mShareDevice = shareDevice
    }

    func updateShareDeviceList(_ shareDeviceList: [ShareDevice]) {
        shareDeviceList.forEach { shareDevice in
            self.shareDeviceList.append(shareDevice)
        }
    }

    func addShareDevice(_ shareDevice: ShareDevice) {
        shareDeviceList.append(shareDevice)
    }

    func removeShareDevice(_ deviceName: DeviceName) {
        shareDeviceList.removeAll { shareDevice in
            shareDevice.deviceName.rawName == deviceName.rawName
        }
    }
}