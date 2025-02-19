package xcj.app.starter.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_ALL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.starter.android.AppDefinition
import java.util.UUID

object PackageUtil {
    private const val TAG = "PackageUtil"

    @SuppressLint("QueryPermissionsNeeded")
    @JvmStatic
    suspend fun getPackageList(context: Context): List<AppDefinition> =
        withContext(Dispatchers.IO) {
            val installedPackages =
                context.packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            val appDefinitionList = mutableListOf<AppDefinition>()
            installedPackages.mapNotNullTo(appDefinitionList) { packageInfo ->
                val applicationInfo = packageInfo.applicationInfo
                if (applicationInfo == null) {
                    null
                } else {
                    val appDefinition = AppDefinition(UUID.randomUUID().toString())
                    appDefinition.applicationInfo = applicationInfo
                    appDefinition
                }
            }
            return@withContext appDefinitionList
        }

    @JvmStatic
    fun isSystem(packageInfo: PackageInfo): Boolean {
        val applicationInfo = packageInfo.applicationInfo ?: return false
        val sysApp = (applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1
        val sysUpd =
            (applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == ApplicationInfo.FLAG_UPDATED_SYSTEM_APP
        return sysApp || sysUpd
    }

    @SuppressLint("QueryPermissionsNeeded")
    @JvmStatic
    suspend fun getLauncherIntentAppDefinitionList(context: Context): List<AppDefinition> =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "getLauncherIntentAppDefinitionList 1")
            val packageManager: PackageManager = context.packageManager
            val minePackageName = context.packageName
            val launcherIntent: Intent = Intent().apply {
                action = Intent.ACTION_MAIN
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            PurpleLogger.current.d(TAG, "getLauncherIntentAppDefinitionList 2")
            val queryIntentActivities =
                packageManager.queryIntentActivities(launcherIntent, MATCH_ALL)
            PurpleLogger.current.d(TAG, "getLauncherIntentAppDefinitionList 3")
            val appDefinitions = mutableListOf<AppDefinition>()
            queryIntentActivities.mapNotNullTo(appDefinitions) {
                if (it.activityInfo.packageName == minePackageName) {
                    null
                } else {
                    val appDefinition = AppDefinition(
                        UUID.randomUUID().toString()
                    )
                    appDefinition.applicationInfo = it.activityInfo.applicationInfo
                    appDefinition.name =
                        it.activityInfo.applicationInfo.loadLabel(context.packageManager).toString()
                            .trim()
                            .toString().trim()

                    appDefinition.icon =
                        it.activityInfo.applicationInfo.loadIcon(context.packageManager)
                    appDefinition
                }
            }
            PurpleLogger.current.d(TAG, "getLauncherIntentAppDefinitionList 4")
            return@withContext appDefinitions
        }
}