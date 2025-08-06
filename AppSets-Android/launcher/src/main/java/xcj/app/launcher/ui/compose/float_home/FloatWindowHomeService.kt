package xcj.app.launcher.ui.compose.float_home

import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import xcj.app.compose_share.service.ComposableService

class FloatWindowHomeService : ComposableService() {

    private lateinit var mBinder: FloatWindowHomeBinder


    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate() {
        super.onCreate()
        val floatWindowHome = FloatWindowHome(this)
        floatWindowHome.initWindow(this)
        mBinder = FloatWindowHomeBinder(floatWindowHome)
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (::mBinder.isInitialized) {
            return mBinder
        }
        return null
    }

}

