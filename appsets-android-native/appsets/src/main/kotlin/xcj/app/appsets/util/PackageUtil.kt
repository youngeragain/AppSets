package xcj.app.appsets.util

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import xcj.app.appsets.ui.compose.start.AppDefinition

object PackageUtil {

    fun getPackageList(context: Context):List<AppDefinition>{
        val installedPackages =
            context.packageManager.getInstalledPackages(PackageManager.GET_ACTIVITIES)
        val appDefinitionList = mutableListOf<AppDefinition>()
        installedPackages.forEach {
            val iconDrawable = context.packageManager.getApplicationIcon(it.applicationInfo)
            var appIconImageBitmap: ImageBitmap? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (iconDrawable is AdaptiveIconDrawable) {
                    appIconImageBitmap =
                        (iconDrawable.foreground as? BitmapDrawable)?.bitmap?.asImageBitmap()
                }
            }else if (iconDrawable is BitmapDrawable) {
                appIconImageBitmap = iconDrawable.bitmap.asImageBitmap()
            }

            val label = context.packageManager.getApplicationLabel(it.applicationInfo).toString()

            AppDefinition(appIconImageBitmap, it.packageName, label)

        }
        return appDefinitionList
    }
    fun isSystem(packageInfo: PackageInfo):Boolean{
        val sysApp = (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 1
        val sysUpd = (packageInfo.applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 1
        return sysApp// || sysUpd
    }

    fun hasLauncherIntentAppDefinitionList(context: Context):SnapshotStateList<AppDefinition>{
        val packageManager: PackageManager = context.packageManager
        val minePackageName = context.packageName
        val launcherIntent:Intent = Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val queryIntentActivities = packageManager.queryIntentActivities(launcherIntent, 0)
        val appDefinitions = mutableStateListOf<AppDefinition>()
        queryIntentActivities.mapNotNullTo(appDefinitions) {
            val packageName = it.activityInfo.packageName
            val icon = it.loadIcon(packageManager).toBitmap().asImageBitmap()
            val label =
                packageManager.getApplicationLabel(it.activityInfo.applicationInfo).toString()
            val activityName = it.activityInfo.name
            if (packageName == minePackageName)
                null
            else
                AppDefinition(icon, packageName, label, activityName)
        }

        return appDefinitions
    }
}