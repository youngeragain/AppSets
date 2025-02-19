package xcj.app.starter.android.usecase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import xcj.app.starter.android.ActivityThemeInterface
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage
import xcj.app.starter.android.util.PurpleLogger

class PlatformUseCase {
    companion object {
        private const val TAG = "PlatformUseCase"
        private const val ANDROID_PERMISSIONS_REQUEST_CODE = 8899
        const val REQUEST_CODE_FOR_FILE_PROVIDER = 2221

        fun providePlatformPermissions(context: Context): List<PlatformPermissionsUsage> {
            val platformPermissionsUsages = PlatformPermissionsUsage.provideAll(context)
            platformPermissionsUsages.sortByDescending {
                if (it.granted) {
                    -1
                } else {
                    1
                }
            }
            return platformPermissionsUsages
        }

        fun requestPermission(context: Context, permissions: List<String>) {
            if (context !is Activity) {
                return
            }
            var shouldShowRequestPermissionRationale = false
            for (permission in permissions) {
                shouldShowRequestPermissionRationale =
                    context.shouldShowRequestPermissionRationale(permission)
                if (shouldShowRequestPermissionRationale) {
                    break
                }
            }
            if (shouldShowRequestPermissionRationale) {
                val text =
                    context.getString(xcj.app.starter.R.string.please_grant_permission_in_system_application_settings)
                Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
                navigateToExternalSystemAppDetails(context)
                return
            }
            context.requestPermissions(permissions.toTypedArray(), ANDROID_PERMISSIONS_REQUEST_CODE)

        }

        fun navigateToExternalSystemAppDetails(context: Context) {
            val intent = Intent("android.settings.APP_PERMISSIONS_SETTINGS")
            intent.putExtra(Intent.EXTRA_PACKAGE_NAME, context.packageName)
            runCatching {
                context.startActivity(intent)
            }.onFailure {
                PurpleLogger.current.d(
                    TAG,
                    "navigateToExternalSystemAppDetails, go app's system permission page failed, fallback go to app's system details page, ${it.message}"
                )
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.setData(Uri.parse("package:${context.packageName}"))
                runCatching {
                    context.startActivity(intent)
                }.onFailure {
                    PurpleLogger.current.d(
                        TAG,
                        "navigateToExternalSystemAppDetails, go to app' system details page failed, ${it.message}"
                    )
                }
            }
        }

        fun openSystemFileProviderForOldVersion(
            context: Context,
            requestCode: Int,
            mimeType: String = "*/*",
            multiSelect: Boolean = false
        ) {
            if (context !is Activity) {
                return
            }
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                if (multiSelect) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                type = mimeType
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            runCatching {
                context.startActivityForResult(intent, requestCode)
            }
        }

        fun openSystemFileProviderForNewVersion(
            context: Context,
            mimeType: String = "*/*",
            multiSelect: Boolean = false
        ) {
            if (context !is ActivityThemeInterface) {
                return
            }
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                if (multiSelect) {
                    putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                }
                type = mimeType
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }
            val activityResultLauncher =
                context.getActivityResultLauncher<Intent>(Intent::class.java) as? ActivityResultLauncher<Intent>
            activityResultLauncher?.launch(intent)
        }
    }
}