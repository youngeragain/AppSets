//
//  SpotlightPopulateSearchComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import SwiftUI

struct PopulateSearchComponent: View {
    
    let popularSearchState: PopularSearchState
    
    init(state: PopularSearchState) {
        self.popularSearchState = state
    }
    
    var body: some View {
        ZStack{
            VStack(alignment: .leading,spacing:12){
                HStack{
                    Text("热点")
                    Spacer()
                }
                let words = popularSearchState.items
                ForEach(words.indices, id:\.self){ index in
                    let word = words[index]
                    HStack{
                        let text = if word is Hotsearch {
                            (word as! Hotsearch).cardTitle ?? ""
                        }else{
                            String(describing: word)
                        }
                        Text(text).font(.system(size: 10))
                        Spacer()
                    }
                }
                
            }
            .padding()
            .overlay(content: {
                RoundedRectangle(cornerRadius: 16).stroke().foregroundColor(Color(UIColor.separator))
            })
            
        }.padding()
       
    }
}

#Preview {
    PopulateSearchComponent(state: PopularSearchState(icon: "", title: "", items: []))
}
