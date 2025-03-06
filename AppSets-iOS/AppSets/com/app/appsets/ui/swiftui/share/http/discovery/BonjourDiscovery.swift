//
//  BonjourDiscovery.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/6.
//

class BonjourDiscovery: Discovery {
    private static let TAG = "BonjourDiscovery"
    private static let BONJOUR_PORT: UInt16 = 11100

    private var publisher: BonjourServicePublisher_NW? = nil
    private var browser: BonjourServiceBrowser_NW? = nil

    private let httpShareMethod: HttpShareMethod

    init(httpShareMethod: HttpShareMethod) {
        self.httpShareMethod = httpShareMethod
        
        let publisher = BonjourServicePublisher_NW()
        self.publisher = publisher

        let browser = BonjourServiceBrowser_NW()
        self.browser = browser
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
