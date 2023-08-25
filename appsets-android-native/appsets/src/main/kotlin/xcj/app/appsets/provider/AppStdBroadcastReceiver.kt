package xcj.app.appsets.provider

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AppStdBroadcastReceiver(private val appStdBroadcast: AppStdBroadcast?) : BroadcastReceiver() {
    override fun onReceive(p0: Context?, p1: Intent?) {
        appStdBroadcast?.onReceive(p0, p1)
    }

    interface AppStdBroadcast {
        fun onReceive(context: Context?, p1: Intent?)
    }
}