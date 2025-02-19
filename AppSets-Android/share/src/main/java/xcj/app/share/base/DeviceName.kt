package xcj.app.share.base

import xcj.app.share.util.NameGenerator
import xcj.app.starter.util.VendorUtil

data class DeviceName(
    var rawName: String,
    var nikeName: String? = null
) {
    val name: String
        get() = nikeName ?: rawName

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