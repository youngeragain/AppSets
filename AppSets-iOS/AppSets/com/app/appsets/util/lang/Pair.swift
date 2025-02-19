//
//  Pair.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/2.
//

import Foundation

struct Pair<V1, V2> {
    let first: V1
    let second: V2
    init(_ first: V1, _ second: V2) {
        self.first = first
        self.second = second
    }
}
