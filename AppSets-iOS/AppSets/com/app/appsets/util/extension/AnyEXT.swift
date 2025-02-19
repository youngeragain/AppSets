//
//  AnyEXT.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/10.
//

import Foundation
import SwiftUI

func objectAddress(_ obj:AnyObject) -> UnsafeMutableRawPointer {
    return Unmanaged.passUnretained(obj).toOpaque()
}

extension View {
    
    func startService(_ intent: Intent) {
        
    }
    
}


public extension Int {
    
    static func randomIntNumber(lower: Int = 0,upper: Int = Int(UInt32.max)) -> Int {
        return lower + Int(arc4random_uniform(UInt32(upper - lower)))
    }
    static func randomIntNumber(range: Range<Int>) -> Int {
        return randomIntNumber(lower: range.lowerBound, upper: range.upperBound)
    }
    
}
