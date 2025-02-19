//
//  PinedApps.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import SwiftUI

struct AppDefinition{
    let name:String
    let icon:String
    let packageName:String
}

struct PinedAppsComponent: View {
    
    let pinnedAppState : PinnedAppState
    
    init(state: PinnedAppState) {
        self.pinnedAppState = state
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing:12){
            HStack{
                Text("已固定")
                Spacer()
                Button(action: {}, label: {
                    Text("所有应用").font(.subheadline)
                })
            }
            let apps = pinnedAppState.items
            if apps.isEmpty {
                Spacer()
                HStack{
                    Spacer()
                    Text("将喜欢的应用固定在此")
                    Spacer()
                }
               
                Spacer()
            }
        }.frame(height:250).padding()
    }
}

#Preview {
    PinedAppsComponent(state: PinnedAppState(items: []))
}
