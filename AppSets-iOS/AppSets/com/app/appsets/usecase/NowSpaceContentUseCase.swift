//
//  TopSpaceUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/13.
//

import Foundation
import SwiftUI

protocol NowSpaceState {
    
}

struct NewImMessage: NowSpaceState {
    
    let session: Session
    let imMessage: any ImMessage
    
    init(session: Session, imMessage: any ImMessage) {
        self.session = session
        self.imMessage = imMessage
    }
}

struct NowSpaceNULL: NowSpaceState {
    
}

@Observable
class NowSpaceContentUseCase {
    
    private static let TAG = "NowSpaceContentUseCase"
    
    var content: any NowSpaceState = NowSpaceNULL()
    
    func onNewImMessage(session: Session, imMessage: any ImMessage) {
        PurpleLogger.current.d(NowSpaceContentUseCase.TAG, "onNewImMessage")
        DispatchQueue.main.async {
            withAnimation{
                self.content = NewImMessage(session: session, imMessage: imMessage)
            }
        }
    }
    
    func removeContent() {
        PurpleLogger.current.d(NowSpaceContentUseCase.TAG, "removeContent")
        withAnimation{
            content = NowSpaceNULL()
        }
    }
    
    func removeContentIf(_ test: (NowSpaceState) -> Bool){
        PurpleLogger.current.d(NowSpaceContentUseCase.TAG, "removeContentIf")
        if test(content) {
            removeContent()
        }
    }
}
