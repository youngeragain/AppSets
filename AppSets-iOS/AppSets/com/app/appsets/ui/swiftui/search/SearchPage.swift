//
//  SearchComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/16.
//

import SwiftUI

protocol SearchResult{
  
}

struct ScreenForSearch:SearchResult{

     let userScreenInfo:ScreenInfo? = nil
}

struct UserInfoForSearch:SearchResult{
    let userInfo:UserInfo? = nil
}

struct ApplicationForSearch:SearchResult{
    let application:String? = nil
}

struct GroupInfoForSearch:SearchResult{
    let groupInfo:String? = nil
}


struct SearchPage: View {

    @State var searchResults:[SearchResult] = [
        ScreenForSearch(),
        UserInfoForSearch(),
        ApplicationForSearch(),
        GroupInfoForSearch()
    ]
    
    @State var searchString:String = ""
    
    let onBackClickListener: ()-> Void
    
    let onSearchClickListener: ()-> Void
    
    @ObservedObject var searchUseCase: SearchUseCase
    
    init(searchUseCase:SearchUseCase, onBackClick: @escaping () -> Void, onSearchClick: @escaping () -> Void) {
        self.searchUseCase = searchUseCase
        self.onBackClickListener = onBackClick
        self.onSearchClickListener = onSearchClick
    }
    
    var body: some View {
        VStack{
            Spacer().frame(height: 68)
            TextField(
                text: $searchString,
                prompt: Text("search"),
                label: {
                    Text(searchString)
                }
            )
            .padding(.init(top: 0, leading: 46, bottom: 0, trailing: 46))
            .overlay(
                content: {
                    HStack{
                        SwiftUI.Image("drawable/arrow_back-arrow_back_symbol")
                            .fontWeight(.light)
                            .padding()
                            .onTapGesture {
                                onBackClickListener()
                            }
                        Spacer()
                        SwiftUI.Image("drawable/search-search_symbol")
                            .fontWeight(.light)
                            .padding()
                            .onTapGesture {
                                onSearchClickListener()
                            }
                    }
                    RoundedRectangle(cornerRadius: 36)
                        .stroke()
                        .foregroundColor(Color(UIColor.separator))
            })
            .padding(.init(top: 24, leading: 12, bottom: 0, trailing: 12))
            Spacer()
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
        .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    SearchPage(
        searchUseCase: SearchUseCase(), 
        onBackClick: {
            
        },
        onSearchClick: {
            
        }
    )
}
