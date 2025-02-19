//
//  SpotlightHolidayComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import SwiftUI

struct BingWallpaperComponent: View {
    
    let bingWallpaperState: BingWallpaperState
    
    init(state: BingWallpaperState) {
        self.bingWallpaperState = state
    }
    
    var body: some View {
        TabView {
            ForEach(bingWallpaperState.items.indices, id:\.self){ index in
                ZStack(){
                    let wallpaper = bingWallpaperState.items[index]
                    ZStack{
                        
                        AsyncImage(
                            url: URL(string: wallpaper.url),
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
                    HStack(){
                        ZStack{
                            VStack(alignment: .leading,spacing: 12){
                                Text(wallpaper.whereText).font(.system(size: 12))
                                Text(wallpaper.whereBlowText).font(.system(size: 10))
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
    BingWallpaperComponent(
        state: BingWallpaperState(items: [])
    )
}
