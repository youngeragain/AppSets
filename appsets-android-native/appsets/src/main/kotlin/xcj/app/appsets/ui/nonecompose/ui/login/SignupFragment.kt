package xcj.app.appsets.ui.nonecompose.ui.login

import android.app.Activity
import android.content.ComponentName
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.drawToBitmap
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import xcj.app.appsets.databinding.FragmentSignUpBinding
import xcj.app.appsets.ktx.saveBitmap
import xcj.app.appsets.ui.nonecompose.base.BaseFragment
import xcj.app.appsets.ui.nonecompose.base.BaseViewModelFactory
import xcj.external.appsets.OnMessageInterface

class SignupFragment :
    BaseFragment<FragmentSignUpBinding, SignupVM, BaseViewModelFactory<SignupVM>>() {
    private val arcs_StartActivityForResult = ActivityResultContracts.StartActivityForResult()
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private val TAG = "SignupFragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityResultLauncher = registerForActivityResult(arcs_StartActivityForResult) {
            Log.e(
                TAG, """
                it.resultCode:${it.resultCode}
                Activity.RESULT_OK:${Activity.RESULT_OK}
            """.trimIndent()
            )
            if (it.resultCode == Activity.RESULT_OK) {
                Log.e(TAG, "registerForActivityResult2")
                it.data?.getStringExtra("id")?.let { bitmapId->
                    Uri.parse("content://xcj.appsets.provider/bitmap").also { uri ->
                        lifecycleScope.launch(Dispatchers.IO){
                            val deleteResult = context?.contentResolver?.delete(uri, "id=?", arrayOf(bitmapId))
                            Log.e(TAG, "deleteResult${deleteResult}")
                            if(deleteResult==0){
                                Log.e(TAG, "删除成功!")
                            }
                        }
                    }
                }
            }
        }
    }

    fun initView() {
        binding?.apply {
            tvChoosePicAction.setOnClickListener {
                /* val pair = android.Manifest.permission.READ_EXTERNAL_STORAGE to "选择图片发生"
                 pair.greeting(granted= {
                     SelectActionBottomSheetDialog(requireContext(), emptyList(), 10, 1).show()
                 })*/
                //RenderEffect.createBlurEffect(10f,10f, Shader.TileMode.CLAMP)
                val intent = Intent().apply {
                    //action="cccc"
                    //`package` = "xcj.areyouok"
                    setClassName("xcj.areyouok", "xcj.areyouok.service.TempService")
                }
                val b = requireActivity().bindService(intent, object :ServiceConnection{
                    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                        Log.e(TAG, "onServiceConnected:name${name},service:${service} ")
                        viewModel?.onMessageInterface = OnMessageInterface.Stub.asInterface(service)
                    }

                    override fun onServiceDisconnected(name: ComponentName?) {
                        Log.e(TAG, "onServiceDisconnected:name${name}")
                    }

                    override fun onNullBinding(name: ComponentName?) {
                        super.onNullBinding(name)
                        Log.e(TAG, "onNullBinding:name${name}")
                    }
                }, Context.BIND_AUTO_CREATE)
                Log.e(TAG, "bindService:${b}")
            }
            btnSignUpAction.setOnClickListener {
                val requireActivity = requireActivity()
                lifecycleScope.launch(Dispatchers.IO){
                    requireActivity.window.decorView.drawToBitmap()?.let {
                        val saveBitmapResult = requireActivity.saveBitmap(it)
                        if(saveBitmapResult!=null){
                            val uri = Uri.parse("content://xcj.appsets.provider/bitmap")
                            val newUri = requireActivity.contentResolver.insert(uri, ContentValues(1).apply {
                                put("path", saveBitmapResult.first)
                                put("name", saveBitmapResult.second)
                            })
                            val id = newUri?.pathSegments?.last()
                            val intent = Intent().apply {
                                action = "are_you_ok"
                                `package` = "xcj.areyouok"
                                putExtra("hello", "what's the weather today?")
                                putExtra("bitmapId", id)
                            }
                            activityResultLauncher.launch(arcs_StartActivityForResult.createIntent(requireContext(), intent))
                            //Log.e(TAG, "newUri id:${id}")
                            /*delay(200)
                            requireActivity.contentResolver.query(uri, arrayOf("id"), null, arrayOf(id), null)?.use { cursor->
                                if(cursor.count==1){
                                    while (cursor.moveToNext()){
                                        val index = cursor.getColumnIndexOrThrow("path")
                                        val bitmapDrawable =
                                            BitmapFactory.decodeFile(cursor.getString(index)).toDrawable(resources)
                                        if(bitmapDrawable!=null){
                                            withContext(Dispatchers.Main){
                                                ivUserAvatar.setImageDrawable(bitmapDrawable)
                                            }
                                            break
                                        }
                                    }
                                }
                            }*/
                        }
                    }
                }

                //viewModel?.onMessageInterface?.showNumber(123)

            }
        }
    }
}