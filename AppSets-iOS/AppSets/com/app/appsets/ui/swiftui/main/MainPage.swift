//
//  ContentView.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/11.
//

import SwiftUI

struct Animal{
    public static let Dog = Animal()
    public static let Cat = Animal()
}


class AnimalHelepr{
    func injectAnimal(_ animal:Animal){
        
    }
    
    public static func doSomething(){
        //“.”符号用户快速对应类的访问静态变量
        AnimalHelepr().injectAnimal(.Dog)
    }
}

struct MainPage: View {
    
    public static let TAG = "MainPage"
    
    @EnvironmentObject var viewModel: MainViewModel
    
    @ObservedObject var navigationUseCase: NavigationUseCase
    
    @ObservedObject var nowSpaceContentUseCase: NowSpaceContentUseCase
    
    @ObservedObject var conversationUseCase: ConversationUseCase
    
    
    @State var isShowSettingsLiteDialog: Bool = false
    
    var body: some View {
        ZStack(alignment: .bottom){
            VStack{
                NowSpace()
                Content()
            }
            NavigationBar()
        }
        .sheet(isPresented: $isShowSettingsLiteDialog){
            SettingsLiteDialog()
        }
        .edgesIgnoringSafeArea(.all)
    }
    
    func getTestLinearGradient()->LinearGradient{
        return LinearGradient(gradient: Gradient(stops: [
            .init(color: .red, location: 0.0),
            .init(color: .yellow, location: 0.5),
            .init(color: .green, location: 1.0)
        ]), startPoint: .top, endPoint: .bottom)
    }
    
    func NowSpace() -> some View {
        VStack{
            switch nowSpaceContentUseCase.content {
            case is NewImMessage:
                VStack{
                    let newImMessage = nowSpaceContentUseCase.content as! NewImMessage
                    let messageColorBarVisibility = navigationUseCase.getRawRoute() != PageRouteNameProvider.ConversationDetailsPage ||
                        viewModel.conversationUseCase.currentSession?.id != newImMessage.session.id
                    if messageColorBarVisibility {
                        MessageQuickAccessBar(newImMessage)
                    }
                }
            default:
                Spacer().frame(height: 0)
            }
        }
    }
    
    func MessageQuickAccessBar(_ newImMessage:NewImMessage) -> some View {
        VStack(spacing: 12) {
            Spacer().frame(height: 68)
            HStack{
                Spacer()
                HStack(spacing: 12){
                    AsyncImage(
                        url: URL(string: newImMessage.session.imObj.avatarUrl ?? ""),
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
                    Text(newImMessage.session.imObj.name).font(.system(size: 12)).lineLimit(1)
                }
                Spacer()
            }
            Divider()
            HStack{
                Spacer()
                Text(ImMessageStatic.readableContent(newImMessage.imMessage) ?? "").font(.system(size: 13)).lineLimit(3)
                Spacer()
            }.padding(.init(top: 8, leading: 12, bottom: 20, trailing: 12))
        }
        .background(Color(UIColor.separator))
        .onTapGesture {
            nowSpaceContentUseCase.removeContent()
            viewModel.conversationUseCase.updateCurrentSessionBySession(newImMessage.session)
            navigationUseCase.navigateTo(PageRouteNameProvider.ConversationDetailsPage)
        }
    }
    
    func onBioClick(_ bio: any Bio) {
        switch bio {
        case is UserInfo:
            viewModel.userInfoUseCase.updateCurrentUserInfo(bio as! UserInfo)
            navigationUseCase.navigateTo(PageRouteNameProvider.UserProfilePage)
            
        case is ScreenInfo:
            navigationUseCase.navigateTo(PageRouteNameProvider.ScreenDetailsPage)
            
        case is Application:
            viewModel.appsUseCase.setCurrentApplication(bio as! Application)
            navigationUseCase.navigateTo(PageRouteNameProvider.AppDetailsPage)
        
        default:
            return
        }
    }
    
    func Content() -> some View {
        ZStack{
            LoginInterceptorPage(
                navigationUseCase: navigationUseCase,
                onBackClick: {
                    navigationUseCase.navigationUp(force: true)
                },
                onLoginClick: {
                    navigationUseCase.navigateTo(PageRouteNameProvider.LoginPage)
                }
            ){
                ZStack{
                    switch navigationUseCase.route {
                    
                    case PageRouteNameProvider.GroupInfoPage:
                        SettingsPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        )
                        .onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.SettingsPage:
                        SettingsPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        )
                        .onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.UserProfilePage:
                        UserProfilePage(
                            userInfoUseCase: viewModel.userInfoUseCase,
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.SignUpPage:
                        SignUpPage()
                            .onAppear {
                                withAnimation{
                                    navigationUseCase.visible = true
                                }
                            }
                        
                    case PageRouteNameProvider.LoginPage:
                        LoginPage(
                            userLoginUseCase: viewModel.userLoginUseCase,
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.AppsPage:
                        AppsCenterPage(
                            appsUseCase: viewModel.appsUseCase,
                            onBioClick: onBioClick
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = true
                            }
                        }
                        
                    case PageRouteNameProvider.AppDetailsPage:
                        AppDetailsPage(
                            application: viewModel.appsUseCase.getViewApplication(),
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            },
                            onShowApplicationCreatorClick: { uid in
                                
                            },
                            onJoinToChatClick: { application in
                                conversationUseCase.updateCurrentSessionByBio(application)
                                navigationUseCase.navigateTo(PageRouteNameProvider.ConversationDetailsPage)
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }.onDisappear{
                            viewModel.appsUseCase.setCurrentApplication(nil)
                        }
                        
                    case PageRouteNameProvider.CreateAppPage:
                        CreateAppPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.OutSidePage:
                        OutSidePage(
                            screenUseCase: viewModel.screenUseCase,
                            onBioClick : onBioClick
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = true
                            }
                        }
                        
                    case PageRouteNameProvider.ScreenDetailsPage:
                        ScreenDetailsPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.ScreenEditPage:
                        ScreenEditPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.ConversationOverviewPage:
                        ConversationOverviewPage(
                            conversationUseCase: viewModel.conversationUseCase,
                            onSessionClick: { session in
                                viewModel.conversationUseCase.updateCurrentSessionByBio(session.imObj.bio)
                                
                                navigationUseCase.navigateTo(PageRouteNameProvider.ConversationDetailsPage)
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = true
                            }
                        }
                        
                    case PageRouteNameProvider.ConversationDetailsPage:
                        ConversationDetailsPage(
                            conversationUseCase: viewModel.conversationUseCase,
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.SharePage:
                        WLANP2PPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        )
                        .onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                        
                    case PageRouteNameProvider.StartAppsPage:
                        StartAppsPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = true
                            }
                        }
                        
                    case PageRouteNameProvider.PrivacyPage:
                        PrivacyPage()
                            .onAppear {
                                withAnimation{
                                    navigationUseCase.visible = false
                                }
                            }
                        
                    case PageRouteNameProvider.SearchPage:
                        SearchPage(
                            searchUseCase: viewModel.searchUseCase,
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            },
                            onSearchClick: {
                                
                            }
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = false
                            }
                        }
                    case PageRouteNameProvider.AppToolsPage:
                        AppsToolsPage(
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = true
                            }
                        }
                    default:
                        StartPage(
                            startUseCase: viewModel.startUseCase
                        ).onAppear {
                            withAnimation{
                                navigationUseCase.visible = true
                            }
                        }
                    }
                }
            }
        }
    }
    
    func NavigationBar() -> some View {
        ZStack{
            if navigationUseCase.visible {
                VStack(alignment: .center){
                    Divider()
                    ScrollView(.horizontal){
                        HStack(spacing: 6, content: {
                            Spacer().frame(width: 6)
                            ForEach(navigationUseCase.tabItems.indices, id:\.self) { tabIndex in
                                let tabItem = navigationUseCase.tabItems[tabIndex]
                               
                                TabMain(
                                    tabItem,
                                    onTabClick: { tabAction in
                                        
                                        if tabAction == nil {
                                            navigationUseCase.onTabItemClick(tabItem: tabItem)

                                        }else{
                                            
                                            if tabAction!.route != nil {
                                                navigationUseCase.navigateTo(tabAction!.route!)
                                            }else {
                                                PurpleLogger.current.d(MainPage.TAG, "do tabAction click action")
                                                if (
                                                    tabItem.route == PageRouteNameProvider.ConversationOverviewPage
                                                ) {
                                                    viewModel.conversationUseCase.onTabActionClick(tabAction!)
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                            StandardSearchBar(
                                onClick: { tab in
                                    switch tab {
                                    case "SearchBarIcon":
                                        navigationUseCase.navigateTo(PageRouteNameProvider.SearchPage)
                                    case "UserIcon":
                                        PurpleLogger.current.d(MainPage.TAG, "NavigationBar UserIcon onClick")
                                        withAnimation{
                                            isShowSettingsLiteDialog = true
                                        }
                                    case "ShareIcon":
                                        navigationUseCase.navigateTo(PageRouteNameProvider.SharePage)
                                        
                                    default:
                                        PurpleLogger.current.d(MainPage.TAG, "NavigationBar Default onClick")
                                    }
                                }
                            )
                            Spacer().frame(width: 6)
                        })
                    }.scrollIndicators(.hidden)
                }
            }
        }
        .padding(.init(top: 0, leading: 0, bottom: 24, trailing: 0))
        .background(Color(.systemBackground))
    }
    
  
    
    func TabMain(_ tabItem: TabItem, onTabClick:@escaping (TabAction?) -> Void) -> some View {
        VStack{
            HStack{
                VStack{
                    Button(
                        action: {
                            onTabClick(nil)
                        },
                        label: {
                            SwiftUI.Image(tabItem.icon)
                                .fontWeight(.light)
                                .foregroundColor(.init(UIColor.black))
                                .padding(12)
                        }
                    )
                    .background(Circle().foregroundColor(.init(UIColor.separator)))
                    Spacer().frame(height: 4)
                    if tabItem.route == navigationUseCase.route {
                        Spacer().frame(width: 8, height: 2).background(Rectangle().foregroundColor(Color(UIColor.separator)))
                    } else{
                        Spacer().frame(width: 8, height: 2)
                    }
                }
                if(tabItem.route == navigationUseCase.route ){
                    if let actions = tabItem.tabActions {
                        VStack{
                            HStack{
                                ForEach(actions.indices, id:\.self) { index in
                                    let tabAction = actions[index]
                                    TabAction(
                                        tabAction,
                                        tabActionClick: {
                                            onTabClick(tabAction)
                                        }
                                    )
                                }
                            }
                            Spacer().frame(height: 6)
                        }
                        
                    }
                }
            }
            
        }
        
    }
    
    func TabAction(_ tabAction: TabAction, tabActionClick: @escaping () -> Void) -> some View {
        VStack{
            Button(
                action: tabActionClick,
                label: {
                    let rotation = if tabAction.action == "add_action" && conversationUseCase.isShowAddActions {
                        45.0
                    }else{
                        0.0
                    }
                    if tabAction.icon == "drawable/ic_appsets_plus"{
                        SwiftUI.Image(tabAction.icon)
                            .resizable().frame(width: 40, height: 40)
                            .rotationEffect(Angle(degrees: rotation))
        
                    }else{
                        SwiftUI.Image(tabAction.icon)
                            .fontWeight(.light)
                            .foregroundColor(.init(UIColor.black))
                            .padding(12)
                            .rotationEffect(Angle(degrees: rotation))
                         
                    }
                   
                }
            )
            .background(Circle().foregroundColor(Color(UIColor.separator)))
        }
    }
    
    func SettingsLiteDialog() -> some View {
        VStack(spacing: 12){
            HStack{
                Button(
                    action: {
                        if LocalAccountManager.Instance.isLogged() {
                            viewModel.userLoginUseCase.logout(LocalContext.current)
                        }else{
                            withAnimation{
                                isShowSettingsLiteDialog = false
                            }
                            navigationUseCase.navigateTo(PageRouteNameProvider.LoginPage)
                        }
                       
                    },
                    label: {
                        let text = if LocalAccountManager.Instance.isLogged() {
                            "logout"
                        }else{
                            "login"
                        }
                        Text(text)
                            .tint(.white)
                    }
                )
                .padding()
                .background(RoundedRectangle(cornerRadius: 12).foregroundColor(.blue))
                Spacer()
            }
            Spacer()
        }
        .padding()
    }
}



#Preview {
    VStack{
        let viewModel = MainViewModel()
        MainPage(
            navigationUseCase: viewModel.navigationUseCase,
            nowSpaceContentUseCase: viewModel.nowSpaceContentUseCase,
            conversationUseCase: viewModel.conversationUseCase
        ).environmentObject(viewModel)
    }.onAppear(perform: {
        LocalContext.provide(t: ContextImpl())
    })
}
