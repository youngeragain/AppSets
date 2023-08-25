package xcj.app.core.android.permission

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.res.ResourcesCompat
import xcj.app.core.R

class SpecialPermissionDialog(
    ctx:Context,
    private val specialPermissions: List<String>,
    private val reasons:Map<String,String?>):AlertDialog(ctx) {

    private lateinit var icon:AppCompatImageView
    private lateinit var tipsTextView:AppCompatTextView
    private lateinit var reasonTextView:AppCompatTextView
    private lateinit var denyBtn:AppCompatTextView
    private lateinit var toGrantBtn:AppCompatTextView
    private lateinit var llTipsContainer: LinearLayoutCompat

    private lateinit var textTips:String
    private lateinit var reason:String
    private lateinit var drawable:Drawable
    private var showAnimation = true

    private val toSystemIntent: Intent by lazy { Intent() }

    //点击取消或者用户已经授权才自减
    private var currentSpecialPermissionIndex:Int = specialPermissions.lastIndex

    private val appName: String by lazy { getAppName1() }

    private var isFirst = true
    private fun getAppName1(): String {
        "Unknown application"
        return run {
            val pm = context.packageManager
            val appInfo = context.applicationInfo
            val appName = pm.getApplicationLabel(appInfo).toString()
            appName
        }
    }


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window?.apply {
            setBackgroundDrawable(ColorDrawable(context.getColor(android.R.color.transparent)))
        }

        setContentView(R.layout.dialog_special_permission)
        icon = findViewById<AppCompatImageView>(R.id.iv_ic)!!
        reasonTextView = findViewById<AppCompatTextView>(R.id.tv_request_permission_reason)!!
        tipsTextView = findViewById<AppCompatTextView>(R.id.tv_request_permission_tips)!!
        denyBtn = findViewById<AppCompatTextView>(R.id.tv_deny_action)!!
        toGrantBtn = findViewById<AppCompatTextView>(R.id.tv_to_grant_action)!!
        llTipsContainer = findViewById<LinearLayoutCompat>(R.id.ll1)!!


        denyBtn.setOnClickListener {

            if(currentSpecialPermissionIndex==0)
                dismiss()
            else {
                --currentSpecialPermissionIndex
                showNextPermissionInternal()
            }
        }
        toGrantBtn.setOnClickListener {
            if(currentSpecialPermissionIndex>=0){
                val intent = getIntent()
                intent.action?.let {
                    try{
                        context.startActivity(intent)
                    }catch(e:Exception){
                       e.printStackTrace()
                    }
                }
            }
        }

        showNextPermissionInternal()
    }

    private fun updateUI(){
        if(currentSpecialPermissionIndex>=0){
            icon.setImageDrawable(getIcon())
            reasonTextView.text = getReason1()
            tipsTextView.text = getTips()
        }else{
            dismiss()
        }
    }

    fun showNextPermission(){
        showNextPermissionInternal()
    }

    fun currentSpecialPermissionIndex():Int {
        return currentSpecialPermissionIndex
    }


    private fun showNextPermissionInternal() {
        if (showAnimation && !isFirst) {
            val endRunnable = {
                icon.translationY = 20f
                llTipsContainer.translationY = 20f
                val ani = icon.animate().translationY(0f).alpha(1f).setDuration(250)
                val ani2 = llTipsContainer.animate().translationY(0f).alpha(1f).setDuration(250)
                ani.start()
                ani2.start()
                updateUI()
            }
            val animator = icon.animate().rotationY(-20f).alpha(0f).setDuration(250)
            val animator1 = llTipsContainer.animate().translationY(-20f).alpha(0f).setDuration(250)
                .withEndAction(endRunnable)
            animator.start()
            animator1.start()
        } else {
            updateUI()
        }
        if (isFirst)
            isFirst = false
    }

    private fun getReason1():String {
        return run{
            reason = reasons[specialPermissions[currentSpecialPermissionIndex]]?:context.getString(R.string.the_developer_did_not_provide_a_reason_for_use)
            reason
        }
    }

    private fun getTips():String {
        return run{
            textTips = when (specialPermissions[currentSpecialPermissionIndex]) {
                Manifest.permission.WRITE_SETTINGS -> {
                    String.format(context.getString(R.string.allow_app_to_modify_the_system_settings), appName)
                }
                Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                    String.format(context.getString(R.string.allow_app_to_display_the_overlay), appName)
                }
                Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                    String.format(context.getString(R.string.allow_app_to_manage_external_storage), appName)
                }
                else -> {
                    String.format(context.getString(R.string.allow_app_to_open_a_game), appName)
                }
            }
            textTips
        }
    }


    private fun getIntent():Intent {
        return run{
            when (specialPermissions[currentSpecialPermissionIndex]) {
                Manifest.permission.WRITE_SETTINGS -> {
                    toSystemIntent.action = android.provider.Settings.ACTION_MANAGE_WRITE_SETTINGS
                }
                Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                    toSystemIntent.action = android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION
                }
                Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                    toSystemIntent.action = android.provider.Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                }
                else -> { }
            }
            toSystemIntent
        }
    }
    private fun getIcon():Drawable? {
        return when (specialPermissions[currentSpecialPermissionIndex]) {
            Manifest.permission.WRITE_SETTINGS -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_outline_settings_24, context.theme)
            }
            Manifest.permission.SYSTEM_ALERT_WINDOW -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_baseline_layers_24, context.theme)
            }
            Manifest.permission.MANAGE_EXTERNAL_STORAGE -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_baseline_sd_storage_24, context.theme)
            }
            else -> {
                ResourcesCompat.getDrawable(context.resources, R.drawable.ic_baseline_toys_24, context.theme)
            }
        }
    }

    fun currentSpecialPermissionIndexDecrement() {
        --currentSpecialPermissionIndex
    }
}
