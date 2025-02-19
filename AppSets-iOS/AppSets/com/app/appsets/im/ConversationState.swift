//
//  ConversationState.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class ConversationState {
    
    private static let TAG = "ConversationState"
    
    let messages : ObservableList<any ImMessage> = ObservableList()
    
    func addMessage(_ imMessage: any ImMessage) {
        PurpleLogger.current.d(ConversationState.TAG, "addMessage")
        _  = messages.add(imMessage)
    }
    
    func addMessages(_ imMessages: [any ImMessage]) {
        PurpleLogger.current.d(ConversationState.TAG, "addMessages")
        messages.addAll(imMessages)
    }
    
    func removeMessage(_ imMessage: any ImMessage) {
        PurpleLogger.current.d(ConversationState.TAG, "removeMessage")
    }
    
    func lastMessage() -> (any ImMessage)? {
        return messages.elements.last
    }
}
