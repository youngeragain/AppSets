//
//  CreateAppPage.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import SwiftUI


struct CreateAppPage: View {
    
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
            ZStack{
                Text("CreateAppPage").frame(alignment: .center)
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

