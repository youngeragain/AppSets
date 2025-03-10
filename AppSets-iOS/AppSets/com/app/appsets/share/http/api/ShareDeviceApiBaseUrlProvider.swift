//
//  ShareApiBaseUrlProvider.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/10.
//
struct ShareDeviceApiBaseUrlProvider: BaseUrlProvider {
    
    var shareDevice:HttpShareDevice
    
    func provideUrl() -> String {
        return ShareApiSuffix.apiBaseUrl(shareDevice: shareDevice, port: HttpShareMethod.SHARE_SERVER_API_PORT) ?? ""
    }
}
