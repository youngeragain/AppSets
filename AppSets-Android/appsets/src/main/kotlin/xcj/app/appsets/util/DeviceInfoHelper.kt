package xcj.app.appsets.util

import android.os.Build

object DeviceInfoHelper {

    @JvmStatic
    fun provideInfo(): Map<String, String?> {
        val deviceInfo = hashMapOf<String, String?>().apply {
            put("platform", "android")
            put("screenResolution", "2210*1920")
            put("screenSize", "5.0")
            put("version", Build.VERSION.CODENAME)
            put("vendor", Build.BRAND)
            put("model", Build.DEVICE)
            put("modelCode", Build.MODEL)
            put("ip", "0.0.0.0")
        }
        return deviceInfo
    }
}