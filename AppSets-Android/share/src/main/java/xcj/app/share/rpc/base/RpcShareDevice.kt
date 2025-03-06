package xcj.app.share.rpc.base

import xcj.app.share.base.DeviceAddress
import xcj.app.share.base.DeviceName
import xcj.app.share.base.ShareDevice

data class RpcShareDevice(
    override var deviceName: DeviceName,
    override var deviceAddress: DeviceAddress = DeviceAddress.NONE,
    override var deviceType: Int = ShareDevice.DEVICE_TYPE_PHONE
) : ShareDevice