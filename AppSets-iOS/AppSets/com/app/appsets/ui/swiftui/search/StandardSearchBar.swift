//
//  StandardSearchBar.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/16.
//

import SwiftUI

struct StandardSearchBar: View {
    
    @ObservedObject var brokerTest: BrokerTest
    
    let onClickListener: (String) -> Void
    
    init(onClick: @escaping (String) -> Void) {
        self.onClickListener = onClick
        brokerTest = BrokerTest.Instance
    }
    
    var body: some View {
        VStack{
            HStack{
                Button(
                    action: {
                        onClickListener("SearchBarIcon")
                    }
                ){
                    HStack{
                        Spacer().frame(width: 12)
                        SwiftUI.Image("drawable/search-search_symbol")
                            .fontWeight(.light)
                            .foregroundColor(.init(UIColor.black))
                        Text("seach")
                            .foregroundColor(.init(UIColor.black))
                    }
                    .frame(minWidth:100, maxWidth: 150, minHeight: 40, maxHeight: 40, alignment:.leading)
                    .background(
                        content: {
                            RoundedRectangle(cornerRadius: 20)
                                .foregroundColor(Color(UIColor.separator))
                        }
                    )
                }
                
                Button(
                    action:{
                        onClickListener("UserIcon")
                    }
                ){
                    if LocalAccountManager.Instance.isLogged() {
                        
                        let borderColor = if(brokerTest.isOnline){
                            Color.green
                        }else{
                            Color.red
                        }
                        AsyncImage(
                            url: URL(string: LocalAccountManager.Instance.userInfo.avatarUrl ?? ""),
                            content: { image in
                                image
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 40, height: 40, alignment: .center)
                                    .clipShape(Circle())
                            },
                            placeholder: {
                                SwiftUI.Image("drawable/face-face_symbol")
                                    .fontWeight(.light)
                                    .foregroundColor(.init(UIColor.black))
                                    .padding(12)
                            }
                        ).overlay {
                            Circle().stroke(borderColor, lineWidth: 2)
                        }
                    }else{
                        SwiftUI.Image("drawable/face-face_symbol")
                            .fontWeight(.light)
                            .foregroundColor(.init(UIColor.separator))
                            .padding(12)
                    }
                }
                .background(Circle().foregroundColor(Color(UIColor.separator)))
            }
            Spacer().frame(height: 6)
        }
    }
}

#Preview {
    StandardSearchBar(onClick: { tab in })
}
