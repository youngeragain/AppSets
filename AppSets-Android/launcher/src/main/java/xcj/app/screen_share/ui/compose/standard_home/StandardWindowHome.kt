package xcj.app.screen_share.ui.compose.standard_home

import AppSetsTheme
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import kotlinx.coroutines.launch
import xcj.app.screen_share.ui.compose.float_home.FloatWindowHomeBinder
import xcj.app.screen_share.ui.compose.float_home.FloatWindowHomeService
import xcj.app.starter.android.util.PurpleLogger

class StandardWindowHome : ComponentActivity() {
    companion object {
        private const val TAG = "StandardWindowHome"
    }

    private var floatWindowHomeBinder: FloatWindowHomeBinder? = null

    private val viewModel by viewModels<StandardWindowHomeViewModel>()

    fun requireViewModel(): StandardWindowHomeViewModel? {
        return viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                // A surface container using the 'background' color from the theme
                StandardWindowHomeMainContent()
            }
        }
        lifecycleScope.launch {
            lifecycle.withCreated {
                viewModel.prepare(this@StandardWindowHome)
                //bindFloatWindowHomeService(false)
            }
        }
    }

    private fun bindFloatWindowHomeService(bind: Boolean) {
        if (!bind) {
            return
        }
        val serviceConnection: ServiceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                PurpleLogger.current.d(TAG, "onServiceConnected")
                floatWindowHomeBinder = service as FloatWindowHomeBinder
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                floatWindowHomeBinder = null
            }

            override fun onBindingDied(name: ComponentName?) {
                floatWindowHomeBinder = null
            }
        }
        bindService(
            Intent(
                this,
                FloatWindowHomeService::class.java
            ),
            serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    fun showFloatWindow() {
        floatWindowHomeBinder?.showWindow()
    }

    fun hideFloatWindow() {
        floatWindowHomeBinder?.hideWindow()
    }
}