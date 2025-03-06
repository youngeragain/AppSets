package xcj.app.share.base

data class BasicShareDevice(
    override var deviceAddress: DeviceAddress = DeviceAddress.NONE,
    override var deviceName: DeviceName = DeviceName.NONE,
    override var deviceType: Int = ShareDevice.DEVICE_TYPE_PHONE
) : ShareDevice