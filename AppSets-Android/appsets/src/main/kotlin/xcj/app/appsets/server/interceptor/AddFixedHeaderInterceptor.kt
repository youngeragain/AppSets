package xcj.app.appsets.server.interceptor

import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.server.ApiDesignKeys

class AddFixedHeaderInterceptor : okhttp3.Interceptor {
    companion object {
        private const val TAG = "AddFixedHeaderInterceptor"
    }

    override fun intercept(chain: okhttp3.Interceptor.Chain): okhttp3.Response {
        PurpleLogger.current.d(TAG, "intercept")
        try {
            val requestBuilder = chain.request()
                .newBuilder()
                .header(ApiDesignKeys.VERSION_MD5, "300")
                .header(ApiDesignKeys.PLATFORM_MD5, "android")
                .header(
                    ApiDesignKeys.APPSETS_ID_MD5,
                    xcj.app.appsets.settings.ModuleConfig.moduleConfiguration.appsetsAppId
                )
            val token = xcj.app.appsets.account.LocalAccountManager.token
            if (!token.isNullOrEmpty()) {
                requestBuilder.header(ApiDesignKeys.TOKEN_MD5, token)
            }
            val appToken = xcj.app.appsets.account.LocalAccountManager.provideAppToken()
            if (!appToken.isNullOrEmpty()) {
                requestBuilder.header(ApiDesignKeys.APP_TOKEN_MD5, appToken)
            }
            val newRequest = requestBuilder.build()
            return chain.proceed(newRequest)
        } catch (e: IllegalArgumentException) {
            PurpleLogger.current.d(
                TAG,
                "intercept exception:${e.message}"
            )
        }
        return chain.proceed(chain.request())
    }
}