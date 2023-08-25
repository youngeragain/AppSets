package xcj.app.appsets.server.api

import xcj.app.appsets.BuildConfig

interface URLApi {
    companion object {
        private fun <U : URLApi> getUrl(apiClazz: Class<U>): String {
            val protocol = if (BuildConfig.SSLEnable) {
                "https"
            } else {
                "http"
            }
            val url = "${protocol}://${BuildConfig.ApiHostAddress}:${BuildConfig.ApiPort}/"
            return when (apiClazz) {
                UserApi::class.java -> {
                    url
                }

                AppSetsApi::class.java -> {
                    url
                }

                QRCodeApi::class.java -> {
                    url
                }

                SearchApi::class.java -> {
                    url
                }

                ThirdPartApi1::class.java -> {
                    url
                }

                else -> throw Exception("can't provide an api")
            }
        }

        fun urlHook(rawBaseUrl: String, rawFullUrl: String): String {
            return rawBaseUrl
        }

        fun <U : URLApi> provide(apiClazz: Class<U>): U {
            return xcj.app.appsets.server.RetrofitProvider.getService(
                getUrl(apiClazz),
                apiClazz
            )
        }
    }
}