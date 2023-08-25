package xcj.app.core.android.permission

import android.Manifest
import android.os.*
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

typealias PermissionCallback = (
    permissions: Array<out String>,
    grantResults: IntArray
)->Unit

internal class EmptyPermissionFragment : Fragment(){
    internal var runtimePermissionCallback: PermissionCallback?=null
    internal var runtimePermissions:List<String>?=null
    internal var specialPermissions:List<String>?=null
    private val mRequestCode = 65530

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onResume() {
        super.onResume()
        specialPermissions?.let {
            Greeting.updateSpecialPermissionIfNeeded()
            Greeting.getCurrentSpecialPermissionShowPermissionIndex()
                ?.let { currentShowingSpecialPermissionIndex ->
                    if (it.size > currentShowingSpecialPermissionIndex && currentShowingSpecialPermissionIndex >= 0) {
                        if (Greeting.canDrawOverlays &&
                            it.contains(Manifest.permission.SYSTEM_ALERT_WINDOW) &&
                            it[currentShowingSpecialPermissionIndex] == Manifest.permission.SYSTEM_ALERT_WINDOW
                        ) {
                            Greeting.apply {
                                addGrantedSpecialPermission(Manifest.permission.SYSTEM_ALERT_WINDOW)
                                currentSpecialPermissionShowPermissionIndexDecrement()
                                showNextSpecialPermission()
                            }
                        }

                        if (Greeting.canWrite &&
                            it.contains(Manifest.permission.WRITE_SETTINGS) &&
                            it[currentShowingSpecialPermissionIndex] == Manifest.permission.WRITE_SETTINGS
                        ) {
                            Greeting.apply {
                                addGrantedSpecialPermission(Manifest.permission.WRITE_SETTINGS)
                                currentSpecialPermissionShowPermissionIndexDecrement()
                                showNextSpecialPermission()
                            }
                        }
                        if (Greeting.canManageExternalStorage &&
                            it.contains(Manifest.permission.MANAGE_EXTERNAL_STORAGE) &&
                            it[currentShowingSpecialPermissionIndex] == Manifest.permission.MANAGE_EXTERNAL_STORAGE
                        ) {
                            Greeting.apply {
                                addGrantedSpecialPermission(Manifest.permission.MANAGE_EXTERNAL_STORAGE)
                                currentSpecialPermissionShowPermissionIndexDecrement()
                                showNextSpecialPermission()
                            }
                        }
                }
            }
        }
    }

    fun requestRuntimePermissions(){
        runtimePermissions?.let {
            requestPermissions(it.toTypedArray(), mRequestCode)
        }

    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(mRequestCode == requestCode)
            runtimePermissionCallback?.invoke(permissions, grantResults)
    }
}