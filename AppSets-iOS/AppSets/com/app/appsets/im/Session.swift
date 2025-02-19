//
//  Session.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class Session: Identifiable, Equatable {
    
    static func == (lhs: Session, rhs: Session) -> Bool {
        return lhs.id == rhs.id
    }
    
    
    let imObj : any ImObj
    
    let conversationState : ConversationState
    
    var id: String {
        get{
            return imObj.id
        }
    }
    
    var latestImMessage : (any ImMessage)? {
        get{
            return conversationState.lastMessage()
        }
    }
    
    init(imObj: any ImObj, conversation: ConversationState) {
        self.imObj = imObj
        self.conversationState = conversation
    }
}
