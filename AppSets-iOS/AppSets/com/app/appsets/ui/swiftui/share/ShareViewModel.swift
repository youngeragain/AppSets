//
//  ShareViewModel.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//
import SwiftUI

@Observable
class ShareViewModel {
    private static let TAG = "ShareViewModel"

    public static var INSTANCE = ShareViewModel()

    var boxFocusInfo: BoxFocusInfo = BoxFocusInfo()

    var mShareDevice: ShareDevice = BasicShareDevice()

    var shareDeviceList: ObservableList<ShareDevice> = ObservableList()

    var pendingSendContentList: ObservableList<any DataContent> = ObservableList()
    
    var receivedContentList: ObservableList<any DataContent> = ObservableList()

    func updateShareDevice(_ shareDevice: ShareDevice) {
        mShareDevice = shareDevice
    }

    func updateShareDeviceList(_ shareDeviceList: [ShareDevice]) {
        _ = self.shareDeviceList.clear()
        self.shareDeviceList.addAll(shareDeviceList)
        PurpleLogger.current.d(ShareViewModel.TAG, "updateShareDeviceList, count\(self.shareDeviceList.count())")
    }

    func addShareDevice(_ shareDevice: ShareDevice) {
        _ = shareDeviceList.add(shareDevice)
    }

    func removeShareDevice(_ deviceName: DeviceName) {
        shareDeviceList.removeIf { shareDevice in
            shareDevice.deviceName.rawName == deviceName.rawName
        }
    }

    func updateBoxFocusInfo(_ boxFocusInfo: BoxFocusInfo) {
        self.boxFocusInfo = boxFocusInfo
    }

    func addPendingContent(_ content: Any) {
        if(content is String){
            let dataContent = StringDataContent(content: content as! String)
            _ = pendingSendContentList.add(dataContent)
        }
       
    }
    
    func removeAllPendingSendContent(){
        _ = pendingSendContentList.clear()
    }
    
    func onContentReceived(_ content: any DataContent){
        _ = receivedContentList.add(content)
    }
    
}
