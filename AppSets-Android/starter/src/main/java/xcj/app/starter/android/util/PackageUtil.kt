package xcj.app.starter.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_ALL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import xcj.app.starter.android.AppDefinition
import java.util.UUID

object PackageUtil {
    private const val TAG = "PackageUtil"

    @SuppressLint("QueryPermissionsNeeded")
    @JvmStatic
    suspend fun getAppDefinitionList(context: Context): List<AppDefinition> =
        withContext(Dispatchers.IO) {
            val packageManager = context.packageManager
            val installedPackages =
                packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
            return@withContext installedPackages.mapNotNull { packageInfo ->
                val applicationInfo = packageInfo.applicationInfo
                if (applicationInfo == null) {
                    null
                } else {
                    val appDefinition = AppDefinition(UUID.randomUUID().toString())
                    appDefinition.applicationInfo = applicationInfo
                    appDefinition
                }
            }
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
    fun getLauncherIntentAppDefinitionListFlow(context: Context): Flow<List<AppDefinition>> =
        flow {
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
            val appDefinitions = queryIntentActivities.mapNotNull { resolveInfo ->
                val app = if (resolveInfo.activityInfo.packageName == minePackageName) {
                    null
                } else {
                    val appDefinition = AppDefinition(
                        UUID.randomUUID().toString()
                    )
                    val applicationInfo = resolveInfo.activityInfo.applicationInfo
                    appDefinition.applicationInfo = applicationInfo
                    appDefinition.name =
                        applicationInfo.loadLabel(context.packageManager).toString()
                            .trim()

                    appDefinition.icon =
                        applicationInfo.loadIcon(context.packageManager)
                    appDefinition
                }
                app
            }
            emit(appDefinitions)
            PurpleLogger.current.d(TAG, "getLauncherIntentAppDefinitionList 4")
        }

    suspend fun getAppDefinitionByPackageName(
        context: Context,
        packageName: String
    ): AppDefinition? =
        withContext(
            Dispatchers.IO
        ) {
            try {
                val packageManager = context.packageManager
                val appInfo = packageManager.getApplicationInfo(packageName, 0)
                val appDefinition = AppDefinition(UUID.randomUUID().toString())
                appDefinition.applicationInfo = appInfo
                appDefinition.name = appInfo.loadLabel(packageManager).toString().trim()
                appDefinition.icon = appInfo.loadIcon(packageManager)
                return@withContext appDefinition
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                return@withContext null
            }
        }
}