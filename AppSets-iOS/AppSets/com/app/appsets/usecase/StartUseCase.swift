//
//  StartUseCase.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/15.
//

import Foundation

protocol SpotLightsState{
    
}

struct PinnedAppState : SpotLightsState {
    let items: [AppDefinition]
}

struct RecommendedItemState : SpotLightsState {
    let items: [ItemDefinition]
}

struct BingWallpaperState : SpotLightsState {
  let items: [MicrosoftBingWallpaper]
}

struct WordOfTheDayState : SpotLightsState {
    let items: [Any]
}

struct PopularSearchState : SpotLightsState {
    let icon:String
    let title:String
    let items:[Any]
}

struct ReversedSpaceState : SpotLightsState {
    
}

struct AudioPlayerState : SpotLightsState {
    
}

class StartUseCase : ObservableObject {
    
    private static let TAG = "StartUseCase"
    
    @Published var spotLightsState: [any SpotLightsState] = []
    
    func doInitData(_ context: Context) {
        loadSpotLight(context)
    }
    
    private func loadSpotLight(_ context: Context) {
        if(!spotLightsState.isEmpty){
            return
        }
        
        Task{
            if let spotLight = await AppSetsRepository().getSpotLight() {
                DispatchQueue.main.async {
                    let audioPlayer = AudioPlayerState()
                    self.spotLightsState.append(audioPlayer)
                    
                    let pinnedApps = PinnedAppState(items: [])
                    self.spotLightsState.append(pinnedApps)
                    
                    let recommendItems = RecommendedItemState(items: [])
                    self.spotLightsState.append(recommendItems)
                    
                    if let wallpaperList = spotLight.microsoftBingWallpaperList {
                        let bingWallpaper = BingWallpaperState(items: wallpaperList)
                        self.spotLightsState.append(bingWallpaper)
                    }
                    
                    var worldOfDayItems: [Any] = []
                    spotLight.wordOfTheDayList?.forEach({ wordOfDay in
                        worldOfDayItems.append(wordOfDay)
                    })
                    spotLight.todayInHistoryList?.forEach({ todayInHistory in
                        worldOfDayItems.append(todayInHistory)
                    })
                    let wordOfTheDay = WordOfTheDayState(items: worldOfDayItems)
                    
                    self.spotLightsState.append(wordOfTheDay)
                    
                    var popularSearchItems:[Any] = []
                    spotLight.popularSearches?.keywords?.forEach({ word in
                        popularSearchItems.append(word)
                    })
                    spotLight.baiduHotData?.hotsearch?.forEach({ hotsearch in
                        popularSearchItems.append(hotsearch)
                    })
                   
                    let popularSearch = PopularSearchState(icon: "", title: "热点", items: popularSearchItems)
                    
                    self.spotLightsState.append(popularSearch)
                    
                }
            }
        }
        
    }
    
}
