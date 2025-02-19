//
//  ObservableList.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

class ObservableList<T> : ObservableObject {
    
    @Published var elements:[T] = []
    
    func add(_ t:T) -> Bool {
        elements.append(t)
        return true
    }
    
    func add(position:Int, _ t:T) -> Bool {
        if position < 0 || position > elements.count {
            return false
        }
        elements.insert(t, at: position)
        return true
    }
    
    func addAll(_ tList: [T]) {
        for t in tList {
            elements.append(t)
        }
    }
    
    func remove(_ t:T) -> Bool {
        let findedIndex = elements.firstIndex { T in
            if t is AnyObject && T is AnyObject {
                objectAddress(T as AnyObject) == objectAddress(t as AnyObject)
            }else{
                false
            }
        }
        if findedIndex != nil {
            elements.remove(at: findedIndex!)
            return true
        }
        return false
    }
    
    func updateByIndex(_ index:Int, t:T) {
        if index < 0 || index > elements.count {
            return
        }
        elements.remove(at: index)
        elements.insert(t, at: index)
    }
    
    func removeLast() -> Bool {
        if elements.isEmpty {
            return false
        }
        elements.removeLast()
        return true
    }
    
    func removeFirst() -> Bool {
        if elements.isEmpty {
            return false
        }
        elements.removeFirst()
        return true
    }
    
    func clear() -> Bool {
        if elements.isEmpty {
            return true
        }
        elements.removeAll()
        return true
    }
    
    func contains(_ t:T ) -> Bool {
        let hashT = t as? any Hashable
        return elements.contains(where: { element in
            if let hashable = element as? any Hashable {
                hashT?.hashValue == hashable.hashValue
            }else {
                false
            }
        })
    }
    
    func count()->Int{
        return elements.count
    }
    
}
