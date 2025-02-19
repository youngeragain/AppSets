//
//  SpotlightHotwordsComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import SwiftUI

struct WordOfDayComponent: View {
    
    let wordOfDayState: WordOfTheDayState
    
    init(state: WordOfTheDayState) {
        self.wordOfDayState = state
    }
    
    var body: some View {
        TabView{
            ForEach(wordOfDayState.items.indices, id:\.self){ index in
                ZStack(alignment: .bottom){
                    let item =  wordOfDayState.items[index]
                    let url = if item is WordOfTheDay {
                        (item as! WordOfTheDay).picUrl ?? ""
                    }else if item is TodayInHistory{
                        (item as! TodayInHistory).picUrl ?? ""
                    }else{
                        String(describing: item)
                    }
                    ZStack{
                        Color(uiColor: .separator).clipShape(RoundedRectangle(cornerRadius: 16))
                        AsyncImage(
                            url: URL(string: url),
                            content: { image in
                                image
                                    .resizable()
                                    .scaledToFit()
                                    .cornerRadius(16)
                            },
                            placeholder: {
                                Color(uiColor: .separator)
                                    .cornerRadius(16)
                            }
                        )
                    }
                    HStack{
                        ZStack{
                            VStack(alignment: .leading, spacing: 12){
                                let title = if item is WordOfTheDay {
                                    (item as! WordOfTheDay).word ?? ""
                                }else if item is TodayInHistory{
                                    (item as! TodayInHistory).event ?? ""
                                }else{
                                    String(describing: item)
                                }
                                let subTitle = if item is WordOfTheDay {
                                    (item as! WordOfTheDay).author ?? ""
                                }else if item is TodayInHistory{
                                    (item as! TodayInHistory).title ?? ""
                                }else{
                                    String(describing: item)
                                }
                                Text(title).font(.system(size: 12))
                                Text(subTitle).font(.system(size: 10))
                            }
                        }
                        .padding()
                        Spacer()
                    }
                   
                }.padding()
            }
        }
        .frame(height: 250)
        .tabViewStyle(.page(indexDisplayMode: .never))
    }
}

#Preview {
    WordOfDayComponent(state: WordOfTheDayState(items: []))
}
