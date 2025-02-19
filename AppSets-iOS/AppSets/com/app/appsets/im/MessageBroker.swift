//
//  MessageBroker.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/2.
//

import Foundation

protocol MessageBroker {
    
    associatedtype C:MessageBrokerConfig
    
    func bootstrap(_ config: C)
    
    func retry()
    
    func close()
    
    func sendMessage(_ imObj:ImObj, _ imImMessage:any ImMessage) async -> Bool
}
