//
//  WLANP2PPage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI
import UniformTypeIdentifiers

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

    @State var isShowContentSelection: Bool = false

    @State var containerSize: CGSize = CGSize()
    
    @State var isShowDeviceContentList:Bool = false
    @State var whichIsShowDeviceContentList:ShareDevice? = nil

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
            AppSetsShareContainer(
                deviceSpaceContent: DevicesSpace,
                receivedSpaceContent: ReceivedSpace,
                sendSpaceContent: SendSpace
            )
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
    
    func AppSetsShareContainer(
        @ViewBuilder deviceSpaceContent: () -> some View,
        @ViewBuilder receivedSpaceContent: () -> some View,
        @ViewBuilder sendSpaceContent: () -> some View
    ) -> some View {
        VStack {
            let boxsHeight = getBoxsHeight(containerSize)
            let receiveBoxHeight: CGFloat = boxsHeight.0
            let devicesBoxHeight: CGFloat = boxsHeight.1
            let sendBoxHeight: CGFloat = boxsHeight.2
            VStack {
                Spacer().frame(height: 12)

                ZStack() {
                    receivedSpaceContent()
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

                ZStack() {
                    deviceSpaceContent()
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

                ZStack() {
                    sendSpaceContent()
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
        }.onGeometryChange(
            for: CGSize.self,
            of: { proxy in
                proxy.size
            },
            action: { proxySize in
                containerSize = proxySize
            }
        )
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

    func ReceivedSpace() -> some View {
        ZStack(alignment: .topLeading) {
            VStack {
                HStack {
                    if !shareViewModel.boxFocusInfo.receiveBoxFocus {
                        SwiftUI.Image(.Drawable.callReceivedCallReceivedSymbol)
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
                    if shareViewModel.boxFocusInfo.receiveBoxFocus && shareViewModel.receivedContentList.isEmpty() {
                        Text("Show received content here")
                    }
                }
            }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            HStack {
                SwiftUI.Image(.Drawable.arrowBackArrowBackSymbol)
                    .resizable()
                    .scaledToFit()
                    .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                    .fontWeight(.light)
                    .padding(12)
                    .onTapGesture {
                        onBackClickListener()
                    }
            }.frame(alignment: .topLeading).padding(12)
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    func DevicesSpace() -> some View {
        ZStack(alignment: .bottomLeading) {
            if shareViewModel.boxFocusInfo.devicesBoxFocus {
                ZStack(alignment: .topLeading){
                    let columns = [GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible()), GridItem(.flexible())]
                    ScrollView(.vertical){
                        LazyVGrid(columns: columns, alignment: .leading) {
                            ForEach(shareViewModel.shareDeviceList.elements) { shareDevice in
                                VStack(spacing: 4) {
                                    VStack {
                                        SwiftUI.Image(.Drawable.smartphoneSmartphoneSymbol)
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                                            .fontWeight(.light)
                                            .tint(Theme.colorSchema.onSecondaryContainer)
                                    }
                                    .padding(12)
                                    .background(Theme.colorSchema.secondaryContainer.clipShape(Circle()))
                                    .onLongPressGesture {
                                        isShowDeviceContentList = true
                                        whichIsShowDeviceContentList = shareDevice
                                        shareMethod.onShareDeviceClick(
                                            shareDevice: shareDevice,
                                            clickType: ShareDevice.CLICK_TYPE_LONG
                                        )
                                    } onPressingChanged: { _ in
                                    }.sheet(isPresented: $isShowDeviceContentList) {
                                        DeviceContentListSheet(shareDevcie: shareDevice)
                                    }

                                    if let nickName = shareDevice.deviceName.nickName {
                                        Text(nickName).font(.system(size: 12)).multilineTextAlignment(.center)
                                    }
                                    Text(shareDevice.deviceName.rawName).font(.system(size: 10)).multilineTextAlignment(.center)
                                }.padding(12)
                            }
                        }
                    }
                    
                }.frame(maxWidth:.infinity, maxHeight:.infinity, alignment: .topLeading)
                
            }

            if !shareViewModel.boxFocusInfo.devicesBoxFocus {
                VStack {
                    HStack {
                        SwiftUI.Image(.Drawable.refreshRefreshSymbol)
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
                    Text(shareViewModel.mShareDevice.deviceName.name).font(.system(size: 20)).fontWeight(.bold)
                }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            }
            
            HStack(alignment: .center, spacing: 12) {
                SwiftUI.Image(.Drawable.qrCodeScannerQrCodeScannerSymbol)
                    .fontWeight(.light)
                    .padding(12)
                    .tint(Theme.colorSchema.onSurface)

                SwiftUI.Image(.Drawable.qrCodeQrCodeSymbol)
                    .fontWeight(.light)
                    .padding(12)
                    .tint(Theme.colorSchema.onSurface)

                SwiftUI.Image(.Drawable.settingsSettingsSymbol)
                    .fontWeight(.light)
                    .padding(12)
                    .tint(Theme.colorSchema.onSurface)

                Spacer().frame(width: 12)
            }.frame(maxWidth: .infinity, alignment: .bottomTrailing)
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    func SendSpace() -> some View {
        ZStack(alignment: .bottomLeading) {
            
            
            if shareViewModel.boxFocusInfo.sendBoxFocus {
                ZStack(alignment: .topLeading){
                    ScrollView(.vertical){
                        LazyVStack(alignment:.leading, spacing: 12){
                            ForEach(shareViewModel.pendingSendContentList.elements, id:\.id) { dataContent in
                                Text(dataContent.name).padding()
                            }
                        }
                    }
                   
                }.frame(maxWidth:.infinity, maxHeight:.infinity, alignment: .topLeading)
                
            }
            
            VStack {
                HStack {
                    if !shareViewModel.boxFocusInfo.sendBoxFocus {
                        SwiftUI.Image(.Drawable.callMadeCallMadeSymbol)
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
                    
                    if shareViewModel.boxFocusInfo.sendBoxFocus &&
                        shareViewModel.pendingSendContentList.isEmpty() {
                        Text("Show send content here")
                    }
                }
            }.frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .center)
            
            HStack(alignment: .center, spacing: 12) {
                SwiftUI.Image(.Drawable.draftDraftSymbol)
                    .fontWeight(.light)
                    .padding(12)
                    .tint(Theme.colorSchema.onSurface).onTapGesture {
                        isShowContentSelection = true
                    }.sheet(isPresented: $isShowContentSelection) {
                        ContentSelectionSheet { urls in
                            withAnimation {
                                urls.forEach { url in
                                    shareViewModel.addPendingContent(url)
                                }
                            }
                        }
                    }

                SwiftUI.Image(.Drawable.notesNotesSymbol)
                    .fontWeight(.light)
                    .padding(12)
                    .tint(Theme.colorSchema.onSurface).onTapGesture {
                        isShowInput = true
                    }.sheet(isPresented: $isShowInput) {
                        InputSheet({ str in
                            isShowInput = false
                            if !str.isEmpty {
                                withAnimation {
                                    shareViewModel.addPendingContent(str)
                                }
                            }

                        }).presentationDetents([.medium])
                    }

                if shareViewModel.pendingSendContentList.count() > 0 {
                    SwiftUI.Image(.Drawable.iosShareIosShareSymbol)
                        .fontWeight(.light)
                        .padding(12)
                        .tint(Theme.colorSchema.onSurface)

                    SwiftUI.Image(.Drawable.deleteForeverDeleteForeverSymbol)
                        .fontWeight(.light)
                        .padding(12)
                        .tint(Theme.colorSchema.onSurface).onTapGesture {
                            shareViewModel.removeAllPendingSendContent()
                        }
                }
                Spacer().frame(width: 12)
            }.frame(maxWidth: .infinity, alignment: .bottomTrailing)
        }.frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    func InputSheet(_ onConfirm: @escaping (String) -> Void) -> some View {
        VStack(spacing: 12) {
            HStack {
                Spacer()
                Button {
                    onConfirm(inputContent)
                } label: {
                    Text("ok").tint(Theme.colorSchema.onSecondaryContainer)
                }.frame(alignment: .trailing)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 6)
                    .background(
                        Theme.colorSchema.secondaryContainer.clipShape(RoundedRectangle(cornerRadius: 24))
                    )
            }
            
            TextEditor(text: $inputContent)
                .scrollContentBackground(.hidden)
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
                .padding()
                .background(Theme.colorSchema.secondaryContainer.clipShape(RoundedRectangle(cornerRadius: 24)))
                .overlay {
                    if(inputContent.isEmpty){
                        Text("Input content").frame(alignment: .topLeading)
                    }
                }
               
        }.padding()
    }

    func ContentSelectionSheet(_ onContentSelected: @escaping ([URL]) -> Void) -> some View {
        DocumentPickerView(onFilePicked: onContentSelected)
    }
    
    func DeviceContentListSheet(shareDevcie:ShareDevice) -> some View {
        VStack{
            HStack{
                Text("Share List")
            }
            if let contentInfoList = shareViewModel.deviceContentListMap[shareDevcie.id] {
                
                LazyVStack{
                    ForEach(contentInfoList.infoList, id: \.id){dataContent in
                        Text(dataContent.name)
                    }
                }
            }
        }.frame(alignment: .top)
    }
}

struct DocumentPickerView: UIViewControllerRepresentable {
    typealias UIViewControllerType = UIDocumentPickerViewController

    typealias Coordinator = PickerUIDocumentPickerDelegate

    var onFilePicked: ([URL]) -> Void

    func makeUIViewController(context: UIViewControllerRepresentableContext<Self>) -> UIDocumentPickerViewController {
        let allowedContentTypes: [UTType] = [UTType.item]
        let picker = UIDocumentPickerViewController(forOpeningContentTypes: allowedContentTypes, asCopy: false)
        picker.allowsMultipleSelection = true
        picker.delegate = context.coordinator
        return picker
    }

    func updateUIViewController(_ uiViewController: UIDocumentPickerViewController, context: UIViewControllerRepresentableContext<Self>) {
    }

    func makeCoordinator() -> PickerUIDocumentPickerDelegate {
        return PickerUIDocumentPickerDelegate(self)
    }

    class PickerUIDocumentPickerDelegate: NSObject, UIDocumentPickerDelegate {
        let parent: DocumentPickerView

        init(_ parent: DocumentPickerView) {
            self.parent = parent
        }

        func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
            parent.onFilePicked(urls)
        }

        func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        }
    }
}

#Preview {
    SharePage(
        onBackClick: {
        }
    )
}
