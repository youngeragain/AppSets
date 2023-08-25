package xcj.app.userinfo.model.req

import jakarta.validation.constraints.NotNull

data class DeviceInfo(
    @get: NotNull(message = "platform is required")
    val platform:String?,
    val screenResolution:String?,
    val screenSize:String?,
    val version:String?,
    val vendor:String?,
    val model:String?,
    val modelCode:String?,
    val ip:String?
) {
    val allEmpty: Boolean
    get() {
        return platform.isNullOrEmpty() &&
                version.isNullOrEmpty() &&
                vendor.isNullOrEmpty() &&
                model.isNullOrEmpty() &&
                modelCode.isNullOrEmpty() &&
                ip.isNullOrEmpty()
    }
}