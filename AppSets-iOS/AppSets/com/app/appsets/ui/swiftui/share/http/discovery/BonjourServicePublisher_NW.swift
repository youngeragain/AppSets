//
//  BonjourServicePublisher_NW.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//

import Foundation
import Network

class BonjourServicePublisher_NW {
    private static let TAG = "BonjourServicePublisher_NW"
    private var listener: NWListener?

    func publishService(port: UInt16, deviceName: DeviceName) {
        PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "publishService, deviceName:\(String(describing: deviceName.nickName))")
        do {
            // 1. 创建 NWListener 对象
            let options = NWProtocolTCP.Options()
            let listener = try NWListener(using: .tcp, on: NWEndpoint.Port(rawValue: port)!)
            self.listener = listener

            var service = NWListener.Service(name: deviceName.nickName, type: "_http._tcp", domain: "local.")

            // 可选: 设置 TXT 记录
            var txtRecord: [String: String] = [
                ShareDevice.RAW_NAME: deviceName.rawName,
                ShareDevice.NICK_NAME: deviceName.nickName ?? "",
                ShareDevice.DEVICE_TYPE: ShareDevice.DEVICE_TYPE_PHONE.description
                
            ] // 示例 TXT 记录
            service.txtRecordObject = NWTXTRecord(txtRecord)

            // 2. 配置 Bonjour 服务
            listener.service = service

            // 3. 监听连接 (开始接受客户端连接，这里仅发布服务，连接处理代码省略)
            listener.stateUpdateHandler = { state in
                switch state {
                case .setup:
                    PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "Bonjour setup!")
                // setup
                case let .waiting(error):
                    PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "Bonjour waiting！error: \(error.debugDescription)")
                // 服务发布成功，可以开始接受客户端连接
                case .ready:
                    PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "Bonjour ready \(listener.service?.domain ?? "unknown"), port: \(listener.port?.rawValue ?? 0)")
                // 服务发布成功，可以开始接受客户端连接
                case let .failed(error):
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
            listener = nil // 创建失败，清空 listener 引用
            // 处理创建监听器失败的情况
        }
    }

    func stopService() {
        PurpleLogger.current.d(BonjourServicePublisher_NW.TAG, "stopService")
        listener?.cancel()
        listener = nil
    }
}
