package xcj.app.launcher.ui.float_home

import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner

class FloatWindowHomeService : Service(), SavedStateRegistryOwner {

    private val mLifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)
    private val mSavedStateRegistryController: SavedStateRegistryController =
        SavedStateRegistryController.create(this)
    override val lifecycle: Lifecycle
        get() = mLifecycleRegistry
    override val savedStateRegistry: SavedStateRegistry
        get() = mSavedStateRegistryController.savedStateRegistry

    private lateinit var mBinder: FloatWindowHomeBinder

    init {
        mSavedStateRegistryController.performAttach()
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate() {
        super.onCreate()
        mSavedStateRegistryController.performRestore(null)
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        val floatWindowHome = FloatWindowHome(this)
        floatWindowHome.initWindow(this)
        mBinder = FloatWindowHomeBinder(floatWindowHome)
    }

    override fun onBind(intent: Intent?): IBinder? {
        if (::mBinder.isInitialized) {
            mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            return mBinder
        }
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        return super.onUnbind(intent)

    }

    override fun onDestroy() {
        super.onDestroy()
        mLifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        mSavedStateRegistryController.performSave(Bundle())
    }
}

