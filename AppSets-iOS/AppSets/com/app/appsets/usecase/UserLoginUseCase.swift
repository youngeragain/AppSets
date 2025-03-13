//
//  UserLoginUseCase .swift
//  AppSets
//
//  Created by caiju Xu on 2024/7/4.
//

import Foundation

@Observable
class UserLoginUserCase {
    
    private static let TAG = "UserLoginUserCase"
    
    func login(
        context: Context,
        account: String,
        password: String
    ) {
        PurpleLogger.current.d(UserLoginUserCase.TAG, "login, account:\(account), password:\(password)")
        Task{
            let userLoginRepository = UserLoginRepository()
            guard let token = await userLoginRepository.login(account: account, password: password) else {
                PurpleLogger.current.d(UserLoginUserCase.TAG, "login, failed, return")
                return
            }
            
            PurpleLogger.current.d(UserLoginUserCase.TAG, "login, success")
            LocalAccountManager.Instance.onUserLogged(userInfo: UserInfo.defaultUser(), token: token, isTemp: true)
            
            let userRepository = UserRepository()
            
            guard let userInfo = await userRepository.getLoggedUseInfo() else{
                PurpleLogger.current.d(UserLoginUserCase.TAG, "login, get userInfo return null, return")
                return
            }
            
            LocalAccountManager.Instance.onUserLogged(userInfo: userInfo, token: token, isTemp: false)
            
        }
    }
    
    func logout(_ context: Context) {
        PurpleLogger.current.d(UserLoginUserCase.TAG, "logout")
        LocalAccountManager.Instance.onUserLogout(by: LocalAccountManager.LOGOUT_BY_MANUALLY)
    }
    
}
