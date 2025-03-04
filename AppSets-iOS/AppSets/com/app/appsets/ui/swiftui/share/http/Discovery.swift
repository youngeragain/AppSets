//
//  Discovery.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

import Network
import Foundation

class BonjourServicePublisher_NW {
    private static let TAG = "BonjourServicePublisher_NW"
    var listener: NWListener?

    func publishService_NW(port: UInt16, deviceName:DeviceName) {
        PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "publishService_NW")
        do {
            // 1. 创建 NWListener 对象
            let options = NWProtocolTCP.Options()
            let listener = try NWListener(using: .tcp, on: NWEndpoint.Port(rawValue: port)!)
            self.listener = listener
            
            var service = NWListener.Service(name:deviceName.nickName, type:"_http._tcp", domain: "local.")
            
            // 可选: 设置 TXT 记录
            var txtRecord: [String: String] = ["rawName":deviceName.rawName, "nickName":deviceName.nickName ?? ""] // 示例 TXT 记录
            service.txtRecordObject = NWTXTRecord(txtRecord)

            
            // 2. 配置 Bonjour 服务
            listener.service = service

            // 3. 监听连接 (开始接受客户端连接，这里仅发布服务，连接处理代码省略)
            listener.stateUpdateHandler = { state in
                switch state {
                case .setup:
                    PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "Bonjour setup!")
                    // setup
                case .waiting(let error):
                    PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "Bonjour waiting！error: \(error.debugDescription)")
                    // 服务发布成功，可以开始接受客户端连接
                case .ready:
                    PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "Bonjour ready \(listener.service?.domain ?? "unknown"), port: \(listener.port?.rawValue ?? 0)")
                    // 服务发布成功，可以开始接受客户端连接
                case .failed(let error):
                    PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "Bonjour failed error: \(error.debugDescription)")
                    self.listener = nil // 发布失败，清空 listener 引用
                    // 在这里处理发布失败的情况
                case .cancelled:
                    PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "Bonjour cancelled。")
                    self.listener = nil // 服务停止发布，清空 listener 引用
                default:
                    break
                }
            }

            listener.newConnectionHandler = { newConnection in
                // 处理新的客户端连接 (代码省略，本例只关注服务发布)
                newConnection.cancel() // 示例中直接拒绝连接，仅演示服务发布
            }


            // 4. 启动监听器 (开始发布服务)
            listener.start(queue: .main) // 在主队列上处理事件

        } catch {
            PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "create NWListener failed: \(error)")
            self.listener = nil // 创建失败，清空 listener 引用
            // 处理创建监听器失败的情况
        }
    }

    func stopService_NW() {
        listener?.cancel()
        listener = nil
    }
}

class BonjourServiceBrowser_NW {
    
    private static let TAG = "BonjourServiceBrowser_NW"
    // 自定义代理协议 (Network 框架版本)
    protocol BonjourServiceBrowserDelegate_NW: AnyObject {
        func bonjourBrowser_NW(_ browser: BonjourServiceBrowser_NW, didUpdateServices services: [NWBrowser.Result])
    }
    
    var browser: NWBrowser?
    var discoveredServices_NW: [NWBrowser.Result] = []
    var delegate_NW: BonjourServiceBrowserDelegate_NW? // 自定义代理 (Network 框架版本)


    func startBrowsing_NW() {
        PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "startBrowsing_NW")
        // 1. 创建 NWBrowser 对象
        let newBrowser = NWBrowser.init(for: .bonjourWithTXTRecord(type: "_http._tcp", domain: "local."), using: .tcp)
        self.browser = newBrowser
        discoveredServices_NW = [] // 清空已发现服务列表

        // 2. 开始浏览特定类型的 Bonjour 服务
        newBrowser.browseResultsChangedHandler =  { results, changes in
        
            changes.forEach{ change in
                switch change {
                case .added(let foundResult):
                    PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "added Bonjour service (Network): \(foundResult)")
                    self.discoveredServices_NW.append(foundResult) // 添加到已发现服务列表
                    self.delegate_NW?.bonjourBrowser_NW(self, didUpdateServices: self.discoveredServices_NW) // 通知代理
                case .removed(let removedResult):
                    PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "removed Bonjour service (Network): \(removedResult)")
                    self.discoveredServices_NW.removeAll { $0 == removedResult } // 从已发现服务列表中移除
                    self.delegate_NW?.bonjourBrowser_NW(self, didUpdateServices: self.discoveredServices_NW) // 通知代理
                case .changed(let old, let new, let flag):
                    PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "changed Bonjour service (Network): \(old) \(new) \(flag)")
                   
                default:
                    break
                }
            }
        }

        // 3. 启动浏览器 (开始浏览)
        newBrowser.start(queue: .global()) // 在主队列上处理浏览结果
    }

    func stopBrowsing_NW() {
        browser?.cancel()
        browser = nil
        discoveredServices_NW = []
    }
}

class Discovery {
    private static let TAG = "Discovery"
    func start(deviceName:DeviceName){
        PurpleLogger.current.d(Discovery.TAG, "start")
        let publisher = BonjourServicePublisher_NW()
        publisher.publishService_NW(port: 11100, deviceName:deviceName)
        let browser = BonjourServiceBrowser_NW()
        browser.startBrowsing_NW()
    }
}
