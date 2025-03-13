//
//  BonjourServiceBrowser_NW.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//

import Foundation
import Network

struct BrowserResult {
    let result: NWBrowser.Result
    let ipAddress: IPAddress
}

class BonjourServiceBrowser_NW {
    private static let TAG = "BonjourServiceBrowser_NW"
    // 自定义代理协议 (Network 框架版本)
    protocol BonjourServiceListener {
        func onServicesChanged(_ browserResult: BrowserResult)
    }

    private var browser: NWBrowser?
    private var discoveredServices: [NWBrowser.Result] = []
    private var servicesListener: BonjourServiceListener? // 自定义代理 (Network 框架版本)

    func setServicesListener(_ listener: BonjourServiceListener?) {
        servicesListener = listener
    }

    func startBrowsing() {
        browser?.cancel()
        PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "startBrowsing")
        // 1. 创建 NWBrowser 对象
        let newBrowser = NWBrowser(for: .bonjourWithTXTRecord(type: "_http._tcp", domain: "local."), using: .tcp)
        browser = newBrowser
        discoveredServices = [] // 清空已发现服务列表

        // 2. 开始浏览特定类型的 Bonjour 服务
        newBrowser.browseResultsChangedHandler = { results, changes in
            self.resolveServiceEndpoint(browser: newBrowser, results: results, changes: changes)
        }

        // 3. 启动浏览器 (开始浏览)
        newBrowser.start(queue: .global()) // 在主队列上处理浏览结果
    }

    func resolveServiceEndpoint(browser: NWBrowser, results: Set<NWBrowser.Result>, changes: Set<NWBrowser.Result.Change>) {
        for result in results {
            resolveServiceEndpointSingle(browser: browser, result: result)
        }
        changes.forEach { change in
            switch change {
            case let .added(result):
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "added Bonjour service (Network): \(result)")
                resolveServiceEndpointSingle(browser: browser, result: result)
            case let .removed(result):
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "removed Bonjour service (Network): \(result)")
            case let .changed(old, new, flag):
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "changed Bonjour service (Network): \(old) \(new) \(flag)")

            default:
                break
            }
        }
    }

    func resolveServiceEndpointSingle(browser: NWBrowser, result: NWBrowser.Result) {
        let connection = NWConnection(to: result.endpoint, using: .udp)
        connection.stateUpdateHandler = { newState in
            switch newState {
            case .ready:
                if let path = connection.currentPath {
                    if case let .hostPort(host: host, port: port) = path.remoteEndpoint {
                        PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle, Resolved host: \(host), port:\(port), Resolved for \(result.endpoint.debugDescription)")
                        if case let .ipv4(iPv4Address) = host {
                            let browserResult = BrowserResult(result: result, ipAddress: iPv4Address)
                            self.servicesListener?.onServicesChanged(browserResult)
                        } else if case let .ipv6(iPv6Address) = host {
                            let browserResult = BrowserResult(result: result, ipAddress: iPv6Address)
                            self.servicesListener?.onServicesChanged(browserResult)
                        }
                    } else {
                        PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle failed, not hostPort")
                    }
                } else {
                    PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle failed, path is null")
                }
            case let .failed(error):
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle, Connection failed with error: \(error)")

            case .setup:
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle, setup")
            case let .waiting(error):
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle, waiting:\(error)")
            case .preparing:
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle, preparing, Resolved for \(result.endpoint.debugDescription)")
            case .cancelled:
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle, cancelled")
            default:
                PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "resolveServiceEndpointSingle, unknown")
                break
            }
        }
        connection.start(queue: .global())
    }

    func stopBrowsing() {
        PurpleLogger.current.d(BonjourServiceBrowser_NW.TAG, "stopBrowsing")
        browser?.cancel()
        browser = nil
        discoveredServices = []
    }
}
