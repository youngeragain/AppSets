//
//  MainComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/12.
//

import SwiftUI

struct StartPage: View {
    
    @EnvironmentObject var viewModel: MainViewModel
    
    @ObservedObject var startUseCase: StartUseCase
    
    var body: some View {
        ScrollView{
            VStack(){
                Spacer().frame(height: 68)
                let spotLightsState = startUseCase.spotLightsState
                ForEach(spotLightsState.indices, id:\.self) { index in
                    let state = spotLightsState[index]
                    HStack{
                        switch state {
                        case is ReversedSpaceState:
                            ReversedSpaceComponent()
                            
                        case is AudioPlayerState:
                            MediaComponent(mediaPlaybackUseCase: viewModel.mediaPlaybackUseCase)
                            
                        case is PinnedAppState:
                            PinedAppsComponent(state: state as! PinnedAppState)
                            
                        case is RecommendedItemState:
                            RecommendItemsComponent(state: state as! RecommendedItemState)
                            
                        case is BingWallpaperState:
                            BingWallpaperComponent(state: state as! BingWallpaperState)
                            
                        case is WordOfTheDayState:
                            WordOfDayComponent(state: state as! WordOfTheDayState)
                            
                        case is PopularSearchState:
                            PopulateSearchComponent(state: state as! PopularSearchState)
                        default:
                            Spacer().frame(height: 12)
                        }
                    }
                }
                Spacer().frame(height: 128)
            }
        }.scrollIndicators(.hidden)
    }
}

#Preview {
    let mainViewModel = MainViewModel()
    StartPage(
        startUseCase: StartUseCase()
    )
    .environmentObject(mainViewModel)
    .onAppear(perform: {
        LocalContext.provide(t: ContextImpl())
    })
}
