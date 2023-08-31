package xcj.app.binder

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import xcj.app.core.android.DesignMessageDeliver
import xcj.external.binder.OnMessageInterface

class ShareableMessageService:Service() {
    private val TAG = "ShareableMessageService"
    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "onCreate")
    }
    override fun onBind(intent: Intent?): IBinder? {
        DesignMessageDeliver.post("Service_Connect_State", "connected!")
        val asBinder = NoDefault().asBinder()
        return asBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        DesignMessageDeliver.post("Service_Connect_State", "not connected!")
        return super.onUnbind(intent)
    }

}

class NoDefault: OnMessageInterface.Stub(){
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
        Log.e(TAG, "showNumber a number from remote:${number}")
    }

    override fun showMessage(message: String?) {
        DesignMessageDeliver.post("Message_From_Remote", message)
    }

    override fun getMessage(): String {
        Log.e(TAG, "getMessage message request from remote!")
        return "remote message:"+(0..1000).random()+"time:"+System.currentTimeMillis()
    }

    override fun asBinder(): IBinder {
        return this
    }

}