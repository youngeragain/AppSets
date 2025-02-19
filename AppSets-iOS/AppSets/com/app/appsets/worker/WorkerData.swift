//
//  Data.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

struct WorkerData {
    
    private let mValues:[String:Any]?
    
    init(_ values:[String:Any]) {
        self.mValues = values
    }
    
    func getString(_ key:String) -> String? {
        if mValues == nil {
            return nil
        }
        return mValues![key] as? String
    }
}
