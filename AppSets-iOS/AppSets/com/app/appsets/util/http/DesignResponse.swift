//
//  DesignResponse.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/8.
//

import Foundation
import Hummingbird

protocol DesignResponse<D>: ResponseCodable, Codable where D: Codable {
    associatedtype D

    var code: Int { get set }

    var info: String? { get set }

    var data: D? { get set }
}
