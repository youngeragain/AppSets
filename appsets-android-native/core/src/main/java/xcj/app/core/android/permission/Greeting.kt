package xcj.app.core.android.permission

import android.Manifest.permission.*
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.*
import java.io.File
import java.lang.ref.WeakReference

object Greeting {


    private class GreetingsLifecycleObserver : DefaultLifecycleObserver {
        /*@OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun cleanUp() {
            clearSelf()
        }*/
        override fun onDestroy(owner: LifecycleOwner) {
            clearSelf()
        }
    }

    /**
     * 危险权限, 需要到特定页面手动授予
     *
     * INSTANT_APP_FOREGROUND_SERVICE api 26
     * LOADER_USAGE_STATS api 30
     * MANAGE_EXTERNAL_STORAGE api 30
     * MANAGE_MEDIA api 31
     * MANAGE_ONGOING_CALLS api 31
     * PACKAGE_USAGE_STATS api 23
     * SMS_FINANCIAL_TRANSACTIONS add in api 29 (android 10), deprecate in api 31 (android 12)
     * SYSTEM_ALERT_WINDOW api 1 (android 1.0)
     * USE_ICC_AUTH_WITH_DEVICE_IDENTIFIER api 31 (android 12)
     *
     */
    private val constantsSpecialPermissions: List<String> = listOf(
        INSTANT_APP_FOREGROUND_SERVICE,
        MANAGE_EXTERNAL_STORAGE,
        ACCESS_MEDIA_LOCATION,
        PACKAGE_USAGE_STATS,
        SYSTEM_ALERT_WINDOW,
        WRITE_SETTINGS,
        INSTALL_PACKAGES,
    )

    private lateinit var specialPermissionDialog: SpecialPermissionDialog
    private lateinit var emptyPermissionFragment: EmptyPermissionFragment

    private val deniedPermissions: MutableList<String> = mutableListOf()
    private val grantedPermissions: MutableList<String> = mutableListOf()

    private var runtimePermissions: List<String> = mutableListOf()
    private var specialPermissions: List<String> = mutableListOf()

    private var mHandler: Handler? = null

    private lateinit var context: WeakReference<Context>

    private var holder: Any? = null
        set(value) {
            if (value != null) {
                mHandler = Handler(Looper.getMainLooper())
                if (value is AppCompatActivity) {
                    context = WeakReference(value)
                    field = value
                    addLifecycleOwnerObserver(value)
                } else if (value is Fragment && value.requireContext() != null) {
                    context = WeakReference(value.requireContext())
                    field = value
                    addLifecycleOwnerObserver(value)
                } else {
                    Log.e("blue", "current only support AppcompatActivity and Fragment")
                }
            }
        }
        get() {
            return context.get()
        }

    private var callListGreetingTimes = 0
    internal var canDrawOverlays = false
    internal var canWrite = false
    internal var canManageExternalStorage = false

    private var grantedCallback:((List<String>)->Unit)? = null
    private var deniedCallback:((List<String>)->Unit)? = null

    private fun putHolder(any: Any): Greeting {
        holder = any
        return this
    }

    fun showNextSpecialPermission(){
        if (!::specialPermissionDialog.isInitialized) {
            return
        } else {
            if (specialPermissionDialog.isShowing) {
                specialPermissionDialog.showNextPermission()
            }
        }
    }

    private fun addLifecycleOwnerObserver(provider:LifecycleOwner){
        provider.lifecycle.addObserver(GreetingsLifecycleObserver())
    }

    private fun clearSelf() {
        grantedCallback = null
        deniedCallback = null
        grantedPermissions.clear()
        deniedPermissions.clear()
        holder = null
        context.clear()
        mHandler = null
        callListGreetingTimes = 0
        if (::specialPermissionDialog.isInitialized) {
            if (specialPermissionDialog.isShowing) {
                specialPermissionDialog.setOnDismissListener(null)
                specialPermissionDialog.dismiss()
            }
        }
        if (::emptyPermissionFragment.isInitialized) {
            if (emptyPermissionFragment.isAdded) {
                removeEmptyFragment()
            }
        }
    }

    fun addGrantedSpecialPermission(specialPermission: String) {
        if (!grantedPermissions.contains(specialPermission)) {
            grantedPermissions.add(specialPermission)
        }
        if(deniedPermissions.contains(specialPermission)){
            deniedPermissions.remove(specialPermission)
        }

    }

    private fun addDeniedSpecialPermission(specialPermission: String) {
        if (!deniedPermissions.contains(specialPermission))
            deniedPermissions.add(specialPermission)
        if(grantedPermissions.contains(specialPermission)){
            grantedPermissions.remove(specialPermission)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun showSpecialPermissionDialog(specialPermissions: List<String>, reasons: Map<String, String?>){
        context.get()?.let { context ->
            //过滤掉已经拥有的特殊权限
            val list = specialPermissions.toMutableList()
            updateSpecialPermissionIfNeeded()
            if (canWrite) {//过滤掉修改设置
                list.removeItem(WRITE_SETTINGS)
            } else {
                val contains = list.contains(WRITE_SETTINGS)
                if (contains)
                    addDeniedSpecialPermission(WRITE_SETTINGS)
            }
            if(canDrawOverlays){
                list.removeItem(SYSTEM_ALERT_WINDOW)
            }else{
                val contains = list.contains(SYSTEM_ALERT_WINDOW)
                if(contains)
                    addDeniedSpecialPermission(SYSTEM_ALERT_WINDOW)
            }
            if(canManageExternalStorage){

                list.removeItem(MANAGE_EXTERNAL_STORAGE)
            }else{
                val contains = list.contains(MANAGE_EXTERNAL_STORAGE)
                if(contains){
                    addDeniedSpecialPermission(MANAGE_EXTERNAL_STORAGE)
                }
            }
            if(list.isEmpty()){
                Log.e("blue", "all special permissions is granted!")
                dispatchRequestCallback()
            }else{
                emptyPermissionFragment.specialPermissions = list
                specialPermissionDialog = SpecialPermissionDialog(context, list, reasons).apply {
                    setOnDismissListener { //消失的时候
                        dispatchRequestCallback()
                    }
                }
                specialPermissionDialog.show()
            }
        }
    }



    private fun getBoolean(permissionName: String):Boolean {
        return run {
            var b = false
            for (c in constantsSpecialPermissions) {
                b = b || (permissionName == c)
            }
            b
        }
    }

    private fun removeEmptyFragment() {
        getActivity()?.supportFragmentManager
            ?.beginTransaction()
            ?.remove(emptyPermissionFragment)
            ?.commit()
    }

    private fun getActivity(): FragmentActivity? {
        return (holder as? AppCompatActivity) ?: ((holder as? Fragment)?.requireActivity())
    }

    private fun resolveRuntimePermissions(
        runtimePermissions: List<String>,
        reasons: Map<String, String?>? = null,
        then: ((then: (() -> Unit)?) -> Unit)? = null
    ) {
        runtimePermissions.let {
            emptyPermissionFragment.runtimePermissions = it
            emptyPermissionFragment.runtimePermissionCallback = { permissions, results ->
                for (i in permissions.indices) {
                    if (results[i] == PackageManager.PERMISSION_GRANTED) {
                        grantedPermissions.add(permissions[i])
                    } else if (results[i] == PackageManager.PERMISSION_DENIED) {
                        deniedPermissions.add(permissions[i])
                    }
                }

                then?: run {
                    dispatchRequestCallback()
                }
                then?.invoke(null)
            }
            emptyPermissionFragment.requestRuntimePermissions()
        }
    }

    private fun dispatchRequestCallback(){
        grantedCallback?.invoke(grantedPermissions)
        deniedCallback?.invoke(deniedPermissions)
        callListGreetingTimes = 0
        removeEmptyFragment()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun resolveSpecialPermissions(
        specialPermissions: List<String>,
        reasons: Map<String, String?>) {
        showSpecialPermissionDialog(specialPermissions, reasons)
    }

    fun getCurrentSpecialPermissionShowPermissionIndex():Int? {
        return if (!::specialPermissionDialog.isInitialized) {
            null
        } else {
            if (!specialPermissionDialog.isShowing) {
                null
            } else {
                specialPermissionDialog.currentSpecialPermissionIndex()
            }
        }
    }

    fun currentSpecialPermissionShowPermissionIndexDecrement() {
        if (!::specialPermissionDialog.isInitialized) {
            return
        } else {
            if (!specialPermissionDialog.isShowing) {
                return
            } else {
                specialPermissionDialog.currentSpecialPermissionIndexDecrement()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun updateSpecialPermissionIfNeeded() {
        if (!::context.isInitialized)
            return
        if (context.get() == null) {
            return
        }
        canDrawOverlays = Settings.canDrawOverlays(context.get())
        canWrite = Settings.System.canWrite(context.get())
        //只有android 10之后才会为null, 如果开发者最高适配了安卓10，并且Manifests.xml里面请求了legacy storage,这里不会为null
        val tempFile = Environment.getExternalStorageDirectory()?.path?.let {
            File(it)
        }
        canManageExternalStorage = tempFile != null && (tempFile.canWrite())
        if (canManageExternalStorage) {
            tempFile?.delete()
        }
    }

    private fun MutableList<String>.removeItem(item: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            this.removeIf {
                if (it == item) {
                    addGrantedSpecialPermission(it)
                    true
                } else {
                    false
                }
            }
        } else {
            for (specialPermission in this) {
                if (specialPermission == item) {
                    addGrantedSpecialPermission(specialPermission)
                    this.remove(specialPermission)
                    break
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @JvmName("GreetingYou_greeting")
    fun greeting(
        context: Context,
        map: Map<String, String>,
        granted: (List<String>) -> Unit,
        denied: (List<String>) -> Unit
    ) = map.greeting(context, granted, denied)


    @RequiresApi(Build.VERSION_CODES.M)
    fun Map<String, String?>.greeting(
        context: Context,
        granted: ((List<String>) -> Unit)? = null,
        denied: ((List<String>) -> Unit)? = null
    ) = entries.map { it.key }.greeting(context, this, granted, denied)

    @RequiresApi(Build.VERSION_CODES.M)
    fun Pair<String, String?>.greeting(
        context: Context,
        granted: ((String?) -> Unit)? = null,
        denied: ((String?) -> Unit)? = null
    ) = mapOf<String, String?>(this).greeting(
        context,
        {
            if (!it.isNullOrEmpty()) {
                if (it.size == 1) {
                    granted?.invoke(it[0])
                } else {
                    granted?.invoke(null)
                }
            }

        },
        {
            if (!it.isNullOrEmpty()) {
                if (it.size == 1) {
                    denied?.invoke(it[0])
                } else {
                    denied?.invoke(null)
                }
            }
        })

    @RequiresApi(Build.VERSION_CODES.M)
    fun String.greeting(
        context: Context, granted: ((String?) -> Unit)? = null,
        denied: ((String?) -> Unit)? = null
    ) =
        (this to null).greeting(context, granted, denied)

    /**
     * real entry point
     */

    @RequiresApi(Build.VERSION_CODES.M)
    @Synchronized
    fun List<String>.greeting(
        context: Context,
        reasons: Map<String, String?>,
        granted: ((List<String>) -> Unit)? = null,
        denied: ((List<String>) -> Unit)? = null
    ) {
        if (++callListGreetingTimes > 1) {
            callListGreetingTimes = 1
            Log.e("blue", "Please do not call the greeting method multiple times synchronously!")
            return
        }
        putHolder(context)
        val activity = getActivity() ?: return
        grantedCallback = granted
        deniedCallback = denied

        if (!::emptyPermissionFragment.isInitialized) {
            emptyPermissionFragment = EmptyPermissionFragment()
        }
        if (!emptyPermissionFragment.isAdded) {
            activity
                .supportFragmentManager
                .beginTransaction()
                .add(emptyPermissionFragment, "special_permission_fragment")
                .commit()
        }
        grantedPermissions.clear()
        deniedPermissions.clear()
        runtimePermissions = this.filterNot {
            getBoolean(it)
        }
        specialPermissions = this.filter {
            getBoolean(it)
        }

        if (runtimePermissions.isEmpty() && specialPermissions.isEmpty()) {
            //do nothing
        } else if (runtimePermissions.isNotEmpty() && specialPermissions.isNotEmpty()) {//既有运行时权限，又有危险权限

            if (emptyPermissionFragment.isResumed) {
                //first 处理运行时权限
                resolveRuntimePermissions(runtimePermissions) {
                    //then 处理特殊权限
                    resolveSpecialPermissions(specialPermissions, reasons)
                }
            } else {
                mHandler?.postDelayed({
                    //first 处理运行时权限
                    resolveRuntimePermissions(runtimePermissions){
                        //then 处理特殊权限
                        resolveSpecialPermissions(specialPermissions, reasons)
                    }
                }, 0)
            }
        } else if (runtimePermissions.isEmpty()) {//只有危险权限
            resolveSpecialPermissions(specialPermissions, reasons)
        } else {//只有运行时权限
            if(emptyPermissionFragment.isResumed){
                resolveRuntimePermissions(runtimePermissions)
            }else{
                mHandler?.postDelayed({
                    resolveRuntimePermissions(runtimePermissions)
                }, 0)
            }
        }
    }
}