//
//  ApiBaseUrlProvider.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/10.
//

struct ApiBaseUrlProvider: BaseUrlProvider {
    
    public static let INSTANCE = ApiBaseUrlProvider()
    
    func provideUrl() -> String {
        return APISuffix.apiBaseUrl()
    }
}
