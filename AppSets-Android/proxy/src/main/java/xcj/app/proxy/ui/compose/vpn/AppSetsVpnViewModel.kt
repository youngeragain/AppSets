package xcj.app.proxy.ui.compose.vpn

import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.content.pm.PackageInfo
import android.net.VpnService
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.proxy.ui.compose.vpn.AppSetsVpnActivity.Prefs
import xcj.app.starter.android.ui.base.DesignViewModel
import java.util.Arrays
import java.util.stream.Collectors

data class AppSetsVpnData(
    var serverAddress: String = "127.0.0.1",
    var serverPort: String = "9999",
    var sharedSecret: String = "",
    var httpProxyHostname: String = "",
    var httpProxyPort: String = "",
    var packagesCommaSeparated: String = "",
    var packagesCommaSeparatedAllowed: String? = null
)

class AppSetsVpnViewModel : DesignViewModel() {

    private lateinit var prefs: SharedPreferences

    val appsetsVpnData: MutableState<AppSetsVpnData> = mutableStateOf(AppSetsVpnData())

    fun updateAppsetsVpnData(appSetsVpnData: AppSetsVpnData) {
        appsetsVpnData.value = appSetsVpnData
    }

    fun onActivityCreated(activity: Activity) {
        val sharedPreferences = activity.getSharedPreferences(Prefs.NAME, MODE_PRIVATE)
        prefs = sharedPreferences
        val appSetsVpnData = AppSetsVpnData()
        appSetsVpnData.serverAddress = prefs.getString(Prefs.SERVER_ADDRESS, "") ?: ""
        val serverPortPrefValue = prefs.getInt(Prefs.SERVER_PORT, 0)
        appSetsVpnData.serverPort =
            (if (serverPortPrefValue == 0) "" else serverPortPrefValue).toString()
        appSetsVpnData.sharedSecret = prefs.getString(Prefs.SHARED_SECRET, "") ?: ""
        appSetsVpnData.httpProxyHostname = prefs.getString(Prefs.PROXY_HOSTNAME, "") ?: ""
        val proxyPortPrefValue = prefs.getInt(Prefs.PROXY_PORT, 0)
        appSetsVpnData.httpProxyPort =
            if (proxyPortPrefValue == 0) "" else proxyPortPrefValue.toString()

        val allow = prefs.getBoolean(Prefs.ALLOW, true)
        if (allow) {
            appSetsVpnData.packagesCommaSeparatedAllowed = "Allow"
        } else {
            appSetsVpnData.packagesCommaSeparatedAllowed = "Disallow"
        }
        appSetsVpnData.packagesCommaSeparated = prefs.getStringSet(
            Prefs.PACKAGES, emptySet()
        )?.joinToString(", ") ?: ""
    }

    fun onConnectButtonClick(activity: AppSetsVpnActivity, connect: Boolean) {
        val appSetsVpnData = appsetsVpnData.value
        if (connect) {
            if (!checkProxyConfigs(
                    activity,
                    appSetsVpnData.httpProxyHostname,
                    appSetsVpnData.httpProxyPort
                )
            ) {
                return
            }
            val packageSet =
                Arrays.stream(
                    appSetsVpnData.packagesCommaSeparated.split(",".toRegex())
                        .dropLastWhile { it.isEmpty() }
                        .toTypedArray())
                    .map { obj: String -> obj.trim { it <= ' ' } }
                    .filter { s: String -> !s.isEmpty() }
                    .collect(Collectors.toSet())
            if (!checkPackages(activity, packageSet)) {
                return
            }
            val serverPortNum = try {
                appSetsVpnData.serverPort.toInt()
            } catch (e: NumberFormatException) {
                0
            }
            val proxyPortNum = try {
                appSetsVpnData.httpProxyPort.toInt()
            } catch (e: NumberFormatException) {
                0
            }

            prefs.edit()
                .putString(
                    Prefs.SERVER_ADDRESS,
                    appSetsVpnData.serverAddress
                )
                .putInt(Prefs.SERVER_PORT, serverPortNum)
                .putString(Prefs.SHARED_SECRET, appSetsVpnData.sharedSecret)
                .putString(Prefs.PROXY_HOSTNAME, appSetsVpnData.httpProxyHostname)
                .putInt(Prefs.PROXY_PORT, proxyPortNum)
                .putBoolean(Prefs.ALLOW, appSetsVpnData.packagesCommaSeparatedAllowed == "Allow")
                .putStringSet(Prefs.PACKAGES, packageSet)
                .apply()
            val intent = VpnService.prepare(activity)
            if (intent != null) {
                activity.startActivityForResult(intent, 0)
            } else {
                activity.makeMockActivityResult(0, RESULT_OK, null)
            }
        } else {
            activity.disConnectVpnService()
        }
    }

    private fun checkProxyConfigs(context: Context, proxyHost: String, proxyPort: String): Boolean {
        val hasIncompleteProxyConfigs = proxyHost.isEmpty() != proxyPort.isEmpty()
        if (hasIncompleteProxyConfigs) {
            Toast.makeText(
                context,
                xcj.app.proxy.R.string.incomplete_proxy_settings,
                Toast.LENGTH_SHORT
            ).show()
        }
        return !hasIncompleteProxyConfigs
    }

    private fun checkPackages(activity: AppSetsVpnActivity, packageNames: Set<String>): Boolean {
        val hasCorrectPackageNames = packageNames.isEmpty() ||
                activity.packageManager.getInstalledPackages(0).stream()
                    .map { pi: PackageInfo -> pi.packageName }
                    .collect(Collectors.toSet())
                    .containsAll(packageNames)
        if (!hasCorrectPackageNames) {
            Toast.makeText(
                activity,
                xcj.app.proxy.R.string.unknown_package_names,
                Toast.LENGTH_SHORT
            ).show()
        }
        return hasCorrectPackageNames
    }
}