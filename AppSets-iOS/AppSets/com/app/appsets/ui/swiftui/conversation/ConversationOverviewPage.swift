//
//  ConversationOverviewComponent.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/16.
//

import SwiftUI

struct ConversationOverviewPage: View {
    
    @ObservedObject var conversationUseCase: ConversationUseCase
    
    let onSessionClick: (Session)-> Void
    
    init(
        conversationUseCase: ConversationUseCase,
        onSessionClick: @escaping (Session) -> Void
    ) {
        self.conversationUseCase = conversationUseCase
        self.onSessionClick = onSessionClick
    }
    
    func OverViewSingleSessionContent(_ session:Session) -> some View {
        HStack(alignment: .center, spacing: 10){
            AsyncImage(
                url: URL(string: session.imObj.avatarUrl ?? ""),
                content: { image in
                    image
                        .resizable()
                        .scaledToFit()
                        .frame(width: 48, height: 48, alignment: .center)
                        .clipShape(RoundedRectangle(cornerRadius: 12))
                },
                placeholder: {
                    RoundedRectangle(cornerRadius: 12)
                        .frame(width: 48, height: 48, alignment: .center)
                        .foregroundColor(Color(UIColor.separator))
                }
            )
            let messages = session.conversationState.messages
            VStack(alignment: .leading, spacing: 4){
                Text(session.imObj.name)
                    .lineLimit(1)
                    .font(.system(size: 12))
                    
                Text(ImMessageStatic.readableContent(messages.elements.last) ?? "")
                    .lineLimit(3)
                    .font(.system(size: 10))
            }
            Spacer()
            VStack{
                
            }
        }.padding(.init(top: 4, leading: 12, bottom: 4, trailing: 10))
    }
    
    func OverViewUserContent(_ sessions: ObservableList<Session>) -> some View {
        ZStack{
            LazyVStack{
                ForEach(sessions.elements, content: { session in
                    OverViewSingleSessionContent(session).onTapGesture {
                        onSessionClick(session)
                    }
                })
                Spacer()
            }
        }
    }
    
    func OverViewGroupContent(_ sessions: ObservableList<Session>) -> some View {
        ZStack{
            LazyVStack{
                ForEach(sessions.elements, content: { session in
                    OverViewSingleSessionContent(session).onTapGesture {
                        onSessionClick(session)
                    }
                })
                Spacer()
            }
        }
    }
    
    
    func OverViewSystemContent(_ sessions: ObservableList<Session>) -> some View {
        ZStack{
            LazyVStack{
                ForEach(sessions.elements, content: { session in
                    OverViewSingleSessionContent(session).onTapGesture {
                        onSessionClick(session)
                    }
                })
                Spacer()
            }
        }
    }
    
    func OverViewAIContent(_ sessions: ObservableList<Session>) -> some View {
        ZStack{
            LazyVStack{
                ForEach(sessions.elements, content: { session in
                    OverViewSingleSessionContent(session).onTapGesture {
                        onSessionClick(session)
                    }
                })
                Spacer()
            }
        }
    }
    
    func AddActionssSpaceContent() -> some View {
        VStack{
            if (conversationUseCase.isShowAddActions) {
                Spacer().frame(height: 68)
                VStack(spacing: 12){
                    Spacer().frame(height: 4)
                    HStack{
                        Text("Add Friend").font(.system(size: 12))
                        Spacer()
                    }.padding().background(Color(uiColor: .white)).clipShape(RoundedRectangle(cornerRadius: 12))
                    HStack{
                        Text("Add Group").font(.system(size: 12))
                        Spacer()
                    }.padding().background(Color(uiColor: .white)).clipShape(RoundedRectangle(cornerRadius: 12))
                    HStack{
                        Text("Create Group").font(.system(size: 12))
                        Spacer()
                    }.padding().background(Color(uiColor: .white)).clipShape(RoundedRectangle(cornerRadius: 12))
                    Spacer().frame(height: 4)
                }.padding()
                    .background(Color(uiColor: .separator)).clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
        .padding()
    }
    
    var body: some View {
        VStack{
            AddActionssSpaceContent()
            Spacer().frame(height: 42)
            HStack(){
                Spacer().frame(width: 12)
                ForEach(conversationUseCase.tabs, id: \.type){ tab in
                    Button(
                        action: {
                            withAnimation{
                                conversationUseCase.currentTab = tab.type
                            }
                        },
                        label: {
                            if conversationUseCase.currentTab == tab.type {
                                Text(tab.name)
                                    .tint(.white)
                                    .font(.system(size: 12))
                                    .padding(.init(top: 6, leading: 12, bottom: 6, trailing: 12))
                                    .background(Color(uiColor: .separator))
                                    .clipShape(RoundedRectangle(cornerSize: CGSize(width: 24, height: 24)))
                            }else{
                                Text(tab.name)
                                    .tint(.black)
                                    .font(.system(size: 12))
                                    .padding(.init(top: 6, leading: 12, bottom: 6, trailing: 12))
                                    .overlay(
                                        RoundedRectangle(cornerSize: CGSize(width: 24, height: 24)).stroke(Color(UIColor.separator))
                                    )
                            }
                            
                        }
                    )
                }
            }.frame(maxWidth: /*@START_MENU_TOKEN@*/.infinity/*@END_MENU_TOKEN@*/, alignment: .leading)
            
            Spacer().frame(height: 12)
            Divider()
            let sessions = conversationUseCase.currentTabSessions()
            if sessions.elements.isEmpty {
                VStack(alignment: .center){
                    Spacer()
                    Text("empty_sessions")
                    Spacer()
                }
            }else{
                ScrollView{
                    VStack{
                        switch conversationUseCase.currentTab {
                        case ConversationUseCase.GROUP:
                            OverViewGroupContent(sessions)
                            
                        case ConversationUseCase.SYSTEM:
                            OverViewSystemContent(sessions)
                            
                        case ConversationUseCase.AI:
                            OverViewAIContent(sessions)
                            
                        default:
                            OverViewUserContent(sessions)
                        }
                    }
                }
            }
            
        }.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    ConversationOverviewPage(
        conversationUseCase: ConversationUseCase(topSpaceContentUseCase: NowSpaceContentUseCase()),
        onSessionClick: { session in
            
        }
    )
}
