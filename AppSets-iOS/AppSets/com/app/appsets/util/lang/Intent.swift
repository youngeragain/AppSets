//
//  Intent.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import Foundation

class Intent {
    
    public static let Empty = Intent()
    
    private var bundle:[String: Any] = [String: Any]()
    
    private var context: Context? = nil
    
    private var componentType: Any.Type? = nil
    
    init(context: Context? = nil, componentType: Any.Type? = nil) {
        self.context = context
        self.componentType = componentType
    }
    
    func putString(_ key:String, _ value:String) {
        self.bundle[key] = value
    }
    
    func getString(_ key:String) -> String? {
        let valueFinded = (self.bundle[key] as? String)
        return valueFinded
    }
    
    func putInt(_ key:String, _ value:Int) {
        self.bundle[key] = value
    }
    
    func getInt(_ key:String, defaultValue: Int) -> Int {
        let valueFinded = (self.bundle[key] as? Int) ?? defaultValue
        return valueFinded
    }
    
    func getComponentType() -> Any.Type? {
        return componentType
    }
}
