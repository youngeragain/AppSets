//
//  Triple.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/18.
//

import Foundation

struct Triple<V1, V2, V3> {
    let first: V1
    let second: V2
    let third: V3
    
    init(_ first: V1, _ second: V2, _ third: V3) {
        self.first = first
        self.second = second
        self.third = third
    }
}
