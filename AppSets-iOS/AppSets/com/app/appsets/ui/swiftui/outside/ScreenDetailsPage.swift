//
//  ScreenDetailsPage.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import SwiftUI

struct ScreenDetailsPage: View {
    
    let onBackClickListener: ()-> Void
    
    init(onBackClick: @escaping () -> Void) {
        self.onBackClickListener = onBackClick
    }
    
    var body: some View {
        VStack{
            BackActionTopBar(
                backText: "Screen expand",
                onBackClick: onBackClickListener
            )
            ZStack{
                Text("ScreenDetailsPage").frame(alignment: .center)
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
        .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    ScreenDetailsPage(
        onBackClick: {
        
        }
    )
}
