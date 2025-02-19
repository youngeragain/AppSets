package xcj.app.share.mock

import xcj.app.share.base.DeviceName
import xcj.app.share.base.ShareMethod
import xcj.app.share.ui.compose.AppSetsShareActivity
import xcj.app.share.ui.compose.AppSetsShareViewModel
import xcj.app.starter.android.util.PurpleLogger

class MockShareMethod : ShareMethod() {
    companion object {
        private const val TAG = "MockShareMethod"
    }

    override var mDeviceName: DeviceName = DeviceName.RANDOM

    override fun init(activity: AppSetsShareActivity, viewModel: AppSetsShareViewModel) {
        super.init(activity, viewModel)
        PurpleLogger.current.d(TAG, "init")
    }

    override fun destroy() {
        super.destroy()
        PurpleLogger.current.d(TAG, "destroy")
    }
}