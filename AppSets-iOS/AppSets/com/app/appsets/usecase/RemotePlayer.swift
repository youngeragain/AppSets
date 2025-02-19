//
//  RemotePlayer.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/24.
//

import Foundation
import AVFoundation

class RemotePlayer {
    
    private static let TAG = "RemotePlayer"
    
    private let player: AVPlayer = AVPlayer()

    func play(url: URL) {
        player.replaceCurrentItem(with: AVPlayerItem.init(url: url))
        player.play()
    }

    func pause() {
        player.pause()
    }
    
    func play() {
        player.play()
    }

    func stop() {
        //player.stop()
    }
    
    func seekTo(time:Int) {
        //player.seek(to: Date)
    }
}
