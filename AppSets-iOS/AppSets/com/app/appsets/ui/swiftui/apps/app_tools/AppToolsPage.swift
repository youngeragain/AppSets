//
//  AppToolsPage.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/12.
//

import Foundation
import SwiftUI

struct AppsToolsPage: View {
    private static let TAG = "AppsToolsPage"

    public static let TOOL_TYPE_APPSETS_SHARE = "AppSets Share"

    let onBackClickListener: () -> Void

    let onAppToolClickListner: (String) -> Void

    init(onBackClick: @escaping () -> Void,
         onAppToolClick: @escaping (String) -> Void) {
        onBackClickListener = onBackClick
        onAppToolClickListner = onAppToolClick
        PurpleLogger.current.d(AppsToolsPage.TAG, "init")
    }

    var body: some View {
        VStack(alignment: .leading) {
            Spacer().frame(height: 52)
            BackActionTopBar(
                backText: "Tools",
                onBackClick: onBackClickListener
            )
            VStack(spacing: 12) {
                HStack {
                    SwiftUI.Image("drawable/swap_calls-swap_calls_symbol")
                        .fontWeight(.light)
                        .padding(12)
                        .tint(Theme.colorSchema.onSurface)
                    Text("AppSets Share")
                }.onTapGesture {
                    onAppToolClickListner(AppsToolsPage.TOOL_TYPE_APPSETS_SHARE)
                }

            }.padding()
            Spacer()
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
        .edgesIgnoringSafeArea(.bottom)
    }
}

#Preview {
    AppsToolsPage(
        onBackClick: {}, onAppToolClick: { _ in }
    )
}
