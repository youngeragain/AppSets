//
//  DefaultResponseProvider.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/10.
//

protocol DefaultResponseProvider {
    func provideResponse<D:Codable>(_ d:D.Type) -> DesignResponse<D>
}
