//
//  MD5Helper.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation
import CryptoSwift

struct MD5Helper {
    
    struct TransformResult {
        let outContent:String
        let ountConentBase64:String
    }
    
    static func transform(_ input:String) -> TransformResult {
        let outContent = input.md5()
        let outContentBase64 = outContent.data(using: .utf8)!.base64EncodedString()
        return TransformResult(outContent: input.md5(), ountConentBase64: outContentBase64)
    }
    
}
