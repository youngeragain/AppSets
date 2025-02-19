//
//  RecommendItemsComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import SwiftUI

struct ItemDefinition{
    let name:String
    let icon:String
}

struct RecommendItemsComponent: View {
    
    let recommendItemState: RecommendedItemState
    
    init(state: RecommendedItemState) {
        self.recommendItemState = state
    }
    
    var body: some View {
        VStack(alignment: .leading, spacing:12){
            HStack{
                Text("推荐的项目")
                Spacer()
                Button(action: {}, label: {
                    Text("更多").font(.subheadline)
                })
            }
            let items = recommendItemState.items
            if items.isEmpty {
                Spacer()
                HStack{
                    Spacer()
                    Text("推荐的项目")
                    Spacer()
                }
                Spacer()
            }
            
        }.frame(height:250).padding()
    }
}

#Preview {
    RecommendItemsComponent(state: RecommendedItemState(items: []))
}
