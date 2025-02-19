//
//  RabbitMqBrokerProperty.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct RabbitMqBrokerProperty: Codable {
    let uid: String
    let userExchangeGroups:String?
    let host:String
    let port:Int
    let username:String
    let password:String
    let virtualHost:String
    let queuePrefix:String
    let routingKeyPrefix:String
    let groupExchangePrefix:String
    let groupExchangeParent:String
    let groupRootExchange:String
    let groupSubRootExchange:String
}
