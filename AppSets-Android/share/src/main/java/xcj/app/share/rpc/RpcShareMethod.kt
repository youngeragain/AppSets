package xcj.app.share.rpc

import xcj.app.share.base.ShareDevice
import xcj.app.share.base.ShareMethod
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.share.ui.compose.AppSetsShareViewModel
import xcj.app.starter.android.util.PurpleLogger

class RpcShareMethod : ShareMethod() {
    companion object {
        private const val TAG = "RpcShareMethod"
        const val NAME = "RPC"
    }

    override fun init(activity: AppSetsShareActivity, viewModel: AppSetsShareViewModel) {
        super.init(activity, viewModel)
        PurpleLogger.current.d(TAG, "init")
        val shareDevice =
            ShareDevice.RpcShareDevice(deviceName = mDeviceName)
        viewModel.updateShareDeviceState(shareDevice)
    }

    override fun destroy() {
        super.destroy()
        PurpleLogger.current.d(TAG, "destroy")
    }
}