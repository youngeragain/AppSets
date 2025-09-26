package xcj.app.starter.android.ui.model

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import androidx.annotation.StringRes
import xcj.app.starter.android.usecase.PlatformUseCase

data class PlatformPermissionsUsage(
    @field:StringRes val name: Int,
    @field:StringRes val description: Int,
    @field:StringRes val usage: Int,
    val usageUri: Uri?,
    val androidDefinitionNames: List<String>,
    val relativeAndroidDefinitionNames: List<String>?,
    val granted: Boolean = false,
) {

    companion object {

        private const val TAG = "PlatformPermissionsUsage"

        private fun withCheck(
            context: Context,
            @StringRes name: Int,
            @StringRes description: Int,
            @StringRes usage: Int,
            usageUri: Uri?,
            androidDefinitionNames: List<String>,
            relativeAndroidDefinitionNames: List<String>?,
            checker: (context: Context, permissions: List<String>) -> Boolean = PlatformUseCase::hasPlatformPermissions
        ): PlatformPermissionsUsage {
            val hasPlatformPermissions = checker(
                context,
                androidDefinitionNames
            )
            return PlatformPermissionsUsage(
                name,
                description,
                usage,
                usageUri,
                androidDefinitionNames,
                relativeAndroidDefinitionNames,
                hasPlatformPermissions
            )

        }

        fun provideAll(
            context: Context
        ): List<PlatformPermissionsUsage> {
            return mutableListOf<PlatformPermissionsUsage>().apply {
                val cameraPermission = withCheck(
                    context,
                    xcj.app.starter.R.string.camera,
                    xcj.app.starter.R.string.camera_app_usage_des,
                    xcj.app.starter.R.string.camera_permission_tips,
                    null,
                    listOf(Manifest.permission.CAMERA),
                    null
                )
                add(cameraPermission)
                val locationPermission = withCheck(
                    context,
                    xcj.app.starter.R.string.location,
                    xcj.app.starter.R.string.location_app_usage_des,
                    xcj.app.starter.R.string.location_permission_tips,
                    null,
                    listOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ),

                    null
                )
                add(locationPermission)
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
                val filePermission = withCheck(
                    context,
                    xcj.app.starter.R.string.file,
                    xcj.app.starter.R.string.file_permission_app_usage_des,
                    xcj.app.starter.R.string.file_permission_tips,
                    null,
                    filesAndroidPermissions,
                    null

                )
                add(filePermission)
                val recordPermission = withCheck(
                    context,
                    xcj.app.starter.R.string.record_audio,
                    xcj.app.starter.R.string.record_audio_permission_app_usage_des,
                    xcj.app.starter.R.string.record_audio_permission_tips,
                    null,
                    listOf(Manifest.permission.RECORD_AUDIO),
                    listOf(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    )

                )
                add(recordPermission)
                val installPackageRelatePermissions =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        listOf(
                            Manifest.permission.QUERY_ALL_PACKAGES
                        )
                    } else {
                        null
                    }
                val installPackagePermission = withCheck(
                    context,
                    xcj.app.starter.R.string.install_package,
                    xcj.app.starter.R.string.install_package_permission_app_usage_des,
                    xcj.app.starter.R.string.install_package_permission_tips,
                    null,
                    listOf(Manifest.permission.INSTALL_PACKAGES),
                    installPackageRelatePermissions
                )
                add(installPackagePermission)
                val internetPermissions = listOf(Manifest.permission.INTERNET)
                val internetRelatePermissions = mutableListOf(
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    internetRelatePermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
                }
                val internetPermission = withCheck(
                    context,
                    xcj.app.starter.R.string.internet,
                    xcj.app.starter.R.string.internet_permission_app_usage_des,
                    xcj.app.starter.R.string.internet_permission_tips,
                    null,
                    internetPermissions,
                    internetRelatePermissions
                )
                add(internetPermission)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val postNotificationPermission = withCheck(
                        context,
                        xcj.app.starter.R.string.notification,
                        xcj.app.starter.R.string.notification_permission_app_usage_des,
                        xcj.app.starter.R.string.notification_permission_tips,
                        null,
                        listOf(Manifest.permission.POST_NOTIFICATIONS),
                        listOf(
                            Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
                        )
                    )
                    add(postNotificationPermission)
                }

                val ignoreBatteryUsagePermission = withCheck(
                    context,
                    xcj.app.starter.R.string.ignore_battery_optimizations,
                    xcj.app.starter.R.string.ignore_battery_optimizations_app_usage_des,
                    xcj.app.starter.R.string.ignore_battery_optimizations_permission_tips,
                    null,
                    listOf(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),
                    null,
                    checker = PlatformUseCase::isIgnoringBatteryOptimizations
                )
                add(ignoreBatteryUsagePermission)
            }
        }
    }
}