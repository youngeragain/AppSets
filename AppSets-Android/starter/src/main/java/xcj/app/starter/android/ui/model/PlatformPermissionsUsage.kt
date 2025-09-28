package xcj.app.starter.android.ui.model

import android.Manifest
import android.content.Context
import android.net.Uri
import android.os.Build
import xcj.app.starter.android.usecase.PlatformUseCase

data class PlatformPermissionsUsage(
    val name: Int,
    val description: Int,
    val usage: Int,
    val usageUri: Uri?,
    val androidDefinitionNames: List<String>,
    val relativeAndroidDefinitionNames: List<String>?,
    val granted: Boolean = false,
) {
    var icon: Int? = null
    companion object {

        private const val TAG = "PlatformPermissionsUsage"

        private fun withCheck(
            context: Context,
            name: Int,
            description: Int,
            usage: Int,
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
                granted = hasPlatformPermissions,
            )

        }

        fun provideCamera(context: Context): PlatformPermissionsUsage {
            val permission = withCheck(
                context,
                xcj.app.starter.R.string.camera,
                xcj.app.starter.R.string.camera_app_usage_des,
                xcj.app.starter.R.string.camera_permission_tips,
                null,
                listOf(Manifest.permission.CAMERA),
                null
            )
            return permission
        }

        fun provideLocation(context: Context): PlatformPermissionsUsage {
            val permission = withCheck(
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
            return permission
        }

        fun provideFilePermission(context: Context): PlatformPermissionsUsage {
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
            val permission = withCheck(
                context,
                xcj.app.starter.R.string.file,
                xcj.app.starter.R.string.file_permission_app_usage_des,
                xcj.app.starter.R.string.file_permission_tips,
                null,
                filesAndroidPermissions,
                null

            )
            return permission
        }

        fun provideAudioRecordPermission(context: Context): PlatformPermissionsUsage {
            val permission = withCheck(
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
            return permission
        }

        fun provideInstallPackagePermission(context: Context): PlatformPermissionsUsage {
            val installPackageRelatePermissions =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    listOf(
                        Manifest.permission.QUERY_ALL_PACKAGES
                    )
                } else {
                    null
                }
            val permission = withCheck(
                context,
                xcj.app.starter.R.string.install_package,
                xcj.app.starter.R.string.install_package_permission_app_usage_des,
                xcj.app.starter.R.string.install_package_permission_tips,
                null,
                listOf(Manifest.permission.INSTALL_PACKAGES),
                installPackageRelatePermissions
            )
            return permission
        }

        fun provideInternetPermission(context: Context): PlatformPermissionsUsage {
            val internetPermissions = listOf(Manifest.permission.INTERNET)
            val internetRelatePermissions = mutableListOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                internetRelatePermissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            }
            val permission = withCheck(
                context,
                xcj.app.starter.R.string.internet,
                xcj.app.starter.R.string.internet_permission_app_usage_des,
                xcj.app.starter.R.string.internet_permission_tips,
                null,
                internetPermissions,
                internetRelatePermissions
            )
            return permission
        }

        fun providePostNotificationPermission(context: Context): PlatformPermissionsUsage? {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                return null
            }
            val permission = withCheck(
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
            return permission
        }

        fun provideIgnoreBatteryUsagePermission(context: Context): PlatformPermissionsUsage {
            val permission = withCheck(
                context,
                xcj.app.starter.R.string.ignore_battery_optimizations,
                xcj.app.starter.R.string.ignore_battery_optimizations_app_usage_des,
                xcj.app.starter.R.string.ignore_battery_optimizations_permission_tips,
                null,
                listOf(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS),
                null,
                checker = PlatformUseCase::isIgnoringBatteryOptimizations
            )
            return permission
        }

        fun provideAll(
            context: Context,
            sort: Boolean = true
        ): List<PlatformPermissionsUsage> {
            val list = buildList {
                val platformCameraPermission = provideCamera(context)
                val platformLocationPermission = provideLocation(context)
                val platformFilePermission = provideFilePermission(context)
                val platformRecordAudioPermission = provideAudioRecordPermission(context)
                val platformInstallPackagePermission = provideInstallPackagePermission(context)
                val platformInternetPermission = provideInternetPermission(context)
                val platformPostNotificationPermission = providePostNotificationPermission(context)
                val platformIgnoreBatteryUsagePermission =
                    provideIgnoreBatteryUsagePermission(context)
                add(platformCameraPermission)
                add(platformLocationPermission)
                add(platformFilePermission)
                add(platformRecordAudioPermission)
                add(platformInstallPackagePermission)
                add(platformInternetPermission)
                if (platformPostNotificationPermission != null) {
                    add(platformPostNotificationPermission)
                }
                add(platformIgnoreBatteryUsagePermission)
            }
            if (sort) {
                return list.sortedByDescending {
                    if (it.granted) {
                        -1
                    } else {
                        1
                    }
                }
            }
            return list
        }
    }
}