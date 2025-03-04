//
//  CreateAppPage.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import SwiftUI

struct CreateAppPage: View {
    let onBackClickListener: () -> Void

    init(onBackClick: @escaping () -> Void) {
        onBackClickListener = onBackClick
    }

    var body: some View {
        VStack {
            Spacer().frame(height: 52)
            BackActionTopBar(
                backText: nil,
                onBackClick: onBackClickListener
            )
            VStack {
                Spacer()
                ZStack {
                    Text("CreateAppPage").frame(alignment: .center)
                }
                Spacer()
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
        .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    CreateAppPage(onBackClick: {
    })
}
