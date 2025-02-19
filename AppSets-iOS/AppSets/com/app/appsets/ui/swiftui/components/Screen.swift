//
//  Screen.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct Screen: View {
    
    let screenInfo: ScreenInfo
    
    let onBioClickListener: (any Bio) -> Void
    
    init(screenInfo: ScreenInfo, onBioClick: @escaping (any Bio) -> Void) {
        self.screenInfo = screenInfo
        self.onBioClickListener = onBioClick
    }
    
    var body: some View {
        ZStack{
            VStack(alignment: .leading, spacing: 12){
                ScreenPictures(pictures: screenInfo.mediaFileUrls ?? [])
                if let screenContent = screenInfo.screenContent {
                    if(!screenContent.isEmpty){
                        Text(screenContent).padding(.init(top: 0, leading: 14, bottom: 0, trailing: 0))
                    }
                }
                if let associateTopics = screenInfo.associateTopics {
                    if (!associateTopics.isEmpty) {
                        Text(associateTopics).padding(.init(top: 0, leading: 14, bottom: 0, trailing: 0)).font(.system(size: 12))
                    }
                }
                
                if let associateUsers = screenInfo.associateUsers {
                    if !associateUsers.isEmpty {
                        Text(associateUsers).padding(.init(top: 0, leading: 14, bottom: 0, trailing: 0)).font(.system(size: 12))
                    }
                }
                
                HStack{
                    Text(screenInfo.postTime ?? "").font(.system(size: 12)).font(.system(size: 12))
                    Spacer()
                    HStack(spacing:12){
                        Text(screenInfo.userInfo?.name ?? "").font(.system(size: 12)).font(.system(size: 12))
                        AsyncImage(
                            url: URL(string: screenInfo.userInfo?.avatarUrl ?? ""),
                            content: { image in
                                image
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 16, height: 16, alignment: .center)
                                    .clipShape(RoundedRectangle(cornerRadius: 6))
                            },
                            placeholder: {
                                RoundedRectangle(cornerRadius: 6)
                                    .frame(width: 16, height: 16, alignment: .center)
                                    .foregroundColor(Color(UIColor.separator))
                            }
                        )
                    }.onTapGesture {
                        onBioClickListener(screenInfo.userInfo!)
                    }
                }.padding(12)
            }
            .overlay(
                content: {
                    RoundedRectangle(
                        cornerSize: CGSize(width: 16, height: 16)
                    )
                    .stroke()
                    .foregroundColor(Color(UIColor.separator))
                }
            )
            .padding(10)
            .onTapGesture(perform: {
                onBioClickListener(screenInfo)
            })
        }
        
    }
}

struct ScreenPictures: View {
    let pictures:[ScreenMediaFileUrl]
    var body: some View {
        VStack(alignment: .leading){
            let rowCount = getRowCount()
            ForEach(0..<rowCount, id: \.self) { vIndex in
                HStack(spacing:6){
                    let rowPictures = getRowOfPictures(row: vIndex)
                    ForEach(rowPictures.indices, id:\.self){ hIndex in
                        let screenMediaFileUrl = rowPictures[hIndex]
                        AsyncImage(
                            url: URL(string: screenMediaFileUrl.mediaFileUrl),
                            content: { image in
                                image
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 110, height: 110, alignment: .center)
                                    .clipShape(RoundedRectangle(cornerRadius: 6))
                            },
                            placeholder: {
                                RoundedRectangle(cornerRadius: 6)
                                    .frame(width: 110, height: 110, alignment: .center)
                                    .foregroundColor(Color(UIColor.separator))
                            }
                        )
                    }
                }
            }
        }.padding(.init(top: 12, leading: 12, bottom: 0, trailing: 12))
    }
    
    func getRowOfPictures(row:Int)->[ScreenMediaFileUrl]{
        let start = row * 3
        let end = min(pictures.count - 1, start + 3)
        return Array(pictures[start..<end])
    }
    
    func getRowCount()->Int{
        let temp = pictures.count % 3
        var rowCount = pictures.count / 3
        if temp != 0 {
            rowCount = rowCount + 1
        }
        return rowCount
    }
}

#Preview {
    Text("screen")
}
