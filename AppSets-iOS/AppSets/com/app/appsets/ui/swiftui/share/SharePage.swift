//
//  WLANP2PPage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct BoxFocusInfo {
    var receiveBoxFocus: Bool = false
    var devicesBoxFocus: Bool = false
    var sendBoxFocus: Bool = false

    mutating func setReceiveBoxFocus(_ focus: Bool, exclusive: Bool = true) {
        receiveBoxFocus = focus
        devicesBoxFocus = false
        sendBoxFocus = false
    }

    mutating func setDevicesBoxFocus(_ focus: Bool, exclusive: Bool = true) {
        receiveBoxFocus = false
        devicesBoxFocus = focus
        sendBoxFocus = false
    }

    mutating func setSendBoxFocus(_ focus: Bool, exclusive: Bool = true) {
        receiveBoxFocus = false
        devicesBoxFocus = false
        sendBoxFocus = focus
    }
}

struct SharePage: View {
    private static let TAG = "SharePage"

    var shareMethod: ShareMethod
    var shareViewModel: ShareViewModel

    @State var inputContent: String = ""
    @State var isShowInput: Bool = false
    @State var isShowSettings: Bool = false

    let onBackClickListener: () -> Void

    init(onBackClick: @escaping () -> Void) {
        PurpleLogger.current.d(SharePage.TAG, "init")
        onBackClickListener = onBackClick
        let shareViewModel = ShareViewModel.INSTANCE
        self.shareViewModel = shareViewModel
        let shareMethod = HttpShareMethod.INSTANCE
        self.shareMethod = shareMethod
    }

    func getBoxsHeight(_ containerSize: CGSize) -> (CGFloat, CGFloat, CGFloat) {
        var receiveBoxHeight: CGFloat = .infinity
        var devicesBoxHeight: CGFloat = .infinity
        var sendBoxHeight: CGFloat = .infinity
        let boxFocusInfo = shareViewModel.boxFocusInfo
        if boxFocusInfo.receiveBoxFocus {
            receiveBoxHeight = containerSize.height * 0.7
        } else if !boxFocusInfo.devicesBoxFocus && !boxFocusInfo.sendBoxFocus {
            receiveBoxHeight = containerSize.height * 0.333
        } else {
            receiveBoxHeight = containerSize.height * 0.15
        }

        if boxFocusInfo.devicesBoxFocus {
            devicesBoxHeight = containerSize.height * 0.7
        } else if !boxFocusInfo.receiveBoxFocus && !boxFocusInfo.sendBoxFocus {
            devicesBoxHeight = containerSize.height * 0.333
        } else {
            devicesBoxHeight = containerSize.height * 0.15
        }

        if boxFocusInfo.sendBoxFocus {
            sendBoxHeight = containerSize.height * 0.7
        } else if !boxFocusInfo.receiveBoxFocus && !boxFocusInfo.devicesBoxFocus {
            sendBoxHeight = containerSize.height * 0.333
        } else {
            sendBoxHeight = containerSize.height * 0.15
        }

        return (receiveBoxHeight, devicesBoxHeight, sendBoxHeight)
    }

    var body: some View {
        VStack {
            Spacer().frame(height: 52)
            GeometryReader { geometry in
                let containerSize = geometry.size
                let boxsHeight = getBoxsHeight(containerSize)
                let receiveBoxHeight: CGFloat = boxsHeight.0
                let devicesBoxHeight: CGFloat = boxsHeight.1
                let sendBoxHeight: CGFloat = boxsHeight.2
                VStack {
                    Spacer().frame(height: 12)

                    ZStack(alignment: .topLeading) {
                        VStack {
                            HStack {
                                if shareViewModel.boxFocusInfo.receiveBoxFocus {
                                    Text("Show received content here")
                                } else {
                                    SwiftUI.Image("drawable/call_received_call_received_symbol")
                                        .fontWeight(.light)
                                        .padding(12)
                                        .tint(Theme.colorSchema.onSurface)
                                    let text = if shareViewModel.receivedContentList.count() > 0 {
                                        "Received Space(\(shareViewModel.receivedContentList.count()))"
                                    } else {
                                        "Received Space"
                                    }
                                    Text(text)
                                }
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
                    .frame(maxWidth: .infinity, maxHeight: receiveBoxHeight)
                    .background(
                        Theme.colorSchema.surface.clipShape(RoundedRectangle(cornerRadius: 32))
                    ).onTapGesture {
                        withAnimation {
                            var boxFocusInfo = shareViewModel.boxFocusInfo
                            boxFocusInfo.setReceiveBoxFocus(!boxFocusInfo.receiveBoxFocus)

                            shareViewModel.updateBoxFocusInfo(boxFocusInfo)
                        }
                    }

                    Spacer().frame(height: 12)

                    ZStack(alignment: .topLeading) {
                        HStack(alignment: .center, spacing: 12) {
                            if !shareViewModel.boxFocusInfo.devicesBoxFocus {
                                SwiftUI.Image("drawable/qr_code_scanner_qr_code_scanner_symbol")
                                    .fontWeight(.light)
                                    .padding(12)
                                    .tint(Theme.colorSchema.onSurface)

                                SwiftUI.Image("drawable/qr_code_qr_code_symbol")
                                    .fontWeight(.light)
                                    .padding(12)
                                    .tint(Theme.colorSchema.onSurface)
                            }

                            SwiftUI.Image("drawable/settings-settings_symbol")
                                .fontWeight(.light)
                                .padding(12)
                                .tint(Theme.colorSchema.onSurface)

                            Spacer().frame(width: 12)

                        }.frame(maxWidth: .infinity, alignment: .trailing)

                        if shareViewModel.boxFocusInfo.devicesBoxFocus {
                            Grid(alignment: .topLeading) {
                                GridRow(alignment: .center) {
                                    ForEach(shareViewModel.shareDeviceList.elements) { shareDevice in
                                        VStack(spacing: 4) {
                                            VStack {
                                                SwiftUI.Image("drawable/smartphone_smartphone_symbol")
                                                    .resizable()
                                                    .scaledToFit()
                                                    .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                                                    .fontWeight(.light)
                                                    .tint(Theme.colorSchema.onSecondaryContainer)
                                            }
                                            .padding(12)
                                            .background(Theme.colorSchema.secondaryContainer.clipShape(Circle()))
                                            .onLongPressGesture {
                                            } onPressingChanged: { _ in
                                            }

                                            if let nickName = shareDevice.deviceName.nickName {
                                                Text(nickName).font(.system(size: 12))
                                            }
                                            Text(shareDevice.deviceName.rawName).font(.system(size: 10))
                                        }.padding(12)
                                    }
                                }.frame(alignment: .leading)
                            }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                        }

                        if !shareViewModel.boxFocusInfo.devicesBoxFocus {
                            VStack {
                                HStack {
                                    SwiftUI.Image("drawable/refresh-refresh_symbol")
                                        .fontWeight(.light)
                                        .padding(12)
                                        .tint(Theme.colorSchema.onSurface)
                                    let text = if shareViewModel.shareDeviceList.count() > 0 {
                                        "Devices(\(shareViewModel.shareDeviceList.count()))"
                                    } else {
                                        "Devices"
                                    }
                                    Text(text)
                                }
                                if let nickName = shareViewModel.mShareDevice.deviceName.nickName {
                                    Text(nickName).font(.system(size: 16)).fontWeight(.bold)
                                }
                            }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                        }
                    }
                    .frame(maxWidth: .infinity, maxHeight: devicesBoxHeight)
                    .background(
                        Theme.colorSchema.surface.clipShape(RoundedRectangle(cornerRadius: 32))
                    ).onTapGesture {
                        withAnimation {
                            var boxFocusInfo = shareViewModel.boxFocusInfo
                            boxFocusInfo.setDevicesBoxFocus(!boxFocusInfo.devicesBoxFocus)

                            shareViewModel.updateBoxFocusInfo(boxFocusInfo)
                        }
                    }

                    Spacer().frame(height: 12)

                    ZStack(alignment: .topLeading) {
                        HStack(alignment: .center, spacing: 12) {
                            SwiftUI.Image("drawable/draft_draft_symbol")
                                .fontWeight(.light)
                                .padding(12)
                                .tint(Theme.colorSchema.onSurface)

                            SwiftUI.Image("drawable/notes_notes_symbol")
                                .fontWeight(.light)
                                .padding(12)
                                .tint(Theme.colorSchema.onSurface).onTapGesture {
                                    isShowInput = true
                                }.sheet(isPresented: $isShowInput) {
                                    InputSheet({ str in
                                        isShowInput = false
                                        if !str.isEmpty{
                                            withAnimation {
                                                shareViewModel.addPendingContent(str)
                                            }
                                        }
                                        
                                    }).presentationDetents([.medium])
                                }

                            if shareViewModel.pendingSendContentList.count() > 0 {
                                SwiftUI.Image("drawable/ios_share_ios_share_symbol")
                                    .fontWeight(.light)
                                    .padding(12)
                                    .tint(Theme.colorSchema.onSurface)

                                SwiftUI.Image("drawable/delete_forever_delete_forever_symbol")
                                    .fontWeight(.light)
                                    .padding(12)
                                    .tint(Theme.colorSchema.onSurface).onTapGesture {
                                        shareViewModel.removeAllPendingSendContent()
                                    }
                            }
                            Spacer().frame(width: 12)
                        }.frame(maxWidth: .infinity, alignment: .trailing)
                        VStack {
                            HStack {
                                if shareViewModel.boxFocusInfo.sendBoxFocus {
                                    Text("Show send content here")
                                } else {
                                    SwiftUI.Image("drawable/call_made_call_made_symbol")
                                        .fontWeight(.light)
                                        .padding(12)
                                        .tint(Theme.colorSchema.onSurface)

                                    let text = if shareViewModel.pendingSendContentList.count() > 0 {
                                        "Send Space(\(shareViewModel.pendingSendContentList.count()))"
                                    } else {
                                        "Send Space"
                                    }
                                    Text(text)
                                }
                            }
                        }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
                    }
                    .frame(maxWidth: .infinity, maxHeight: sendBoxHeight)
                    .background(
                        Theme.colorSchema.surface.clipShape(RoundedRectangle(cornerRadius: 32))
                    ).onTapGesture {
                        withAnimation {
                            var boxFocusInfo = shareViewModel.boxFocusInfo
                            boxFocusInfo.setSendBoxFocus(!boxFocusInfo.sendBoxFocus)

                            shareViewModel.updateBoxFocusInfo(boxFocusInfo)
                        }
                    }

                    Spacer().frame(height: 12)

                }.frame(maxWidth: .infinity, maxHeight: .infinity)
                    .padding()
            }

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
            PurpleLogger.current.d(SharePage.TAG, "onSubmit")
        }.onChange(of: shareViewModel.shareDeviceList.elements.count) { _, newValue in
            var boxFocusInfo = shareViewModel.boxFocusInfo
            if newValue > 0 &&
                !boxFocusInfo.sendBoxFocus &&
                !boxFocusInfo.receiveBoxFocus &&
                !boxFocusInfo.devicesBoxFocus {
                boxFocusInfo.setDevicesBoxFocus(true)
                withAnimation {
                    shareViewModel.updateBoxFocusInfo(boxFocusInfo)
                }
            }
        }
    }

    func InputSheet(_ onConfirm: @escaping (String) -> Void) -> some View {
        VStack(spacing: 12) {
            HStack {
                Spacer()
                Button {
                    onConfirm(inputContent)
                } label: {
                    Text("ok").tint(Theme.colorSchema.onSecondaryContainer)
                }.frame(alignment: .trailing).padding().background(
                    Theme.colorSchema.secondaryContainer.clipShape(RoundedRectangle(cornerRadius: 24))
                )
            }

            TextField(text: $inputContent, label: {
                Text("input").frame(alignment: .topLeading)
            }).frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                .padding()
                .background(Theme.colorSchema.secondaryContainer.clipShape(RoundedRectangle(cornerRadius: 24)))
        }.padding()
    }
}

#Preview {
    SharePage(
        onBackClick: {
        }
    )
}
