package xcj.app.appsets.server.ssl

import okhttp3.internal.tls.OkHostnameVerifier
import xcj.app.appsets.settings.ModuleConfig
import xcj.app.starter.android.util.PurpleLogger
import javax.net.ssl.HostnameVerifier
import javax.net.ssl.SSLSession

class DesignHostnameVerifier : HostnameVerifier {
    companion object {
        private const val TAG = "DesignHostnameVerifier"
    }

    override fun verify(hostname: String, session: SSLSession): Boolean {
        PurpleLogger.current.d(TAG, "hostnameVerifier:${hostname}")
        if (ModuleConfig.moduleConfiguration.apiUrl.isEmpty()) {
            if (hostname == ModuleConfig.moduleConfiguration.apiHost) {
                return true
            }
        } else {
            if (hostname == ModuleConfig.moduleConfiguration.apiUrl) {
                return true
            }
        }

        return OkHostnameVerifier.verify(hostname, session)
    }
}