//
//  ShareMethod.swift
//  AppSets
//
//  Created by caiju Xu on 2025/3/2.
//
import Foundation
import SwiftUI

class ShareMethod: SwiftUILifecycle, ObservableObject {
    @Published var deviceName: DeviceName = DeviceName.NONE

    func initMethod() {
    }

    func onAppear() {
    }

    func onDisappear() {
        destroy()
    }

    func onCreate() {
    }

    func onDestroy() {
        destroy()
    }

    func destroy() {
    }
}
