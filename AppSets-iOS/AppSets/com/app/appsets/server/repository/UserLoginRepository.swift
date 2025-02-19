//
//  UserLoginRepository.swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/9.
//

import Foundation

class UserLoginRepository {
    
    private static let TAG = "UserLoginRepository"
    
    private let userApi:UserApi = UserApiImpl.Instance
    
    func login(account:String, password:String) async -> String? {
        let encodedAccount = MD5Helper.transform(account).outContent
        let encodedPassword = MD5Helper.transform(password).outContent
        
        PurpleLogger.current.d(UserLoginRepository.TAG, "encodedAccount:\(encodedAccount), encodedPassword:\(encodedPassword)")
        
        let response = await userApi.login(account: encodedAccount, password: encodedPassword)
        let token = response.data
        
        PurpleLogger.current.d(UserLoginRepository.TAG, "login, token is:\((token ?? ""))")
        
        if String.isNullOrEmpty(token) {
            PurpleLogger.current.d(UserLoginRepository.TAG, "login, token isNullOrEmpty")
            return nil
        }
       
        return token!
    }
    
}
