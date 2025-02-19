//
//  ConversationDetailsPage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct ConversationDetailsPage: View {
    @ObservedObject var conversationUseCase: ConversationUseCase

    let onBackClickListener: () -> Void

    @State var inputContent: String = ""

    @State var sessionSearchKeywords: String = ""

    @FocusState private var isFocused: Bool // 用于跟踪焦点状态

    @State private var keyboardHeight: CGFloat = 0

    init(conversationUseCase: ConversationUseCase, onBackClick: @escaping () -> Void) {
        self.conversationUseCase = conversationUseCase
        onBackClickListener = onBackClick
        inputContent = inputContent
    }

    func ImMessageItemWrapperComponent(_ message: any ImMessage) -> some View {
        ZStack {
            VStack {
                Spacer().frame(height: 12)
                HStack(alignment: .top, spacing: 12) {
                    AsyncImage(
                        url: URL(string: message.fromInfo.avatarUrl ?? ""),
                        content: { image in
                            image
                                .resizable()
                                .scaledToFit()
                                .frame(width: 36, height: 36, alignment: .center)
                                .clipShape(RoundedRectangle(cornerSize: CGSize(width: 8, height: 8)))
                        },
                        placeholder: {
                            RoundedRectangle(cornerSize: CGSize(width: 6, height: 6))
                                .frame(width: 36, height: 36, alignment: .center)
                                .foregroundColor(.white)
                        }
                    )
                    VStack(spacing: 4) {
                        HStack {
                            if let dateString = message.readableDate {
                                Text(dateString).lineLimit(1).font(.system(size: 10))
                            }

                            Text(ImMessageStatic.readableContent(message) ?? "")
                        }
                        .padding(10)
                        .background(Color(UIColor.separator))
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                    }

                    Spacer()
                    VStack {
                    }
                }
                Spacer().frame(height: 12)
            }
        }.padding(.init(top: 0, leading: 12, bottom: 0, trailing: 12))
    }

    var body: some View {
        VStack {
            Spacer().frame(height: 68)
            BackActionTopBar(
                backText: nil,
                onBackClick: onBackClickListener,
                customCenterView: {
                    HStack(spacing: 12) {
                        AsyncImage(
                            url: URL(string: conversationUseCase.currentSession?.imObj.avatarUrl ?? ""),
                            content: { image in
                                image
                                    .resizable()
                                    .scaledToFit()
                                    .frame(width: 20, height: 20, alignment: .center)
                                    .clipShape(RoundedRectangle(cornerRadius: 6))
                            },
                            placeholder: {
                                RoundedRectangle(cornerRadius: 6)
                                    .frame(width: 20, height: 20, alignment: .center)
                                    .foregroundColor(Color(UIColor.separator))
                            }
                        )
                        Text(conversationUseCase.currentSession?.imObj.name ?? "").font(.system(size: 12)).lineLimit(1)
                    }
                }
            )
            let allSimpleSessions = conversationUseCase.getAllSimpleSessionsByKeywords(sessionSearchKeywords)
            if !allSimpleSessions.isEmpty {
                ScrollView(.horizontal) {
                    HStack(spacing: 12) {
                        Spacer().frame(width: 6)
                        ForEach(allSimpleSessions) { session in
                            HStack(spacing: 6) {
                                AsyncImage(
                                    url: URL(string: session.imObj.avatarUrl ?? ""),
                                    content: { image in
                                        image
                                            .resizable()
                                            .scaledToFit()
                                            .frame(width: 16, height: 16, alignment: .center)
                                            .clipShape(Circle())
                                    },
                                    placeholder: {
                                        Circle()
                                            .frame(width: 16, height: 16, alignment: .center)
                                            .foregroundColor(Color(UIColor.separator))
                                    }
                                )
                                Text(session.imObj.name).font(.system(size: 10)).lineLimit(1)
                            }
                            .padding([.all], 8)
                            .overlay(
                                content: {
                                    RoundedRectangle(cornerRadius: 24, style: .continuous)
                                        .stroke()
                                        .cornerRadius(24)
                                        .foregroundColor(Color(UIColor.separator))
                                }
                            ).onTapGesture {
                                sessionSearchKeywords = ""
                                conversationUseCase.updateCurrentSessionBySession(session)
                            }
                        }
                        Spacer().frame(width: 6)
                    }
                }.scrollIndicators(.hidden)
            }

            ScrollViewReader { proxy in
                VStack {
                    ScrollView {
                        LazyVStack {
                            if let session = conversationUseCase.currentSession {
                                ForEach(session.conversationState.messages.elements.indices, id: \.self) { index in
                                    let message = session.conversationState.messages.elements[index]
                                    ImMessageItemWrapperComponent(message).id(index)
                                }
                            }
                            Spacer()
                        }
                    }
                    VStack(spacing: 12) {
                        HStack {
                            TextField(
                                text: $inputContent,
                                prompt: Text("Text Something"),
                                label: {
                                    Text(inputContent).lineLimit(3)
                                }
                            ).focused($isFocused)
                                .padding([.all], 16)
                            if !String.isNullOrEmpty(inputContent) {
                                Button(
                                    action: {
                                        conversationUseCase.onSendMessage(LocalContext.current, InputSelector.TEXT, inputContent)
                                        DispatchQueue.global().async {
                                            Thread.sleep(forTimeInterval: 200)
                                            guard let session = conversationUseCase.currentSession else {
                                                return
                                            }
                                            if session.conversationState.messages.elements.isEmpty {
                                                return
                                            }
                                            DispatchQueue.main.async {
                                                proxy.scrollTo(session.conversationState.messages.elements.count - 1)
                                            }
                                        }
                                    },
                                    label: {
                                        Text("send")
                                            .tint(.white)
                                            .padding(.init(top: 8, leading: 12, bottom: 8, trailing: 12))
                                            .background(
                                                RoundedRectangle(cornerRadius: 16)
                                                    .foregroundColor(.blue)
                                            )
                                    })
                            }
                        }
                        Divider()
                        HStack(spacing: 12) {
                            SwiftUI.Image("drawable/add_circle-add_circle_symbol")
                                .resizable()
                                .scaledToFit()
                                .fontWeight(.light)
                                .frame(width: 20, height: 20)
                                .foregroundColor(Color(UIColor.separator))
                            SwiftUI.Image("drawable/mic-mic_symbol")
                                .resizable()
                                .scaledToFit()
                                .fontWeight(.light)
                                .frame(width: 20, height: 20)
                                .foregroundColor(Color(UIColor.separator))
                            Spacer()
                        }.padding(.init(top: 0, leading: 12, bottom: 24, trailing: 12))
                    }
                    .padding()
                }.offset(x: 0, y: -keyboardHeight)
            }
        }
        .frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
        .edgesIgnoringSafeArea(.all)
        .scrollDismissesKeyboard(ScrollDismissesKeyboardMode.automatic)
        .onAppear {
            NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillShowNotification, object: nil, queue: .main) { notification in
                if let height = getKeyboardHeight(from: notification) {
                    withAnimation{
                        keyboardHeight = height
                    }
                    
                }
            }

            NotificationCenter.default.addObserver(forName: UIResponder.keyboardWillHideNotification, object: nil, queue: .main) { _ in
                withAnimation{
                    keyboardHeight = 0
                }
            }
        }
        .onDisappear {
            conversationUseCase.onDispose()
            NotificationCenter.default.removeObserver(self) // 移除观察者
        }
    }
}

func getKeyboardHeight(from notification: Notification) -> CGFloat? {
    guard let userInfo = notification.userInfo,
          let keyboardFrame = userInfo[UIResponder.keyboardFrameEndUserInfoKey] as? CGRect else {
        return nil
    }
    return keyboardFrame.height
}

#Preview {
    ConversationDetailsPage(
        conversationUseCase: ConversationUseCase(topSpaceContentUseCase: NowSpaceContentUseCase()),
        onBackClick: {
        }
    )
}
