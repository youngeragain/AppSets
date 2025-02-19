package xcj.app.appsets.server.ssl

import okhttp3.internal.tls.OkHostnameVerifier
import xcj.app.appsets.settings.AppConfig
import xcj.app.starter.android.util.PurpleLogger
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class DesignHostnameVerifier : HostnameVerifier {
    companion object {
        private const val TAG = "DesignHostnameVerifier"
    }

    override fun verify(hostname: String, session: SSLSession): Boolean {
        PurpleLogger.current.d(TAG, "hostnameVerifier:${hostname}")
        if (AppConfig.appConfiguration.apiUrl.isEmpty()) {
            if (hostname == AppConfig.appConfiguration.apiHost) {
                return true
            }
        } else {
            if (hostname == AppConfig.appConfiguration.apiUrl) {
                return true
            }
        }

        return OkHostnameVerifier.verify(hostname, session)
    }
}