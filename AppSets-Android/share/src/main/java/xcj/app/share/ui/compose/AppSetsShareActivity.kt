package xcj.app.share.ui.compose

import AppSetsShareMainContent
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
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
import kotlin.getValue

class AppSetsShareActivity : DesignComponentActivity() {
    companion object {
        private const val TAG = "AppSetsShareActivity"
        const val CLICK_TYPE_NORMAL = 0
        const val CLICK_TYPE_LONG = 1
        const val CLICK_TYPE_DOUBLE = 2
    }

    private val viewModel: AppSetsShareViewModel by viewModels<AppSetsShareViewModel>()
    private var openMultipleFilesLauncher: ActivityResultLauncher<Intent>? = null
    private var shareMethod: ShareMethod = MockShareMethod()

    override fun <I> getActivityResultLauncher(inputClazz: Class<I>): ActivityResultLauncher<*>? {
        if (inputClazz == Intent::class.java) {
            return openMultipleFilesLauncher
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
                    onContentViewClick = ::onContentViewClick
                )
            }
        }
        lifecycleScope.launch {
            lifecycle.withCreated {
                updateShareMethod(HttpShareMethod::class.java)
            }
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
        openMultipleFilesLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode != RESULT_OK) {
                    return@registerForActivityResult
                }
                val data: Intent? = result.data
                if (data == null) {
                    return@registerForActivityResult
                }
                // 处理选择的多个文件
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val uri: Uri = clipData.getItemAt(i).uri
                        // 使用 URI 处理文件，例如获取文件路径、读取文件内容等
                        handleFileUri(uri)
                    }
                } else if (data.data != null) {
                    val uri: Uri = data.data!!
                    handleFileUri(uri)
                }
            }
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
        PlatformUseCase.openSystemFileProviderForNewVersion(this, multiSelect = true)
    }

    private fun onAddTextContentClick(content: String) {
        if (content.isEmpty()) {
            return
        }
        viewModel.addPendingContent(this, content)
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
        viewModel.addPendingContent(this, uri)
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
}