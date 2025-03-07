//
//  UserProfilePage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct UserProfilePage: View {
    
    private let userProfileFeatures:[String] = ["Application", "Screen", "Followers/Followed", "Update", "Chat", "Add"]
    
    @Environment(MainViewModel.self) var viewModel: MainViewModel
    
    let onBackClickListener: ()-> Void
    
    init(onBackClick: @escaping () -> Void) {
        self.onBackClickListener = onBackClick
    }
    
    var body: some View {
        VStack(spacing:12){
            let userInfoUseCase = viewModel.userInfoUseCase
            Spacer().frame(height: 68)
            BackActionTopBar(
                backText: nil,
                onBackClick: onBackClickListener
            )
            VStack(spacing: 16){
                AsyncImage(
                    url: URL(string: userInfoUseCase.currentUserInfo.avatarUrl ?? ""),
                    content: { image in
                        image
                            .resizable()
                            .scaledToFit()
                            .frame(width: 250)
                            .clipShape(RoundedRectangle(cornerRadius: 32))
                    },
                    placeholder: {
                        RoundedRectangle(cornerRadius: 32)
                            .frame(width: 250, height: 250, alignment: .center)
                            .foregroundColor(Color(UIColor.separator))
                    }
                )
                Text(userInfoUseCase.currentUserInfo.name ?? "")
                    .padding(.init(top: 2, leading: 0, bottom: 0, trailing: 0))
                Text(userInfoUseCase.currentUserInfo.introduction ?? "")
                    .font(.system(size: 12)).padding(.init(top: 2, leading: 0, bottom: 0, trailing: 0))
            }
            ScrollView(.horizontal){
                HStack(spacing: 12){
                    Spacer().frame(width: 4)
                    ForEach(userProfileFeatures, id:\.self){ feature in
                        Text(feature)
                            .font(.system(size: 12))
                            .padding(12)
                            .background((Color(uiColor: .separator)))
                            .clipShape(RoundedRectangle(cornerRadius: 10))
                    }
                    Spacer().frame(width: 4)
                }
            }.scrollIndicators(.hidden)
            Spacer()
        }
    }
}

#Preview {
    UserProfilePage(
        onBackClick: {
        
        }
    )
}
