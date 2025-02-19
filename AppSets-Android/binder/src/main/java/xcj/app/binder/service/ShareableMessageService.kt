package xcj.app.binder.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import xcj.app.starter.android.util.LocalMessager
import xcj.app.starter.android.util.PurpleLogger
import xcj.external.binder.OnMessageInterface

class ShareableMessageService : Service() {
    companion object {
        private const val TAG = "ShareableMessageService"
    }

    override fun onCreate() {
        super.onCreate()
        PurpleLogger.current.d(TAG, "onCreate")
    }

    override fun onBind(intent: Intent?): IBinder? {
        LocalMessager.post("Service_Connect_State", "connected!")
        val asBinder = NoDefault().asBinder()
        return asBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        LocalMessager.post("Service_Connect_State", "not connected!")
        return super.onUnbind(intent)
    }

}

class NoDefault : OnMessageInterface.Stub() {
    companion object{
        private const val TAG = "NoDefault"
    }
    override fun basicTypes(
        anInt: Int,
        aLong: Long,
        aBoolean: Boolean,
        aFloat: Float,
        aDouble: Double,
        aString: String?
    ) {

    }

    override fun showNumber(number: Int) {
        PurpleLogger.current.d(TAG, "showNumber a number from remote:${number}")
    }

    override fun showMessage(message: String?) {
        LocalMessager.post("Message_From_Remote", message)
    }

    override fun getMessage(): String {
        PurpleLogger.current.d(TAG, "getMessage message request from remote!")
        return "remote message:" + (0..1000).random() + "time:" + System.currentTimeMillis()
    }

    override fun asBinder(): IBinder {
        return this
    }

}