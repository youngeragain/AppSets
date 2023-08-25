package xcj.app.appsets.server.repository

import retrofit2.awaitResponse
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.util.DeviceInfoHelper
import xcj.app.core.foundation.http.DesignResponse

//inject userApi
//此类无需提供单例
class UserLoginRepository(private val userApi: UserApi) {
    suspend fun login(account: String, password: String): DesignResponse<String?> {
        return userApi.login(
            hashMapOf(
                "account" to account,
                "password" to password,
                "signInDeviceInfo" to DeviceInfoHelper.provideInfo(),
                "signInLocation" to "Si Chuan"
            )
        )
    }

    suspend fun login2(): DesignResponse<String?> {
        return userApi.login2(
            hashMapOf(
                "account" to "account",
                "password" to "password",
                "signInDeviceInfo" to DeviceInfoHelper.provideInfo(),
                "signInLocation" to "Si Chuan"
            )
        )
    }

    suspend fun loginCall(account: String, password: String): DesignResponse<String?> {
        val loginCall = userApi.loginCall(hashMapOf("account" to account, "password" to password))
        val awaitResponse = loginCall.awaitResponse().body()
        return DesignResponse(data = awaitResponse)
    }

    suspend fun signUp(
        account: String,
        password: String,
        userName: String,
        userAvatarUrlMarker: String?,
        userIntroduction: String,
        userTags: String,
        userSex: String,
        userAge: String,
        userPhone: String,
        userEmail: String,
        userArea: String,
        userAddress: String,
        userWebsite: String
    ): DesignResponse<Boolean> {
        return userApi.signUp(
            hashMapOf<String, Any?>(
                "account" to account,
                "password" to password
            ).apply {
                if (userName.isNotEmpty()) {
                    put("name", userName)
                }
                if (!userAvatarUrlMarker.isNullOrEmpty()) {
                    put("avatarUrl", userAvatarUrlMarker)
                }
                if (userIntroduction.isNotEmpty()) {
                    put("introduction", userIntroduction)
                }
                if (userTags.isNotEmpty()) {
                    put("tags", userTags)
                }
                if (userSex.isNotEmpty()) {
                    put("sex", userSex)
                }
                if (userAge.isNotEmpty()) {
                    put("age", userAge.toIntOrNull() ?: 0)
                }
                if (userPhone.isNotEmpty()) {
                    put("phone", userPhone)
                }
                if (userEmail.isNotEmpty()) {
                    put("email", userEmail)
                }
                if (userArea.isNotEmpty()) {
                    put("area", userArea)
                }
                if (userAddress.isNotEmpty()) {
                    put("address", userAddress)
                }
                if (userWebsite.isNotEmpty()) {
                    put("website", userWebsite)
                }
            }
        )
    }

    suspend fun signOut(): DesignResponse<Boolean> {
        return userApi.signOut()
    }

    suspend fun preSignUp(account: String): DesignResponse<Boolean> {
        return userApi.preSignUp(account)
    }
}