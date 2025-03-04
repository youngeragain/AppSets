//
//  ContentInfoListResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/3.
//

struct ContentInfoListResponse: DesignResponse {
    
    typealias D = ContentInfoListWrapper
    
    var code: Int
    
    var info: String?
    
    var data: ContentInfoListWrapper?
    
}
