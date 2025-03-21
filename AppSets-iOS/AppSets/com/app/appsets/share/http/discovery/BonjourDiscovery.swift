//
//  BonjourDiscovery.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//
import Foundation
import Network

class BonjourDiscovery: Discovery {
    private static let TAG = "BonjourDiscovery"
    private static let BONJOUR_PORT: UInt16 = 11100

    private var publisher: BonjourServicePublisher_NW?
    private var browser: BonjourServiceBrowser_NW?

    private let httpShareMethod: HttpShareMethod

    init(httpShareMethod: HttpShareMethod) {
        self.httpShareMethod = httpShareMethod

        let publisher = BonjourServicePublisher_NW()
        self.publisher = publisher

        let browser = BonjourServiceBrowser_NW()

        struct Listener: BonjourServiceBrowser_NW.BonjourServiceListener {
            let bonjourDiscovery: BonjourDiscovery
            func onServicesChanged(_ browserResult: BrowserResult) {
                PurpleLogger.current.d(BonjourDiscovery.TAG, "onServicesChanged, browserResult:\(browserResult)")
                let metadata = browserResult.result.metadata

                switch metadata {
                case let Network.NWBrowser.Result.Metadata.bonjour(txtRecord):
                    guard let nickNameEntryData = txtRecord.getEntry(for: ShareDevice.NICK_NAME)?.data else {
                        PurpleLogger.current.d(BonjourDiscovery.TAG, "onServicesChanged, result.nickNameEntryData is null, return")
                        return
                    }
                    guard let nickName = String(data: nickNameEntryData, encoding: .utf8) else {
                        PurpleLogger.current.d(BonjourDiscovery.TAG, "onServicesChanged, result.nickName is null, return")
                        return
                    }
                    guard let rawNameEntryData = txtRecord.getEntry(for: ShareDevice.RAW_NAME)?.data else {
                        PurpleLogger.current.d(BonjourDiscovery.TAG, "onServicesChanged, result.rawNameEntryData is null, return")
                        return
                    }
                    guard let rawName = String(data: rawNameEntryData, encoding: .utf8) else {
                        PurpleLogger.current.d(BonjourDiscovery.TAG, "onServicesChanged, result.rawName is null, return")
                        return
                    }

                    var deviceType: Int = ShareDevice.DEVICE_TYPE_PHONE
                    if let deviceTypeEntryData = txtRecord.getEntry(for: ShareDevice.DEVICE_TYPE)?.data {
                        // PurpleLogger.current.d(BonjourDiscovery.TAG, "onServicesChanged, result.deviceTypeEntryData is null")
                        if let deviceTypeString = String(data: deviceTypeEntryData, encoding: .utf8) {
                            // PurpleLogger.current.d(BonjourDiscovery.TAG, "onServicesChanged, result.deviceTypeString is null")

                            if let deviceTypeTemp = Int(deviceTypeString) {
                                // PurpleLogger.current.d(BonjourDiscovery.TAG, "onServicesChanged, result.deviceType is null")
                                deviceType = deviceTypeTemp
                            }
                        }
                    }

                    let deviceName = DeviceName(rawName: rawName, nickName: nickName)

                    var deviceIps: [DeviceIP] = []

                    if let ipString = bonjourDiscovery.extraIpAddressToIpString(browserResult.ipAddress){
                        let deviceIp = DeviceIP(ip: ipString)
                        deviceIps.append(deviceIp)
                    }

                    let deviceAddress = DevcieAddress(ips: deviceIps)

                    let httpShareDevice = HttpShareDevice(
                        deviceName: deviceName,
                        deviceAddress: deviceAddress,
                        deviceType: deviceType
                    )
                    bonjourDiscovery.httpShareMethod.notifyShareDeviceAddOnDiscovery(httpShareDevice)
                default:
                    break
                }
            }
        }
        let listener = Listener(bonjourDiscovery: self)
        browser.setServicesListener(listener)
        self.browser = browser
    }

    func extraIpAddressToIpString(_ ipAddress: IPAddress) -> String? {
        if ipAddress is IPv4Address {
            let str = "\(ipAddress)"
            if !str.contains("%") {
                return str
            }
            if let index = str.firstIndex(of: "%") {
                let ip = String(str[..<index])
                return ip
            }
        } else if ipAddress is IPv6Address {
            let str = "\(ipAddress)"
            if !str.contains("%") {
                return str
            }
            
            if let index = str.firstIndex(of: "%") {
                let ip = String(str[..<index])
                return ip
            }
        }
        return nil
    }

    func startService() {
        PurpleLogger.current.d(BonjourDiscovery.TAG, "startService")
        publisher?.publishService(port: BonjourDiscovery.BONJOUR_PORT, deviceName: httpShareMethod.deviceName)
    }

    func stopService() {
        publisher?.stopService()
        browser?.stopBrowsing()
        publisher = nil
        browser = nil
    }

    func startDiscovery() {
        browser?.startBrowsing()
    }

    func cancelDiscovery() {
        browser?.stopBrowsing()
    }
}
