package xcj.app.share.ui.compose

import AppSetsShareMainContent
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Parcelable
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.withCreated
import kotlinx.coroutines.launch
import xcj.app.share.base.DataContent
import xcj.app.share.base.ShareDevice
import xcj.app.share.base.ShareMethod
import xcj.app.share.http.HttpShareMethod
import xcj.app.share.mock.MockShareMethod
import xcj.app.share.ui.compose.theme.AppSetsTheme
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.usecase.PlatformUseCase
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.util.ContentType
import kotlin.getValue

class AppSetsShareActivity : DesignComponentActivity() {
    companion object {
        private const val TAG = "AppSetsShareActivity"

        const val INTENT_ACTION = "xcj.app.share"
        const val INTENT_CATEGORY = "xcj.app.share.main"

        const val CONTENT_FORM_APP = 0
        const val CONTENT_FORM_SYSTEM_APP = 1
        const val CONTENT_FORM_OTHER_APP = 2

    }

    private val viewModel: AppSetsShareViewModel by viewModels<AppSetsShareViewModel>()
    private var intentResultLauncher: ActivityResultLauncher<Intent>? = null
    private var shareMethod: ShareMethod = MockShareMethod()

    override fun <I> getActivityResultLauncher(
        inputClazz: Class<I>,
        requestPrams: Any?
    ): ActivityResultLauncher<*>? {
        if (inputClazz == Intent::class.java) {
            return intentResultLauncher
        }
        return null
    }

    override fun isKeepScreenOn(): Boolean {
        return true
    }

    override fun requireViewModel(): AppSetsShareViewModel {
        return viewModel
    }

    fun getShareMethod(): ShareMethod {
        return shareMethod
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppSetsTheme {
                AppSetsShareMainContent(
                    onBackClick = ::onBackClick,
                    onDiscoveryClick = ::onDiscoveryClick,
                    onCloseEstablishCLick = ::onCloseEstablishCLick,
                    onShareDeviceClick = ::onShareDeviceClick,
                    onAddFileContentClick = ::onAddFileContentClick,
                    onAddTextContentClick = ::onAddTextContentClick,
                    onContentViewClick = ::onContentViewClick,
                    onScanClick = ::onScanClick
                )
            }
        }
        lifecycleScope.launch {
            lifecycle.withCreated {
                updateShareMethod(HttpShareMethod::class.java)
                lifecycleScope.launch {
                    handleExternalShareContentIfNeeded(intent)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        lifecycleScope.launch {
            handleExternalShareContentIfNeeded(intent)
        }
    }

    private fun onScanClick() {
        val platformPermissionsUsageOfCamera =
            PlatformUseCase.providePlatformPermissions(this).firstOrNull {
                it.name == getString(xcj.app.starter.R.string.camera)
            }
        if (platformPermissionsUsageOfCamera == null) {
            return
        }
        if (!platformPermissionsUsageOfCamera.granted) {
            val permissionsToRequest =
                platformPermissionsUsageOfCamera.androidDefinitionNames.toMutableList().apply {
                    val relatives = platformPermissionsUsageOfCamera.relativeAndroidDefinitionNames
                    if (!relatives.isNullOrEmpty()) {
                        addAll(relatives)
                    }
                }
            PlatformUseCase.requestPermission(this, permissionsToRequest)
            return
        }
        val intent = Intent()
        val componentName = ComponentName(
            "xcj.app.container",
            "xcj.app.appsets.ui.compose.camera.CameraComposeActivity"
        )
        intent.setComponent(componentName)
        runCatching {
            val activityResultLauncher = getActivityResultLauncher<Intent>(Intent::class.java, null) as? ActivityResultLauncher<Intent>
            activityResultLauncher?.launch(intent)
        }.onFailure {
            it.printStackTrace()
            PurpleLogger.current.d(TAG, "onScanClick, start CameraActivity failed!, ${it.message}")
        }
    }

    private fun onContentViewClick(dataContent: DataContent) {
        when (dataContent) {
            is DataContent.FileContent -> {
                runCatching {
                    val intent = Intent(Intent.ACTION_VIEW)
                    val uri =
                        FileProvider.getUriForFile(
                            this,
                            "$packageName.provider",
                            dataContent.file
                        )
                    intent.setDataAndType(uri, "*/*")
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    startActivity(intent)
                }.onFailure {
                    PurpleLogger.current.d(
                        TAG,
                        "onContentViewClick, startActivity failed, e:${it.message}"
                    )
                }
            }

            else -> Unit
        }
    }

    override fun makeActivityResultLauncher() {
        val contract = ActivityResultContracts.StartActivityForResult()
        val activityResultCallback = ActivityResultCallback<ActivityResult> { result ->
            if (result.resultCode != RESULT_OK) {
                return@ActivityResultCallback
            }
            val returnIntent: Intent? = result.data
            if (returnIntent == null) {
                return@ActivityResultCallback
            }
            val hasShareDeviceAddress = returnIntent.hasExtra("APPSETS_SHARE_DEVICE_ADDRESSES")
            if(hasShareDeviceAddress){
                val shareDeviceAddresses =
                    returnIntent.getStringArrayExtra("APPSETS_SHARE_DEVICE_ADDRESSES")
                getShareMethod().onScanShareDeviceAddress(shareDeviceAddresses)
            }else{
                // 处理选择的多个文件
                val clipData = returnIntent.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri: Uri = clipData.getItemAt(i).uri
                        // 使用 URI 处理文件，例如获取文件路径、读取文件内容等
                        handleFileUri(uri)
                    }
                } else if (returnIntent.data != null) {
                    val uri: Uri = returnIntent.data!!
                    handleFileUri(uri)
                }
            }
        }
        intentResultLauncher = registerForActivityResult(contract, activityResultCallback)

    }

    private fun onCloseEstablishCLick() {
        shareMethod.close()
    }

    private fun onAddFileContentClick() {
        val platformPermissionsUsageOfFile =
            PlatformUseCase.providePlatformPermissions(this).firstOrNull {
                it.name == this.getString(xcj.app.starter.R.string.file)
            }
        if (platformPermissionsUsageOfFile == null) {
            return
        }
        if (!platformPermissionsUsageOfFile.granted) {
            PurpleLogger.current.d(TAG, "onAddFileContentClick, No Android Permissions!")
            val permissionsToRequest =
                platformPermissionsUsageOfFile.androidDefinitionNames.toMutableList().apply {
                    val relatives = platformPermissionsUsageOfFile.relativeAndroidDefinitionNames
                    if (!relatives.isNullOrEmpty()) {
                        addAll(relatives)
                    }
                }
            PlatformUseCase.requestPermission(this, permissionsToRequest)
            return
        }
        PlatformUseCase.openSystemFileProvider(this, multiSelect = true)
    }

    private fun onAddTextContentClick(text: String) {
        if (text.isEmpty()) {
            return
        }
        viewModel.addPendingContent(this, text, CONTENT_FORM_APP)
    }

    fun onBackClick() {
        onBackPressedDispatcher.onBackPressed()
    }

    private fun onShareDeviceClick(shareDevice: ShareDevice, clickType: Int) {
        getShareMethod().onShareDeviceClick(shareDevice, clickType)
    }

    private fun onDiscoveryClick() {
        getShareMethod().discovery()
    }

    private fun handleFileUri(uri: Uri) {
        viewModel.addPendingContent(this, uri, CONTENT_FORM_SYSTEM_APP)
    }

    fun <SM : ShareMethod> updateShareMethod(shareMethodType: Class<SM>) {
        val oldShareMethod = getShareMethod()
        if (oldShareMethod::class.java == shareMethodType) {
            return
        }
        viewModel.updateShareMethodTypeState(shareMethodType)
        oldShareMethod.destroy()
        val newShareMethod = ShareMethod.createInstanceByType(shareMethodType)
        newShareMethod.mDeviceName = oldShareMethod.mDeviceName
        newShareMethod.init(this, viewModel)
        shareMethod = newShareMethod

    }

    fun sendAll() {
        getShareMethod().sendAll()
    }

    private fun handleExternalShareContentIfNeeded(
        intent: Intent?
    ) {
        if (intent == null) {
            return
        }
        when (intent.action) {
            Intent.ACTION_SEND, Intent.ACTION_SEND_MULTIPLE -> {
                handleIntentExternalContent(intent)
            }
        }
    }


    private fun handleIntentExternalContent(
        intent: Intent
    ) {
        if (intent.type == ContentType.TEXT_PLAIN) {
            intent.getStringExtra(Intent.EXTRA_TEXT)?.let { text ->
                viewModel.addPendingContent(this, text, CONTENT_FORM_OTHER_APP)
            }
        } else {
            val isMulti = intent.action == Intent.ACTION_SEND_MULTIPLE
            if (!isMulti) {
                (intent.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let { uri ->
                    viewModel.addPendingContent(this, uri, CONTENT_FORM_OTHER_APP)
                }
            } else {
                intent.getParcelableArrayListExtra<Parcelable>(Intent.EXTRA_STREAM)
                    ?.mapNotNull {
                        it as? Uri
                    }?.let { uris ->
                        viewModel.addPendingContent(this, uris, CONTENT_FORM_OTHER_APP)
                    }
            }
        }
    }
}