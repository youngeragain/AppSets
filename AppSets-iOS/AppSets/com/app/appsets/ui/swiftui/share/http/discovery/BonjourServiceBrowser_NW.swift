//
//  BonjourServiceBrowser_NW.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//

import Foundation
import Network

class BonjourServiceBrowser_NW {
    private static let TAG = "BonjourServiceBrowser_NW"
    // 自定义代理协议 (Network 框架版本)
    protocol BonjourServiceBrowserDelegate_NW: AnyObject {
        func bonjourBrowser_NW(_ browser: BonjourServiceBrowser_NW, didUpdateServices services: [NWBrowser.Result])
    }

    private var browser: NWBrowser?
    private var discoveredServices_NW: [NWBrowser.Result] = []
    private var delegate_NW: BonjourServiceBrowserDelegate_NW? // 自定义代理 (Network 框架版本)

    func startBrowsing() {
        browser?.cancel()
        PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "startBrowsing")
        // 1. 创建 NWBrowser 对象
        let newBrowser = NWBrowser(for: .bonjourWithTXTRecord(type: "_http._tcp", domain: "local."), using: .tcp)
        browser = newBrowser
        discoveredServices_NW = [] // 清空已发现服务列表

        // 2. 开始浏览特定类型的 Bonjour 服务
        newBrowser.browseResultsChangedHandler = { _, changes in

            changes.forEach { change in
                switch change {
                case let .added(foundResult):
                    PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "added Bonjour service (Network): \(foundResult)")
                    self.discoveredServices_NW.append(foundResult) // 添加到已发现服务列表
                    self.delegate_NW?.bonjourBrowser_NW(self, didUpdateServices: self.discoveredServices_NW) // 通知代理
                case let .removed(removedResult):
                    PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "removed Bonjour service (Network): \(removedResult)")
                    self.discoveredServices_NW.removeAll { $0 == removedResult } // 从已发现服务列表中移除
                    self.delegate_NW?.bonjourBrowser_NW(self, didUpdateServices: self.discoveredServices_NW) // 通知代理
                case let .changed(old, new, flag):
                    PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "changed Bonjour service (Network): \(old) \(new) \(flag)")

                default:
                    break
                }
            }
        }

        // 3. 启动浏览器 (开始浏览)
        newBrowser.start(queue: .global()) // 在主队列上处理浏览结果
    }

    func stopBrowsing() {
        PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "stopBrowsing")
        browser?.cancel()
        browser = nil
        discoveredServices_NW = []
    }
}
