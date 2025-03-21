package xcj.app.share.base

interface ShareDevice {
    companion object {
        const val DEVICE_TYPE = "deviceType"
        const val RAW_NAME = "rawName"
        const val NICK_NAME = "nickName"
        const val DEVICE_TYPE_PHONE = 0
        const val DEVICE_TYPE_TABLET = 1
        const val DEVICE_TYPE_COMPUTER = 2
        const val DEVICE_TYPE_TV = 3
        const val DEVICE_TYPE_WEB_DEVICE = 4

        const val CLICK_TYPE_NORMAL = 0
        const val CLICK_TYPE_LONG = 1
        const val CLICK_TYPE_DOUBLE = 2
    }

    var deviceAddress: DeviceAddress
    var deviceName: DeviceName
    var deviceType: Int
}