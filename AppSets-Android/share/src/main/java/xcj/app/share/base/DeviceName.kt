package xcj.app.share.base

import xcj.app.share.util.NameGenerator
import xcj.app.starter.util.VendorUtil

data class DeviceName(
    var rawName: String,
    var nickName: String? = null
) {
    val name: String
        get() = nickName ?: rawName

    companion object {
        val NONE: DeviceName
            get() = DeviceName("", null)

        val RANDOM: DeviceName
            get() {
                val rawName = VendorUtil.getVendorDeviceName()
                return DeviceName(rawName, NameGenerator.randomNikeName())
            }
    }
}