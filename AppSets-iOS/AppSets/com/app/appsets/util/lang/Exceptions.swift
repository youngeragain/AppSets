//
//  Exceptions.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/4.
//

import Foundation

enum Exception : Error {
    
    case IllegalStateException(message:String? = nil)
    
    case NotImplementationException(message:String? = nil)
    
}
