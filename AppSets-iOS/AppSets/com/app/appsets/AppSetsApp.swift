//
//  AppSetsApp.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/11.
//	

import SwiftUI

@main
struct AppSetsApp: App {
        
    private let viewModel: MainViewModel

    var body: some Scene {
        WindowGroup {
            MainPage().environment(viewModel)
        }
    }
    
    init() {
        let contextImpl = ContextImpl()
        let contextWrapper = ContextWrapper(contextImpl)
        LocalContext.provide(t: contextWrapper)
        MockSystem.startMock()
        self.viewModel = MainViewModel()
    }

}
