//
//  ShareApiSuffix.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/10.
//

struct ShareApiSuffix {
    public static let API_GREETING = "/appsets/share"
    public static let API_PING = "/appsets/share/ping"
    public static let API_IS_NEED_PIN = "/appsets/share/pin/isneed"
    public static let API_PAIR = "/appsets/share/pair"
    public static let API_PAIR_RESPONSE = "/appsets/share/pair_response"
    public static let API_POST_TEXT = "/appsets/share/text"
    public static let API_POST_FILE = "/appsets/share/file"
    public static let API_PREPARE_SEND = "/appsets/share/prepare"
    public static let API_PREPARE_SEND_RESPONSE = "/appsets/share/prepare_response"
    public static let API_GET_CONTENT = "/appsets/share/content/get"
    public static let API_GET_CONTENT_LIST = "/appsets/share/contents/get"
    public static let API_EXCHANGE_DEVICE_INFO = "/appsets/share/device/info/exchange"

    public static func apiBaseUrl(shareDevice:HttpShareDevice, port:Int) -> String? {
        if let ip4 = shareDevice.deviceAddress.ip4 {
            if !ip4.isEmpty {
                return "http://\(ip4):\(port)"
            }
        }
        
        if let ip6 = shareDevice.deviceAddress.ip6 {
            if !ip6.isEmpty {
                return "http://[\(ip6)]:\(port)"
            }
        }
        
        return nil
    }
}
