//
//  ObservableList.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation
import SwiftUI

@Observable
class ObservableList<T> {
    var elements: [T] = []

    func add(_ t: T) -> Bool {
        withAnimation{
            elements.append(t)
        }
        
        return true
    }

    func add(position: Int, _ t: T) -> Bool {
        if position < 0 || position > elements.count {
            return false
        }
        withAnimation{
            elements.insert(t, at: position)
        }
     
        return true
    }

    func addAll(_ tList: [T]) {
        withAnimation{
            for t in tList {
                elements.append(t)
              
            }
        }
        
    }

    func remove(_ t: T) -> Bool {
        let findedIndex = elements.firstIndex { T in
            objectAddress(T as AnyObject) == objectAddress(t as AnyObject)
        }
        if let index = findedIndex{
           _ =  withAnimation{
                elements.remove(at: index)
            }
           
            return true
        }
        return false
    }

    func removeIf(_ test: (T) -> Bool) {
        let findedIndex = elements.firstIndex { T in
            test(T)
        }
        if let index = findedIndex {
          _ = withAnimation{
                elements.remove(at: index)
            }
           
        }
    }

    func updateByIndex(_ index: Int, t: T) {
        if index < 0 || index > elements.count {
            return
        }
        withAnimation{
            elements.remove(at: index)
            elements.insert(t, at: index)
        }
       
    }

    func removeLast() -> Bool {
        if elements.isEmpty {
            return false
        }
        _ = withAnimation{
            elements.removeLast()
        }
        
        return true
    }

    func removeFirst() -> Bool {
        if elements.isEmpty {
            return false
        }
        _ = withAnimation{
            elements.removeFirst()
        }
       
        return true
    }

    func clear() -> Bool {
        if elements.isEmpty {
            return true
        }
        _ = withAnimation{
            elements.removeAll()
        }
       
        return true
    }

    func contains(_ t: T) -> Bool {
        let hashT = t as? any Hashable
        return elements.contains(where: { element in
            if let hashable = element as? any Hashable {
                hashT?.hashValue == hashable.hashValue
            } else {
                false
            }
        })
    }

    func count() -> Int {
        return elements.count
    }
    
    func isEmpty()->Bool{
        return elements.isEmpty
    }
}
