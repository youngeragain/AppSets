//
//  WLANP2PPage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct WLANP2PPage: View {
    
    let onBackClickListener: ()-> Void
    
    init(onBackClick: @escaping () -> Void) {
        self.onBackClickListener = onBackClick
    }
    
    var body: some View {
        VStack{
            Spacer().frame(height: 36)
            BackActionTopBar(
                backText: "Share",
                onBackClick: onBackClickListener,
              
                customEndView: {
                    Button(
                        action: {
                            print("search")
                        },
                        label: {
                            Text("搜索")
                        }
                    )
            })
            Spacer()
        }.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
            .padding()
            .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    WLANP2PPage(
        onBackClick: {
            
        }
    )
}
