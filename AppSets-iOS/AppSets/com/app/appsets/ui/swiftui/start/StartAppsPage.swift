//
//  StartAppPanelPage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct StartAppsPage: View {
    
    let onBackClickListener: ()-> Void
    
    init(onBackClick: @escaping () -> Void) {
        self.onBackClickListener = onBackClick
    }
    
    var body: some View {
        VStack{
            BackActionTopBar(
                backText: nil,
                onBackClick: onBackClickListener
            )
            Text("StartAppsPage")
        }.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    StartAppsPage(
        onBackClick: {
            
        }
    )
}
