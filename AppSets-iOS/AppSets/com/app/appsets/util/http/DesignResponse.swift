//
//  DesignResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/8.
//

import Foundation

protocol DesignResponse<D>: Codable where D : Codable {
    
    associatedtype D
    
    var code:Int { get set }
    
    var info:String? { get set }
    
    var data:D? { get set }
    
}
