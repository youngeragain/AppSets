//
//  MediaComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import SwiftUI

struct Song{
    let title:String
    let artist:String
    let imageUrl:String
    let url:String
}

struct MediaComponent: View {
    
    @ObservedObject var mediaPlaybackUseCase: MediaPlaybackUseCase

    var body: some View {
        ZStack(){
            
            VStack(alignment:.leading,spacing: 12) {
                HStack{
                    Text("now_playing")
                        .font(.largeTitle)
                        .foregroundColor(.white)
                        .fontWeight(.bold)
                        .frame(alignment: .leading)
                    Spacer()
                }
                

                HStack{
                    VStack(alignment: .leading, spacing: 6) {
                        Text(mediaPlaybackUseCase.currentSong.title)
                            .font(.title3)
                            .fontWeight(.medium)
                            .foregroundColor(.white)

                        Text(mediaPlaybackUseCase.currentSong.artist)
                            .font(.title3)
                            .foregroundColor(.white)
                    }
                    Spacer()
                }
                Spacer()
                
                HStack{
                    Spacer()
                    HStack {
                        Button(
                            action: {
                                mediaPlaybackUseCase.requestPrevious()
                            }
                        ) {
                            SwiftUI.Image(systemName: "chevron.backward.circle.fill")
                                .font(.largeTitle)
                                .foregroundColor(.white)
                        }
                        
                        if mediaPlaybackUseCase.isPlaying {
                            Button(
                                action: {
                                    withAnimation(.smooth){
                                        mediaPlaybackUseCase.togglePlayback()
                                    }
                                
                                }
                            ) {
                                SwiftUI.Image(systemName: "pause.circle.fill")
                                    .font(.largeTitle)
                                    .foregroundColor(.white)
                            }
                        }else{
                            Button(
                                action: {
                                    withAnimation(.smooth){
                                        mediaPlaybackUseCase.togglePlayback()
                                    }
                                }
                            ) {
                                SwiftUI.Image(systemName: "play.circle.fill")
                                    .font(.largeTitle)
                                    .foregroundColor(.white)
                            }
                        }
                        
                        Button(
                            action: {
                                mediaPlaybackUseCase.requestNext()
                            }
                        ) {
                            SwiftUI.Image(systemName: "chevron.forward.circle.fill")
                                .font(.largeTitle)
                                .foregroundColor(.white)
                        }
                    }
                    Spacer()
                }
            }
            .padding()
        }
        .frame(height:200)
        .background(.blue)
        .clipShape(RoundedRectangle(cornerRadius: 16))
        .overlay(
            content: {
                RoundedRectangle(cornerRadius: 16)
                    .stroke()
                    .foregroundColor(Color(UIColor.separator))
            }
        )
        .padding()
    }
}

#Preview {
    MediaComponent(mediaPlaybackUseCase: MediaPlaybackUseCase())
}
