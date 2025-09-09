package xcj.app.starter.util

import android.annotation.SuppressLint
import android.os.Build

object VendorUtil {

    @JvmStatic
    fun getVendorDeviceName(): String {
        if (isMi()) {
            return getSystemProperty("persist.sys.device_name") ?: Build.MODEL
        }
        return Build.MODEL
    }

    @JvmStatic
    fun isMi(): Boolean {
        return !getSystemProperty("ro.miui.ui.version.name").isNullOrEmpty()
    }

    @JvmStatic
    fun isGoogle(): Boolean {
        return getSystemProperty("ro.product.manufacturer") == "Google"
    }

    @SuppressLint("PrivateApi")
    @JvmStatic
    private fun getSystemProperty(propName: String): String? {
        try {
            val systemPropertiesClass = Class.forName("android.os.SystemProperties")
            val getMethod = systemPropertiesClass.getMethod("get", String::class.java)
            getMethod.isAccessible = true
            return getMethod.invoke(systemPropertiesClass, propName)?.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}