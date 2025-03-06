//
//  WLANP2PPage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct SharePage: View {
    private static let TAG = "SharePage"

    @ObservedObject var shareMethod: ShareMethod
    @ObservedObject var shareViewModel: ShareViewModel

    let onBackClickListener: () -> Void

    init(onBackClick: @escaping () -> Void) {
        PurpleLogger.current.d(SharePage.TAG, "init")
        onBackClickListener = onBackClick
        let shareViewModel = ShareViewModel.INSTANCE
        self.shareViewModel = shareViewModel
        let shareMethod = HttpShareMethod.INSTANCE
        self.shareMethod = shareMethod
       
    }

    var body: some View {
        VStack {
            Spacer().frame(height: 52)
            VStack {
                Spacer().frame(height: 12)
                ZStack(alignment: .topLeading) {
                    VStack {
                        HStack {
                            SwiftUI.Image("drawable/call_received_call_received_symbol")
                                .fontWeight(.light)
                                .padding(12)
                                .tint(Theme.colorSchema.onSurface)
                            Text("Received Space")
                        }
                    }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                    HStack {
                        SwiftUI.Image("drawable/arrow_back-arrow_back_symbol")
                            .resizable()
                            .scaledToFit()
                            .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                            .fontWeight(.light)
                            .padding(12)
                            .onTapGesture {
                                onBackClickListener()
                            }
                    }.frame(alignment: .topLeading).padding(12)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(
                    Theme.colorSchema.surface.clipShape(RoundedRectangle(cornerRadius: 32))
                )
                Spacer().frame(height: 12)
                ZStack(alignment: .center) {
                    VStack {
                        HStack {
                            SwiftUI.Image("drawable/refresh-refresh_symbol")
                                .fontWeight(.light)
                                .padding(12)
                                .tint(Theme.colorSchema.onSurface)
                            Text("Devices")
                        }

                        Text(shareViewModel.mShareDevice.deviceName.nickName ?? shareViewModel.mShareDevice.deviceName.rawName)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(
                    Theme.colorSchema.surface.clipShape(RoundedRectangle(cornerRadius: 32))
                )
                Spacer().frame(height: 12)
                ZStack(alignment: .center) {
                    VStack {
                        HStack {
                            SwiftUI.Image("drawable/call_made_call_made_symbol")
                                .fontWeight(.light)
                                .padding(12)
                                .tint(Theme.colorSchema.onSurface)
                            Text("Send Space")
                        }
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .background(
                    Theme.colorSchema.surface.clipShape(RoundedRectangle(cornerRadius: 32))
                )
                Spacer().frame(height: 12)
            }.frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding()
        }.frame(
            minWidth: 0,
            maxWidth: .infinity,
            minHeight: 0,
            maxHeight: .infinity
        )
        .background(Theme.colorSchema.primaryContainer)
        .edgesIgnoringSafeArea(.all)
        .onAppear {
            PurpleLogger.current.d(SharePage.TAG, "onAppear")
            shareMethod.onAppear()
        }
        .onDisappear {
            PurpleLogger.current.d(SharePage.TAG, "onDisappear")
            shareMethod.onDisappear()
        }
        .onSubmit {
            PurpleLogger.current.d(SharePage.TAG, "onDisappear")
        }
    }
}

#Preview {
    SharePage(
        onBackClick: {
        }
    )
}
