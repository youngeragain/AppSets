//
//  ContentView.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/11.
//

import SwiftUI

struct MainPage: View {
    public static let TAG = "MainPage"

    @Environment(MainViewModel.self) var viewModel: MainViewModel

    @State var isShowSettingsLiteDialog: Bool = false

    var brokerTest: BrokerTest = BrokerTest.Instance

    var body: some View {
        ZStack(alignment: .bottom) {
            VStack {
                NowSpace()
                Content()
            }
            NavigationBar()
        }
        .background(Theme.colorSchema.background)
        .sheet(isPresented: $isShowSettingsLiteDialog) {
            SettingsLiteDialog().presentationDetents([.medium])
        }
        .edgesIgnoringSafeArea(.all)
    }

    func getTestLinearGradient() -> LinearGradient {
        return LinearGradient(gradient: Gradient(stops: [
            .init(color: .red, location: 0.0),
            .init(color: .yellow, location: 0.5),
            .init(color: .green, location: 1.0),
        ]), startPoint: .top, endPoint: .bottom)
    }

    func NowSpace() -> some View {
        VStack {
            let nowSpaceContentUseCase = viewModel.nowSpaceContentUseCase
            let navigationUseCase = viewModel.navigationUseCase
            switch nowSpaceContentUseCase.content {
            case is NewImMessage:
                VStack {
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

    func MessageQuickAccessBar(_ newImMessage: NewImMessage) -> some View {
        VStack {
            let nowSpaceContentUseCase = viewModel.nowSpaceContentUseCase
            let navigationUseCase = viewModel.navigationUseCase
            VStack(spacing: 12) {
                Spacer().frame(height: 68)
                HStack {
                    Spacer()
                    HStack(spacing: 12) {
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
                HStack {
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
    }

    func onBioClick(_ bio: any Bio) {
        let navigationUseCase = viewModel.navigationUseCase
        switch bio {
        case is UserInfo:
            let userInfo = bio as! UserInfo
            viewModel.userInfoUseCase.updateCurrentUserInfo(userInfo)
            navigationUseCase.navigateTo(PageRouteNameProvider.UserProfilePage)

        case is ScreenInfo:
            navigationUseCase.navigateTo(PageRouteNameProvider.ScreenDetailsPage)

        case is Application:
            let app = bio as! Application
            viewModel.appsUseCase.setViewApplication(app)
            navigationUseCase.navigateTo(PageRouteNameProvider.AppDetailsPage)

        default:
            return
        }
    }

    func Content() -> some View {
        ZStack {
            let navigationUseCase = viewModel.navigationUseCase
            let conversationUseCase = viewModel.conversationUseCase
            LoginInterceptorPage(
                navigationUseCase: navigationUseCase,
                onBackClick: {
                    navigationUseCase.navigationUp(force: true)
                },
                onLoginClick: {
                    navigationUseCase.navigateTo(PageRouteNameProvider.LoginPage)
                }
            ) {
                ZStack {
                    switch navigationUseCase.route {
                    case PageRouteNameProvider.GroupInfoPage:
                        SettingsPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        )
                        .onAppear {
                            withAnimation {
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
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }

                    case PageRouteNameProvider.UserProfilePage:
                        UserProfilePage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }

                    case PageRouteNameProvider.SignUpPage:
                        SignUpPage()
                            .onAppear {
                                withAnimation {
                                    navigationUseCase.visible = true
                                }
                            }

                    case PageRouteNameProvider.LoginPage:
                        LoginPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }

                    case PageRouteNameProvider.AppsCenterPage:
                        AppsCenterPage(
                            onBioClick: onBioClick
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = true
                            }
                        }

                    case PageRouteNameProvider.AppDetailsPage:
                        AppDetailsPage(
                            application: viewModel.appsUseCase.getViewApplication(),
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            },
                            onShowApplicationCreatorClick: { _ in

                            },
                            onJoinToChatClick: { application in
                                conversationUseCase.updateCurrentSessionByBio(application)
                                navigationUseCase.navigateTo(PageRouteNameProvider.ConversationDetailsPage)
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }.onDisappear {
                            viewModel.appsUseCase.setViewApplication(nil)
                        }

                    case PageRouteNameProvider.CreateAppPage:
                        CreateAppPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }

                    case PageRouteNameProvider.OutSidePage:
                        OutSidePage(
                            onBioClick: onBioClick
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = true
                            }
                        }

                    case PageRouteNameProvider.ScreenDetailsPage:
                        ScreenDetailsPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }

                    case PageRouteNameProvider.ScreenEditPage:
                        ScreenEditPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }

                    case PageRouteNameProvider.ConversationOverviewPage:
                        ConversationOverviewPage(
                            onSessionClick: { session in
                                viewModel.conversationUseCase.updateCurrentSessionByBio(session.imObj.bio)

                                navigationUseCase.navigateTo(PageRouteNameProvider.ConversationDetailsPage)
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = true
                            }
                        }

                    case PageRouteNameProvider.ConversationDetailsPage:
                        ConversationDetailsPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }

                    case PageRouteNameProvider.SharePage:
                        SharePage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            }
                        )
                        .onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }

                    case PageRouteNameProvider.PrivacyPage:
                        PrivacyPage()
                            .onAppear {
                                withAnimation {
                                    navigationUseCase.visible = false
                                }
                            }

                    case PageRouteNameProvider.SearchPage:
                        SearchPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            },
                            onSearchClick: {
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }
                    case PageRouteNameProvider.AppToolsPage:
                        AppsToolsPage(
                            onBackClick: {
                                navigationUseCase.navigationUp()
                            },
                            onAppToolClick: { type in
                                if type == AppsToolsPage.TOOL_TYPE_APPSETS_SHARE {
                                    navigationUseCase.navigateTo(PageRouteNameProvider.SharePage)
                                }
                            }
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = false
                            }
                        }
                    default:
                        StartPage(
                        ).onAppear {
                            withAnimation {
                                navigationUseCase.visible = true
                            }
                        }
                    }
                }
            }
        }
    }

    func NavigationBar() -> some View {
        ZStack {
            let navigationUseCase = viewModel.navigationUseCase

            if navigationUseCase.visible {
                VStack(alignment: .center) {
                    Divider().foregroundColor(Theme.colorSchema.outline)
                    ScrollView(.horizontal) {
                        HStack(spacing: 6, content: {
                            Spacer().frame(width: 6)
                            ForEach(navigationUseCase.tabItems.indices, id: \.self) { tabIndex in
                                let tabItem = navigationUseCase.tabItems[tabIndex]

                                TabMain(
                                    tabItem,
                                    onTabClick: { tabAction in

                                        if tabAction == nil {
                                            navigationUseCase.onTabItemClick(tabItem: tabItem)

                                        } else {
                                            if tabAction!.route != nil {
                                                navigationUseCase.navigateTo(tabAction!.route!)
                                            } else {
                                                PurpleLogger.current.d(MainPage.TAG, "do tabAction click action")
                                                if
                                                    tabItem.route == PageRouteNameProvider.ConversationOverviewPage
                                                {
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
                                        withAnimation {
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
        .background(Theme.colorSchema.background)
    }

    func TabMain(_ tabItem: TabItem, onTabClick: @escaping (TabAction?) -> Void) -> some View {
        VStack {
            let navigationUseCase = viewModel.navigationUseCase
            HStack {
                VStack {
                    Button(
                        action: {
                            onTabClick(nil)
                        },
                        label: {
                            SwiftUI.Image(tabItem.icon)
                                .resizable()
                                .scaledToFit()
                                .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                                .fontWeight(.light)
                                .tint(Theme.colorSchema.onSurface)
                        }
                    )
                    .padding(12)
                    .background(Theme.colorSchema.outline.clipShape(Circle()))
                    Spacer().frame(height: 4)
                    if tabItem.route == navigationUseCase.route {
                        Spacer().frame(width: 8, height: 2).background(Theme.colorSchema.secondary.frame(width: 8, height: 2))
                    } else {
                        Spacer().frame(width: 8, height: 2)
                    }
                }
                if tabItem.route == navigationUseCase.route {
                    if let actions = tabItem.tabActions {
                        VStack {
                            HStack {
                                ForEach(actions.indices, id: \.self) { index in
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
        VStack {
            let conversationUseCase = viewModel.conversationUseCase
            Button(
                action: tabActionClick,
                label: {
                    if tabAction.action == "add_action" {
                        let rotation = if conversationUseCase.isShowAddActions {
                            45.0
                        } else {
                            0.0
                        }
                        SwiftUI.Image(tabAction.icon)
                            .resizable()
                            .scaledToFit()
                            .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                            .fontWeight(.light)
                            .tint(Theme.colorSchema.onSurface)
                            .rotationEffect(Angle(degrees: rotation))

                    } else {
                        SwiftUI.Image(tabAction.icon)
                            .resizable()
                            .scaledToFit()
                            .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                            .fontWeight(.light)
                            .tint(Theme.colorSchema.onSurface)
                    }
                }
            )
            .padding(12)
            .background(Theme.colorSchema.outline.clipShape(Circle()))
        }
    }

    func SettingsLiteDialog() -> some View {
        VStack(alignment: .leading, spacing: 24) {
            if LocalAccountManager.Instance.isLogged() {
                HStack(spacing: 12) {
                    let borderColor = if brokerTest.isOnline {
                        Color.green
                    } else {
                        Color.red
                    }
                    AsyncImage(
                        url: URL(string: LocalAccountManager.Instance.userInfo.avatarUrl ?? ""),
                        content: { image in
                            image
                                .resizable()
                                .scaledToFit()
                                .frame(width: (Theme.size.iconSizeNormal * 2) - 2, height: (Theme.size.iconSizeNormal * 2) - 2)
                                .clipShape(Circle())
                        },
                        placeholder: {
                            SwiftUI.Image(.Drawable.faceFaceSymbol)
                                .resizable()
                                .scaledToFit()
                                .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                                .padding(11)
                                .fontWeight(.light)
                                .tint(Theme.colorSchema.onSurface)
                        }
                    ).overlay {
                        Circle().stroke(borderColor, lineWidth: 2)
                    }

                    Text(LocalAccountManager.Instance.userInfo.name ?? "")
                }

            } else {
                HStack(spacing: 12) {
                    SwiftUI.Image(.Drawable.faceFaceSymbol)
                        .resizable()
                        .scaledToFit()
                        .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                        .fontWeight(.light)
                        .tint(Theme.colorSchema.onSurface)
                        .padding(12)
                        .background(Theme.colorSchema.outline.clipShape(Circle()))

                    Text("Login to AppSets")
                }
            }

            HStack(spacing: 12) {
                SwiftUI.Image(.Drawable.settingsSettingsSymbol)
                    .resizable()
                    .scaledToFit()
                    .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                    .fontWeight(.light)
                    .tint(Theme.colorSchema.onSurface)
                    .padding(12)
                    .background(Theme.colorSchema.outline.clipShape(Circle()))
                Text("App Settings")
            }
            let navigationUseCase = viewModel.navigationUseCase
            HStack(spacing: 12) {
                HStack {}
                    .frame(width: Theme.size.iconSizeNormal, height: Theme.size.iconSizeNormal)
                    .padding(12)

                let text = if LocalAccountManager.Instance.isLogged() {
                    "logout"
                } else {
                    "login"
                }
                Text(text)
                    .onTapGesture {
                        if LocalAccountManager.Instance.isLogged() {
                            viewModel.userLoginUseCase.logout(LocalContext.current)
                        } else {
                            withAnimation {
                                isShowSettingsLiteDialog = false
                            }
                            navigationUseCase.navigateTo(PageRouteNameProvider.LoginPage)
                        }
                    }
                Spacer()
            }
            Spacer()
        }
        .padding(32)
    }
}

#Preview {
    VStack {
        let viewModel = MainViewModel()
        MainPage().environment(viewModel)
    }.onAppear(perform: {
        LocalContext.provide(t: ContextImpl())
    })
}
