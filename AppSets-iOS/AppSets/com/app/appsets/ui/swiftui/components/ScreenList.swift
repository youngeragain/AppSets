//
//  ScreenList.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/27.
//

import SwiftUI

struct ScreenList: View {
    
    var screens:[ScreenInfo]
    
    let onBioClickListener: (any Bio) -> Void
    
    init(screens: [ScreenInfo], onBioClick: @escaping (any Bio) -> Void) {
        self.screens = screens
        self.onBioClickListener = onBioClick
    }
    
    var body: some View {
        ZStack{
            ScrollView(.vertical){
                Spacer().frame(height: 52)
                VStack(spacing: 8){
                    ForEach(screens.indices, id:\.self){ index in
                        Screen(
                            screenInfo: screens[index],
                            onBioClick: onBioClickListener
                        )
                    }

                }
                Spacer().frame(height: 128)
            }
            .padding()
            .scrollIndicators(.hidden)
        }
        
    }
}

#Preview {
    ScreenList(
        screens: [],
        onBioClick: { bio in
                
        }
    )
}
