//
//  SettingsPage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct SettingsPage: View {
    
    let onBackClickListener: ()-> Void
    
    init(onBackClick: @escaping () -> Void) {
        self.onBackClickListener = onBackClick
    }
    
    var body: some View {
        VStack{
            BackActionTopBar(
                backText: "setttings",
                onBackClick: onBackClickListener
            )
        }
    }
}

#Preview {
    SettingsPage(
        onBackClick: {
        
        }
    )
}
