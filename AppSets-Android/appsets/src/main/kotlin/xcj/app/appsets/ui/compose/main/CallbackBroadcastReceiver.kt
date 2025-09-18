package xcj.app.appsets.ui.compose.main

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class CallbackBroadcastReceiver(
    private val callback: (Context, Intent) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        callback(context, intent)
    }
}