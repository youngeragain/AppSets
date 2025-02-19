//
//  MockTable.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation


class MockTable<T:Identifiable> : Mockable {
    
    private var colums: [T] = []
    
    func clear() {
        colums = []
    }
    
    func findAll() async -> [T] {
        var result:[T] = []
        colums.forEach { element in
            result.append(element)
        }
        return result
    }
    
    func findById(_ id: String) async -> T?{
        return colums.first { element in
            element.id.hashValue == id.hashValue
        }
    }
    
    func insert(_ t: T) async {
        colums.append(t)
    }
    
    func update(_ t: T) async {

        let index = colums.firstIndex { element in
            (element.id.hashValue) == t.id.hashValue
        }
        if let existIndex = index {
            colums.remove(at: existIndex)
            colums.insert(t, at: existIndex)
        }
        
    }
    
    func delete(_ t: T) async {
        colums.removeAll(where: { element in
            element.id.hashValue == t.id.hashValue
        })
    }
    
    func deleteById(_ id: String ) async {
        colums.removeAll(where: { element in
            element.id.hashValue == id.hashValue
        })
    }
    
    func deleteBy(_ test: (T) -> Bool) async {
        colums.removeAll(where: test)
    }
    
}
