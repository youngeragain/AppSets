//
//  Member.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/4.
//

import Foundation

protocol Bio: Identifiable {
    
    var id:String { get }
    
    var name:String? { get }
    
    var bioUrl:String? { get set }
    
}
