//
//  ScreenEditPage.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import SwiftUI

struct ScreenEditPage: View {
    
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
            Text("ScreenEditPage")
        }.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    ScreenEditPage(
        onBackClick: {
            
        }
    )
}
