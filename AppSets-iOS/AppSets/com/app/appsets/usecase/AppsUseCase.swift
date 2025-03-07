//
//  AppsUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

@Observable
class AppsUseCase {
    
    private static let TAG = "AppsUseCase"
    
    var applications: [AppWithCategory] = []
    
    private var application: Application? = nil
    
    func setCurrentApplication(_ application: Application?) {
        self.application = application
        PurpleLogger.current.d(AppsUseCase.TAG, "setCurrentApplication, application:\(String(describing: application))")
    }
    
    func getViewApplication() -> Application? {
        return self.application
    }
    
    func loadInitialData(_ context: Context) {
        if !applications.isEmpty {
            return
        }
        PurpleLogger.current.d(AppsUseCase.TAG, "loadInitialData")
        Task {
            if let applications = await AppSetsRepository().getIndexApplications(context) {
                self.updateApplications(applications)
            }
        }
    }
    
    func updateApplications(_ applications:[AppWithCategory]){
        self.applications = applications
    }
    
}
