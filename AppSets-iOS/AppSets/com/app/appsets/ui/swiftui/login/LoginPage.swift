//
//  LoginPage.swift
//  AppSets
//
//  Created by caiju Xu on 2023/12/25.
//

import SwiftUI

struct LoginPage: View {
    
    @ObservedObject var userLoginUseCase: UserLoginUserCase
    
    @State private var account:String = ""
    
    @State private var password:String = ""
    
    let onBackClickListener: () -> Void
    
    let context: Context = LocalContext.current
    
    var body: some View {
        VStack(alignment: .leading){
            Spacer().frame(height: 24)
            HStack(alignment: .top, spacing: 12){
                Text("sign_up")
                    .tint(.white)
                    .padding([.leading, .trailing], 8)
                    .padding([.top, .bottom], 8)
                Text("login_with_qrcode")
                    .tint(.white)
                    .padding([.leading, .trailing], 8)
                    .padding([.top, .bottom], 8)
                
                Text("scan_qrcode")
                    .tint(.white)
                    .padding([.leading, .trailing], 8)
                    .padding([.top, .bottom], 8)
               
            }
            
            Spacer().frame(height: 12)
            
            VStack(alignment: .leading, spacing: 12){
                Text("login").fontWeight(.bold).font(.system(size: 120))
                Spacer()
                TextField(text: $account, label: {
                    Text("account")
                })
                .padding([.all], 16)
                    .cornerRadius(12) /// make the background rounded
                    .overlay( /// apply a rounded border
                        RoundedRectangle(cornerRadius: 32)
                            .stroke(.gray, lineWidth: 1)
                    )
                TextField(text: $password, label: {
                    Text("password")
                })
                .padding([.all], 16)
                    .cornerRadius(12) /// make the background rounded
                    .overlay( /// apply a rounded border
                        RoundedRectangle(cornerRadius: 32)
                            .stroke(.gray, lineWidth: 1)
                    )
                Button(action: {
                    userLoginUseCase.login(
                        context: context,
                        account: account,
                        password: password
                    )
                }, label: {
                    Text("ok")
                        .frame(width: .infinity)
                        .tint(.white)
                        .padding([.leading, .trailing], 16)
                        .padding([.top, .bottom], 12)
                        .background(RoundedRectangle(cornerSize: CGSize(width: 32, height: 32)).foregroundColor(.blue))
                        .cornerRadius(32) /// make the background rounded
                }).frame(width: .infinity)
                    
                    
            }
            Spacer()
            HStack{
                Spacer()
                Button(
                    action: onBackClickListener
                    ,
                    label: {
                        SwiftUI.Image("drawable/arrow_back_ios_arrow_back_ios_symbol")
                            .tint(.white)
                            .padding([.leading, .trailing], 16)
                            .padding([.top, .bottom], 14)
                            .background(Circle().foregroundColor(.blue))
                        .cornerRadius(12) /// make the background rounded
                    }
                )
                .frame(alignment: /*@START_MENU_TOKEN@*/.center/*@END_MENU_TOKEN@*/)
                Spacer()
            }
        }.safeAreaPadding().padding()
    }
    
    init(userLoginUseCase: UserLoginUserCase, onBackClick: @escaping () -> Void) {
        self.userLoginUseCase = userLoginUseCase
        self.onBackClickListener = onBackClick
    }
}

#Preview {
    LoginPage(
        userLoginUseCase: UserLoginUserCase(),
        onBackClick: {
        
        }
    )
}
