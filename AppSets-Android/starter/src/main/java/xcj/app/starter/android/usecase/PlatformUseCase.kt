package xcj.app.starter.android.usecase

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.DocumentsContract
import android.provider.Settings
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import xcj.app.starter.android.ActivityThemeInterface
import xcj.app.starter.android.util.PurpleLogger

class PlatformUseCase {
    companion object {
        private const val TAG = "PlatformUseCase"
        private const val ANDROID_PERMISSIONS_REQUEST_CODE = 8899
        const val REQUEST_CODE_FOR_FILE_PROVIDER = 2221

        fun hasPlatformPermissions(context: Context, permissions: List<String>): Boolean {
            permissions.forEach { permission ->
                val checkSelfPermissionResult =
                    ContextCompat.checkSelfPermission(context, permission)
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

        fun requestPermission(context: Context, permissions: List<String>) {
            if (context !is Activity) {
                return
            }
            if (permissions.contains(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)) {
                requestBatteryOptimizationSettings(context)
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
                    ContextCompat.getString(
                        context,
                        xcj.app.starter.R.string.please_grant_permission_in_system_application_settings
                    )
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
                    "navigateToExternalSystemAppDetails, go app's system permission page failed," +
                            " fallback go to app's system details page, ${it.message}"
                )
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.setData("package:${context.packageName}".toUri())
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

        fun makeFileSelectionIntent(
            mimeType: String = "*/*",
            multiSelect: Boolean,
        ): Intent {
            return Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
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
        }

        fun makeOpenDirectoryIntent(pickerInitialUri: Uri? = null): Intent {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                // Optionally, specify a URI for the directory that should be opened in
                // the system file picker when it loads.

                if (pickerInitialUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
                }
            }
            return intent
        }

        fun openSystemFileProvider(
            context: Context,
            requestCode: Int,
            mimeType: String = "*/*",
            multiSelect: Boolean = false,
        ) {
            if (context !is Activity) {
                return
            }
            val intent = makeFileSelectionIntent(mimeType, multiSelect)
            runCatching {
                context.startActivityForResult(intent, requestCode)
            }
        }

        fun openSystemFileProvider(
            context: Context,
            mimeType: String = "*/*",
            multiSelect: Boolean = false,
        ) {
            if (context !is ActivityThemeInterface) {
                return
            }
            val intent = makeFileSelectionIntent(mimeType, multiSelect)
            runCatching {
                val activityResultLauncher =
                    context.getActivityResultLauncher(
                        ActivityResultContracts.StartActivityForResult::class.java,
                        null
                    )
                activityResultLauncher?.launch(intent)
            }
        }

        fun openSystemFileProviderToOpenDirectory(
            context: Context,
            pickerInitialUri: Uri? = null,
        ) {
            if (context !is ActivityThemeInterface) {
                return
            }
            val intent = makeOpenDirectoryIntent(pickerInitialUri)
            runCatching {
                val activityResultLauncher =
                    context.getActivityResultLauncher(
                        ActivityResultContracts.StartActivityForResult::class.java,
                        null
                    )
                activityResultLauncher?.launch(intent)
            }
        }

        fun isIgnoringBatteryOptimizations(context: Context, permissions: List<String>): Boolean {
            val powerManager = context.getSystemService(PowerManager::class.java)
            if (powerManager == null) {
                return true
            }
            val packageName = context.packageName
            return powerManager.isIgnoringBatteryOptimizations(packageName)
        }

        fun requestBatteryOptimizationSettings(context: Context) {
            val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
            intent.setData(("package:${context.packageName}").toUri())
            context.startActivity(intent)
        }
    }
}