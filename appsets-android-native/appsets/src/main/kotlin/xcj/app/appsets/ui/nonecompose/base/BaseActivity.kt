package xcj.app.appsets.ui.nonecompose.base

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.ViewDataBinding
import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.provider.AppStdBroadcastReceiver
import xcj.app.appsets.util.NotificationPusher
import xcj.app.appsets.util.ThemeUtil

abstract class BaseActivity<VDB : ViewDataBinding, VM : CommonViewModel, VMF : BaseViewModelFactory<VM>> :
    AppCompatActivity(), DesignInterface, NotificationPusherInterface {
    private lateinit var mAppStdBroadcastReceiver: AppStdBroadcastReceiver
    private val themeUtil = ThemeUtil()
    var viewModel: VM? = null
    var binding: VDB? = null
    var baseBinding: ViewDataBinding? = null

    open fun createBy(): String {
        return "default"
    }

    open fun createBinding(): VDB? {
        return null
    }

    open fun createViewModel(): VM? {
        return null
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        themeUtil.onCreate(this)
        when (createBy()) {
            "default" -> {
                binding = createBinding()
                if (baseBinding != null) {
                    if (binding != null)
                        (baseBinding!!.root as? ViewGroup)?.addView(binding!!.root)
                }
                if (baseBinding != null)
                    setContentView(baseBinding!!.root)
                else if (binding != null)
                    setContentView(binding!!.root)
                viewModel = createViewModel()
            }

            "reflection" -> {
                val fullReflectionConstructor = FullReflectionConstructor()
                binding = fullReflectionConstructor.createViewBinding(this, this)
                if (baseBinding != null) {
                    if (binding != null)
                        (baseBinding!!.root as ViewGroup).addView(binding!!.root)
                }
                if (baseBinding != null)
                    setContentView(baseBinding!!.root)
                else if (binding != null)
                    setContentView(binding!!.root)
                viewModel = fullReflectionConstructor.createViewModel(this, this, this)
            }
        }
    }

    override fun onReceive(context: Context?, p1: Intent?) {

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onResume() {
        val systemBarsHiddenState = getSystemBarsHiddenState()
        themeUtil.onResume(this, systemBarsHiddenState.first, systemBarsHiddenState.second)
        super.onResume()
    }

    open fun getSystemBarsHiddenState(): Pair<Boolean, Boolean> {
        return false to false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::mAppStdBroadcastReceiver.isInitialized) {
            unregisterReceiver(mAppStdBroadcastReceiver)
        }
    }

    override fun pushNotificationIfNeeded(
        notificationPusher: NotificationPusher,
        sessionId: String,
        imMessage: ImMessage
    ) {

    }
}


