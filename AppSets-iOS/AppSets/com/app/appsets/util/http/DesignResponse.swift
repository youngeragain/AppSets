//
//  DesignResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/8.
//

import Foundation
import Hummingbird

class DesignResponse<D>: ResponseCodable where D : Codable {
    var code: Int

    var info: String?

    var data: D?
    
    init(code: Int, info: String? = nil, data: D? = nil) {
        self.code = code
        self.info = info
        self.data = data
    }
}

struct DesignResponseStatic {
    public static func notFound<D:Codable>()->DesignResponse<D>{
        return DesignResponse<D>(code: 404, data: nil)
    }
    public static func badRequest<D:Codable>()->DesignResponse<D>{
        return DesignResponse<D>(code: 400, data: nil)
    }
}
