//
//  OutSideComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/12.
//

import SwiftUI

struct OutSidePage: View {
    @Environment(MainViewModel.self) var viewModel: MainViewModel
 
    let onBioClickListener: (any Bio) -> Void
    
    init(onBioClick: @escaping (any Bio) -> Void) {
        self.onBioClickListener = onBioClick
    }
    
    var body: some View {
        VStack{
            let screenUseCase = viewModel.screenUseCase
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
}

#Preview {
    OutSidePage(
        onBioClick: { bio in
        
        }
    )
}
