package xcj.app.appsets.server.ssl

import android.annotation.SuppressLint
import java.security.cert.X509Certificate
import javax.net.ssl.X509TrustManager

@SuppressLint("TrustAllX509TrustManager", "CustomX509TrustManager")
class DesignX509TrustManager : X509TrustManager {

    override fun checkClientTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?
    ) {

    }


    override fun checkServerTrusted(
        chain: Array<out X509Certificate>?,
        authType: String?
    ) {

    }

    override fun getAcceptedIssuers(): Array<X509Certificate> {
        return arrayOf()
    }
}