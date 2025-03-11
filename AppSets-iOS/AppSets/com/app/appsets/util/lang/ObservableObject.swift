//
//  ObservableObject.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/11.
//
import Foundation

@Observable
class ObservableObject<T> {
    var obj:T
    init(_ obj: T) {
        self.obj = obj
    }
}
