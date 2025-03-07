//
//  ScreenUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

@Observable
class ScreenUseCase {
    
    private static let TAG = "ScreenUseCase"
    
    var userScreens:[ScreenInfo] = []
    
    private let screenRepository: ScreenRepository = ScreenRepository()
    
    func loadOutSideScreen() {
        PurpleLogger.current.d(ScreenUseCase.TAG, "loadOutSideScreen")
        Task {
            if let systemScreens = await screenRepository.getIndexRecommendScreens(){
                self.updateScreens(systemScreens)
            }
        }
    }
    
    //@MainActor
    private func updateScreens(_ screens:[ScreenInfo]){
        userScreens = screens
    }
    
}
