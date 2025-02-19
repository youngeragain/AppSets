package xcj.app.proxy.ui.compose.vpn

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import kotlinx.coroutines.launch
import xcj.app.proxy.service.AppSetsVpnService
import xcj.app.proxy.ui.compose.theme.AppSetsTheme
import xcj.app.starter.android.ui.base.DesignComponentActivity

class AppSetsVpnActivity : DesignComponentActivity() {

    companion object {
        private const val TAG = "AppSetsVpnActivity"
    }

    interface Prefs {
        companion object {
            const val NAME: String = "connection"
            const val SERVER_ADDRESS: String = "server.address"
            const val SERVER_PORT: String = "server.port"
            const val SHARED_SECRET: String = "shared.secret"
            const val PROXY_HOSTNAME: String = "proxyhost"
            const val PROXY_PORT: String = "proxyport"
            const val ALLOW: String = "allow"
            const val PACKAGES: String = "packages"
        }
    }

    private val serviceIntent: Intent
        get() = Intent(this, AppSetsVpnService::class.java)

    private val viewModel: AppSetsVpnViewModel by viewModels<AppSetsVpnViewModel>()

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                AppSetsVpnMainContent(
                    onConnectButtonClick = {
                        viewModel.onConnectButtonClick(this, it)
                    }
                )
            }
        }
        lifecycleScope.launch {
            lifecycle.withCreated {
                viewModel.onActivityCreated(this@AppSetsVpnActivity)
            }
        }
    }

    override fun onActivityResult(request: Int, result: Int, data: Intent?) {
        super.onActivityResult(request, result, data)
        if (result == RESULT_OK) {
            startService(serviceIntent.setAction(AppSetsVpnService.ACTION_CONNECT))
        }
    }

    fun makeMockActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onActivityResult(requestCode, resultCode, data)
    }

    fun disConnectVpnService() {
        serviceIntent.setAction(AppSetsVpnService.ACTION_DISCONNECT)
    }
}
