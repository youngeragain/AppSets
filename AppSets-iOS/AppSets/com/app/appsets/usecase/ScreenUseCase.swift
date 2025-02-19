//
//  ScreenUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class ScreenUseCase: ObservableObject {
    
    private static let TAG = "ScreenUseCase"
    
    @Published var userScreens:[ScreenInfo] = []
    
    private let screenRepository: ScreenRepository = ScreenRepository()
    
    func loadOutSideScreen() {
        PurpleLogger.current.d(ScreenUseCase.TAG, "loadOutSideScreen")
        Task {
            if let systemScreens = await screenRepository.getIndexRecommendScreens(){
                DispatchQueue.main.async {
                    self.updateScreens(systemScreens)
                }
            }
        }
    }
    
    @MainActor
    private func updateScreens(_ new:[ScreenInfo]){
        userScreens = new
    }
    
}
