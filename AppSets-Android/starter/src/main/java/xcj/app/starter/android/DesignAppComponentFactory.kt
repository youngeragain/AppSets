package xcj.app.starter.android

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.app.Service
import android.content.BroadcastReceiver
import android.content.ContentProvider
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.CoreComponentFactory
import xcj.app.starter.android.util.PurpleLogger

@SuppressLint("RestrictedApi")
@RequiresApi(Build.VERSION_CODES.P)
class DesignAppComponentFactory : CoreComponentFactory() {
    companion object {
        private const val TAG = "DesignAppComponentFactory"
    }

    override fun instantiateActivity(
        cl: ClassLoader,
        className: String,
        intent: Intent?
    ): Activity {
        PurpleLogger.current.d(TAG, "instantiateActivity")
        val activity = super.instantiateActivity(cl, className, intent)
        return activity
    }

    override fun instantiateReceiver(
        cl: ClassLoader,
        className: String,
        intent: Intent?
    ): BroadcastReceiver {
        PurpleLogger.current.d(TAG, "instantiateReceiver")
        return super.instantiateReceiver(cl, className, intent)
    }

    override fun instantiateService(
        cl: ClassLoader,
        className: String,
        intent: Intent?
    ): Service {
        PurpleLogger.current.d(TAG, "instantiateService")
        return super.instantiateService(cl, className, intent)
    }

    override fun instantiateApplication(cl: ClassLoader, className: String): Application {
        PurpleLogger.current.d(TAG, "instantiateApplication")
        return super.instantiateApplication(cl, className)
    }

    override fun instantiateProvider(cl: ClassLoader, className: String): ContentProvider {
        PurpleLogger.current.d(TAG, "instantiateProvider")
        return super.instantiateProvider(cl, className)
    }

    override fun instantiateClassLoader(cl: ClassLoader, aInfo: ApplicationInfo): ClassLoader {
        PurpleLogger.current.d(TAG, "instantiateClassLoader")
        return super.instantiateClassLoader(cl, aInfo)
    }
}