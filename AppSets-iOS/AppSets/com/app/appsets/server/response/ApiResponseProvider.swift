//
//  ResponseProvider.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/10.
//

struct ApiResponseProvider: DefaultResponseProvider {
    
    public static let INSTANCE = ApiResponseProvider()
    
    func provideResponse<D>(_ d: D.Type) -> DesignResponse<D> where D : Decodable, D : Encodable {
        return DesignResponse<D>(code: -1, data: nil)
    }

}
