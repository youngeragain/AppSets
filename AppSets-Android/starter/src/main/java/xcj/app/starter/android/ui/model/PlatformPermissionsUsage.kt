package xcj.app.starter.android.ui.model

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import xcj.app.starter.android.usecase.PlatformUseCase
import xcj.app.starter.android.util.PurpleLogger

data class PlatformPermissionsUsage(
    val name: String,
    val androidDefinitionNames: List<String>,
    val relativeAndroidDefinitionNames: List<String>?,
    val description: String,
    val usage: String,
    val usageUri: Uri?,
    val granted: Boolean = false
) {

    companion object {

        private const val TAG = "PlatformPermissionsUsage"

        private fun hasPlatformPermissions(context: Context, permissions: List<String>): Boolean {
            permissions.forEach { permission ->
                val checkSelfPermissionResult = context.checkSelfPermission(permission)
                PurpleLogger.current.d(
                    TAG,
                    "hasPlatformPermissions, permission:$permission, granted:${
                        checkSelfPermissionResult == PackageManager.PERMISSION_GRANTED
                    }"
                )
                if (checkSelfPermissionResult == PackageManager.PERMISSION_DENIED) {
                    return false
                }
            }
            return true
        }

        private fun withCheck(
            context: Context,
            name: String,
            androidDefinitionNames: List<String>,
            relativeAndroidDefinitionNames: List<String>?,
            description: String,
            usage: String,
            usageUri: Uri?,
            checker: (context: Context, permissions: List<String>) -> Boolean = ::hasPlatformPermissions
        ): PlatformPermissionsUsage {
            val hasPlatformPermissions = checker(
                context,
                androidDefinitionNames
            )
            return PlatformPermissionsUsage(
                name,
                androidDefinitionNames,
                relativeAndroidDefinitionNames,
                description,
                usage,
                usageUri,
                hasPlatformPermissions
            )

        }

        fun provideAll(
            context: Context
        ): MutableList<PlatformPermissionsUsage> {
            return mutableListOf<PlatformPermissionsUsage>().apply {
                add(
                    withCheck(
                        context,
                        context.getString(xcj.app.starter.R.string.camera),
                        listOf(Manifest.permission.CAMERA),
                        null,
                        context.getString(xcj.app.starter.R.string.camera_permission_tips),
                        context.getString(xcj.app.starter.R.string.camera_app_usage_des),
                        null
                    )
                )
                add(
                    withCheck(
                        context,
                        context.getString(xcj.app.starter.R.string.location),
                        listOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ),
                        null,
                        context.getString(xcj.app.starter.R.string.location_permission_tips),
                        context.getString(xcj.app.starter.R.string.location_app_usage_des),
                        null
                    )
                )
                val filesAndroidPermissions =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        listOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.READ_MEDIA_AUDIO
                        )
                    } else {
                        listOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        )
                    }
                add(
                    withCheck(
                        context,
                        context.getString(xcj.app.starter.R.string.file),
                        filesAndroidPermissions,
                        null,
                        context.getString(xcj.app.starter.R.string.file_permission_tips),
                        context.getString(xcj.app.starter.R.string.file_permission_app_usage_des),
                        null
                    )
                )
                add(
                    withCheck(
                        context,
                        context.getString(xcj.app.starter.R.string.record_audio),
                        listOf(Manifest.permission.RECORD_AUDIO),
                        listOf(
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        ),
                        context.getString(xcj.app.starter.R.string.record_audio_permission_tips),
                        context.getString(xcj.app.starter.R.string.record_audio_permission_app_usage_des),
                        null
                    )
                )
                add(
                    withCheck(
                        context,
                        context.getString(xcj.app.starter.R.string.install_package),
                        listOf(Manifest.permission.INSTALL_PACKAGES),
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            listOf(
                                Manifest.permission.QUERY_ALL_PACKAGES
                            )
                        } else {
                            null
                        },
                        context.getString(xcj.app.starter.R.string.install_package_permission_tips),
                        context.getString(xcj.app.starter.R.string.install_package_permission_app_usage_des),
                        null
                    )
                )
                val internetPermissions = listOf(Manifest.permission.INTERNET)
                val internetRelatePermissions = mutableListOf(
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    internetRelatePermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                }
                add(
                    withCheck(
                        context,
                        context.getString(xcj.app.starter.R.string.internet),
                        internetPermissions,
                        internetRelatePermissions,
                        context.getString(xcj.app.starter.R.string.internet_permission_tips),
                        context.getString(xcj.app.starter.R.string.internet_permission_app_usage_des),
                        null
                    )
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    add(
                        withCheck(
                            context,
                            context.getString(xcj.app.starter.R.string.notification),
                            listOf(Manifest.permission.POST_NOTIFICATIONS),
                            listOf(
                                Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
                            ),
                            context.getString(xcj.app.starter.R.string.notification_permission_tips),
                            context.getString(xcj.app.starter.R.string.notification_permission_app_usage_des),
                            null
                        )
                    )
                }

                add(
                    withCheck(
                        context,
                        context.getString(xcj.app.starter.R.string.ignore_battery_optimizations),
                        listOf(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),
                        null,
                        context.getString(xcj.app.starter.R.string.ignore_battery_optimizations_permission_tips),
                        context.getString(xcj.app.starter.R.string.ignore_battery_optimizations_app_usage_des),
                        null,
                        checker = PlatformUseCase::isIgnoringBatteryOptimizations
                    )
                )
            }
        }
    }
}