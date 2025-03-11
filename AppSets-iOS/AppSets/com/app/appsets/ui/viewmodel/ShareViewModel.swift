//
//  ShareViewModel.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//
import Foundation
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

    var deviceContentListMap: [String: ObservableObject<ContentInfoList>] = [:]

    func updateShareDevice(_ shareDevice: ShareDevice) {
        mShareDevice = shareDevice
    }

    func updateShareDeviceList(_ shareDeviceList: [ShareDevice]) {
        _ = self.shareDeviceList.clear()
        self.shareDeviceList.addAll(shareDeviceList)
        PurpleLogger.current.d(ShareViewModel.TAG, "updateShareDeviceList, count\(self.shareDeviceList.count())")
    }
    
    func addShareDevice(_ shareDevice: ShareDevice) {
        _ = self.shareDeviceList.add(shareDevice)
        PurpleLogger.current.d(ShareViewModel.TAG, "addShareDevice, count\(self.shareDeviceList.count())")
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
        if content is String {
            PurpleLogger.current.d(ShareViewModel.TAG, "addPendingContent, add String:\(content)")
            let str = content as! String
            let dataContent = StringDataContent(content: str)
            _ = pendingSendContentList.add(dataContent)
        } else if content is URL {
            PurpleLogger.current.d(ShareViewModel.TAG, "addPendingContent, add URL:\(content)")
            let url = content as! URL
            let dataContent = UriDataContent(uri: url)
            _ = pendingSendContentList.add(dataContent)
        }
    }

    func removeAllPendingSendContent() {
        _ = pendingSendContentList.clear()
    }

    func onContentReceived(_ content: any DataContent) {
        _ = receivedContentList.add(content)
    }

    func getPendingSendContentList() -> [any DataContent] {
        return pendingSendContentList.elements
    }

    func updateDeviceContentList(shareDevice: HttpShareDevice, contentInfoList: ContentInfoList) {
        deviceContentListMap[shareDevice.id] = ObservableObject(contentInfoList)
    }
}
