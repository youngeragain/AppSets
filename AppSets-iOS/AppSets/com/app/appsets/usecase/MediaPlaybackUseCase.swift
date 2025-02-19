//
//  MediaPlaybackUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import Foundation

class MediaPlaybackUseCase: ObservableObject {
    
    private static let TAG = "MediaPlaybackUseCase"
    
    private let remotePlayer:RemotePlayer = RemotePlayer()
    
    @Published var isPlaying:Bool = false
    
    @Published var currentSong: Song = Song(
            title: "See You Again",
            artist: "Artist",
            imageUrl: "drawable/pic_see_you_again",
            url:"https://appsets-2022-1258462798.cos.ap-chengdu.myqcloud.com/files-dev/m/see_you_again.mp3"
        )
    
    
    init() {
        PurpleLogger.current.d(MediaPlaybackUseCase.TAG, "init, \(self)")
    }
    
    deinit {
        PurpleLogger.current.d(MediaPlaybackUseCase.TAG, "deInit, \(self)")
    }
    
    func play() {
        PurpleLogger.current.d(MediaPlaybackUseCase.TAG, "play, currentSong:\(currentSong)")
        guard let url = URL(string: currentSong.url), !String.isNullOrEmpty(currentSong.url) else{
            return
        }
        isPlaying = true
        remotePlayer.play(url: url)
    }
    
    func pause() {
        PurpleLogger.current.d(MediaPlaybackUseCase.TAG, "pause, currentSong:\(currentSong)")
        isPlaying = false
        remotePlayer.pause()
    }
    
    func togglePlayback() {
        PurpleLogger.current.d(MediaPlaybackUseCase.TAG, "togglePlayback")
      
        if isPlaying {
            pause()
        }else {
            play()
        }
    }
    
    func requestNext() {
        PurpleLogger.current.d(MediaPlaybackUseCase.TAG, "requestNext")
    }
    
    func requestPrevious() {
        PurpleLogger.current.d(MediaPlaybackUseCase.TAG, "requestPrevious")
    }
}
