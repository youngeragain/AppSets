//
//  ShareMethod.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/2.
//
import Foundation
import SwiftUI

@Observable
class ShareMethod: SwiftUILifecycle {
    public static let TAG_B = "ShareMethod"
    var deviceName: DeviceName = DeviceName.NONE
    
    let viewModel: ShareViewModel
    
    init(viewModel: ShareViewModel) {
        self.viewModel = viewModel
    }

    func initMethod() {
        
    }

    func onAppear() {
        initMethod()
    }

    func onDisappear() {
        destroy()
    }

    func onCreate() {
    }

    func onDestroy() {
        destroy()
    }

    func destroy() {
        
    }
    
    func updateDeviceName(){
        PurpleLogger.current.d(ShareMethod.TAG_B, "updateDeviceName, before name:\(deviceName.nickName)")
        deviceName = DeviceName.RANDOM
        PurpleLogger.current.d(ShareMethod.TAG_B, "updateDeviceName, after name:\(deviceName.nickName)")
    }
    
    func onShareDeviceClick(shareDevice:ShareDevice, clickType:Int){
        
    }
}
