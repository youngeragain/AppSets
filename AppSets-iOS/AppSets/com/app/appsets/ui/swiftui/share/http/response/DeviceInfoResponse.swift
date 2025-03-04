//
//  ContentInfoListResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct DeviceInfoResponse: DesignResponse {
    
    typealias D = HttpShareDevice
    
    var code: Int
    
    var info: String?
    
    var data: HttpShareDevice?
    
}
