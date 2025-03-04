//
//  OutSideComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/12.
//

import SwiftUI

struct OutSidePage: View {
    
    @ObservedObject var screenUseCase: ScreenUseCase
    
    let onBioClickListener: (any Bio) -> Void
    
    init(screenUseCase: ScreenUseCase, onBioClick: @escaping (any Bio) -> Void) {
        self.screenUseCase = screenUseCase
        self.onBioClickListener = onBioClick
    }
    
    var body: some View {
        VStack{
            ScreenList(
                screens: screenUseCase.userScreens,
                onBioClick: onBioClickListener
            )
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
        .edgesIgnoringSafeArea(.all)
        .onAppear{
            screenUseCase.loadOutSideScreen()
        }
    }
}

#Preview {
    OutSidePage(
        screenUseCase: ScreenUseCase(),
        onBioClick: { bio in
        
        }
    )
}
