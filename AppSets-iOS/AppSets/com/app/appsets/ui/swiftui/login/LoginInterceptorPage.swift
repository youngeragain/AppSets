//
//  LoginInterceptorPage.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/11.
//

import SwiftUI

struct LoginInterceptorPage<V:View>: View {
    
    let navigationUseCase: NavigationUseCase
    
    let onBackClickListener: () -> Void
    
    let onLoginClickListener: () -> Void
    
    @ViewBuilder let content: ()-> V
    
    var body: some View {
        VStack {
            if !LocalAccountManager.Instance.isLogged() && LocalPageRouteNameNeedLoggedProvider.current.contains(navigationUseCase.route) {
                LoginInterceptorConent().onAppear(perform: {
                    withAnimation{
                        navigationUseCase.visible = false
                    }
                })
            }else{
                content()
            }
        }.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
    }
    
    init(
        navigationUseCase: NavigationUseCase,
        onBackClick: @escaping () -> Void,
        onLoginClick: @escaping () -> Void,
        conetnt contentViewBuilder: @escaping () -> V
    ) {
        self.navigationUseCase = navigationUseCase
        self.onBackClickListener = onBackClick
        self.onLoginClickListener = onLoginClick
        self.content = contentViewBuilder
    }
    
    func LoginInterceptorConent() -> some View {
        VStack{
            HStack{
                Spacer()
                VStack(spacing: 24){
                    Spacer()
                    Button(
                        action: onLoginClickListener,
                        label: {
                            Text("Require Login to AppSets").tint(Theme.colorSchema.primary)
                        }
                    )
                    Spacer()
                    Button(
                        action: onBackClickListener,
                        label: {
                            SwiftUI.Image("drawable/arrow_back_ios_arrow_back_ios_symbol")
                                .tint(Theme.colorSchema.onSurface)
                                .padding([.leading, .trailing], 16)
                                .padding([.top, .bottom], 14)
                                .background(Circle().foregroundColor(Theme.colorSchema.secondaryContainer))
                            .cornerRadius(12) /// make the background rounded
                        }
                    )
                    Spacer().frame(height: 32)
                    
                }
                .frame(alignment: /*@START_MENU_TOKEN@*/.center/*@END_MENU_TOKEN@*/)
                Spacer()
            }
        }.frame(minWidth: 0, maxWidth: .infinity, minHeight: 0, maxHeight: .infinity)
            .edgesIgnoringSafeArea(.all)
    }
}

#Preview {
    LoginInterceptorPage(
        navigationUseCase: NavigationUseCase(),
        onBackClick: {
        
        },
        onLoginClick: {
            
        }
    ){
        Text("Real Content")
    }

}
