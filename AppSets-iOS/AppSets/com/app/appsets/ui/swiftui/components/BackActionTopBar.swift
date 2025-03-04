//
//  BackActionTopBar.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct BackActionTopBar<V, V2>: View where V: View, V2: View {
    
    let backButtonRightText : String?
    
    let onBackClickListener: () -> Void
    
    
    @ViewBuilder let customCenterContent: (()-> V)?
    
    @ViewBuilder let customEndContent: (()-> V2)?
    
    init(
        backText: String?,
        onBackClick: @escaping () -> Void,
        @ViewBuilder customCenterView centerView: @escaping (()-> V) = {VStack{}},
        @ViewBuilder customEndView endView: @escaping (()-> V2) = {VStack{}}
    ) {
        self.backButtonRightText = backText
        self.onBackClickListener = onBackClick
        self.customCenterContent = centerView
        self.customEndContent = endView
    }
    
    var body: some View {
        VStack(spacing: 12){
            HStack{
                HStack{
                    Spacer().frame(width: 12)
                    SwiftUI.Image("drawable/arrow_back-arrow_back_symbol")
                        .resizable()
                        .scaledToFit()
                        .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                        .fontWeight(.light)
                        .padding(12)
                        .onTapGesture {
                            onBackClickListener()
                        }
                    if let title = backButtonRightText {
                        Text(title)
                    }
                    Spacer()
                }
                HStack{
                    if let centerContent = customCenterContent {
                        centerContent()
                    }else{
                        Spacer()
                    }
                }
                HStack{
                    Spacer()
                    if let endContent = customEndContent {
                        endContent()
                    }
                    Spacer().frame(width: 12)
                }
            }
            Divider().foregroundColor(Theme.colorSchema.outline)
        }
    }
    
}

#Preview {
    BackActionTopBar(
        backText: "back",
        onBackClick: {
            print("click")
        },
        customCenterView: {
            VStack{}
        },
        customEndView: {
            Text("custom end view")
        })
}
