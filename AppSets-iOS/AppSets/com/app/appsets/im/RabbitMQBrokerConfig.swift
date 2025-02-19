//
//  RabbitMQBrokerConfig.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

struct RabbitMQBrokerConfig : MessageBrokerConfig {
    
    let property:RabbitMqBrokerProperty
    
    init(_ property: RabbitMqBrokerProperty) {
        self.property = property
    }
}
