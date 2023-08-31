package xcj.app.appsets.ui.nonecompose

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModelProvider
import xcj.app.appsets.databinding.ActivityShareableMessageBinding
import xcj.app.appsets.ui.nonecompose.base.BaseActivity
import xcj.app.appsets.ui.nonecompose.base.BaseViewModel
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.external.appsets.OnMessageInterface

class ShareableMessageViewModel : BaseViewModel()

class ShareableMessageActivity :
    BaseActivity<ActivityShareableMessageBinding, ShareableMessageViewModel, BaseViewModelFactory<ShareableMessageViewModel>>() {
    private var onMessageInterface: OnMessageInterface? = null
    private var serviceConnection: ServiceConnection? = null
    private var binded = false
    private val TAG = "ShareableMessageActivity"

    override fun createBinding(): ActivityShareableMessageBinding? {
        return ActivityShareableMessageBinding.inflate(layoutInflater)
    }

    override fun createViewModel(): ShareableMessageViewModel? {
        return ViewModelProvider(this)[ShareableMessageViewModel::class.java]
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    fun connectToService(view: View) {
        if (binded)
            return
        val intent = Intent()
        intent.component = ComponentName("xcj.app.bindersimple", "xcj.app.bindersimple.ShareableMessageService")
        if(serviceConnection==null) {
            serviceConnection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                    onMessageInterface = OnMessageInterface.Stub.asInterface(service)
                    Log.e(
                        TAG,
                        "onServiceConnected:name:$name, onMessageInterface:$onMessageInterface"
                    )
                    binded = true
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    Log.e(TAG, "onServiceDisconnected:name:$name")
                }

                override fun onBindingDied(name: ComponentName?) {
                    Log.e(TAG, "onBindingDied:name:$name")
                }

                override fun onNullBinding(name: ComponentName?) {
                    Log.e(TAG, "onNullBinding:name:$name")
                }
            }
        }
        if(serviceConnection!=null)
            bindService(intent, serviceConnection!!,  BIND_AUTO_CREATE)
    }
    fun getMessage(view: View) {
        val message = onMessageInterface?.message
        binding!!.tvMessageFromRemote.text = "Message from remote:$message"
    }
    fun setMessage(view: View) {
        val message = "message:"+(0..10000).random()+"time:"+System.currentTimeMillis()
        onMessageInterface?.showMessage(message)
    }

    fun disconnectToService(view: View) {
        if(binded){
            if(onMessageInterface!=null&&serviceConnection!=null){
                unbindService(serviceConnection!!)
                binded = false
            }
        }
    }
}