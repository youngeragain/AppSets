package xcj.app.appsets.ui.compose.main

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MotionEvent
import android.window.OnBackInvokedDispatcher
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import xcj.app.appsets.ui.compose.theme.AppSetsTheme
import xcj.app.appsets.ui.viewmodel.MainViewModel
import xcj.app.appsets.util.SplashScreenHelper
import xcj.app.compose_share.ui.viewmodel.VisibilityComposeStateViewModel.Companion.bottomSheetState
import xcj.app.starter.android.AppDefinition
import xcj.app.starter.android.ui.base.DesignComponentActivity
import xcj.app.starter.android.util.PackageUtil
import xcj.app.starter.android.util.PurpleLogger

class MainActivity : DesignComponentActivity() {

    companion object {
        const val TAG = "MainActivity"
        const val EXTERNAL_CONTENT_HANDLE_BY_APPSETS = 0
        const val EXTERNAL_CONTENT_HANDLE_BY_LOCAL_SHARE = 1
    }

    private var pickVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? = null
    private var pickMultipleVisualMediaLauncher: ActivityResultLauncher<PickVisualMediaRequest>? =
        null
    private val viewModel by viewModels<MainViewModel>()

    override fun <V : ViewModel> requireViewModel(): V? {
        return viewModel as? V
    }

    private fun makeActivityResultLauncher() {
        pickVisualMediaLauncher = registerForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
            callback = { uri ->
                getSystemContentSelectionCallback()?.onSystemContentSelected(uri)
            }
        )
        pickMultipleVisualMediaLauncher = registerForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia(),
            callback = { uris ->
                getSystemContentSelectionCallback()?.onSystemContentSelected(uris)
            }
        )
    }

    override fun <I, C : ActivityResultContract<I, *>> getActivityResultLauncher(
        contractClass: Class<C>,
        requestPrams: Any?
    ): ActivityResultLauncher<I>? {
        if (contractClass == ActivityResultContracts.PickVisualMedia::class.java) {
            return pickVisualMediaLauncher as? ActivityResultLauncher<I>
        } else if (contractClass == ActivityResultContracts.PickMultipleVisualMedia::class.java) {
            return pickMultipleVisualMediaLauncher as? ActivityResultLauncher<I>
        }
        return null
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SplashScreenHelper.onActivityCreate(this)
        setContent {
            AppSetsTheme {
                // A surface container using the 'background' color from the theme
                MainPage()
            }
        }

        viewModel.onActivityCreated(this@MainActivity)
        viewModel.handleIntent(intent)
        makeActivityResultLauncher()
        handleExternalShareContentIfNeeded(this@MainActivity, intent)

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        viewModel.handleIntent(intent)
        handleExternalShareContentIfNeeded(this, intent)
    }

    override fun getOnBackInvokedDispatcher(): OnBackInvokedDispatcher {
        val onBackInvokedDispatcher = super.getOnBackInvokedDispatcher()
        /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
             val t: OnBackInvokedCallback = object :OnBackInvokedCallback{
                 override fun onBackInvoked() {
                     PurpleLogger.current.d(TAG, "back gesture invoke")
                 }
             }
             onBackInvokedDispatcher.registerOnBackInvokedCallback(OnBackInvokedDispatcher.PRIORITY_DEFAULT, t)
         }*/
        return onBackInvokedDispatcher
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.dispatchActivityResult(this, requestCode, resultCode, data)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return super.onTouchEvent(event)
    }

    private fun handleExternalShareContentIfNeeded(
        lifecycleOwner: LifecycleOwner,
        intent: Intent?
    ) {
        PurpleLogger.current.d(TAG, "handleExternalShareContentIfNeeded, intent:$intent")
        if (intent == null) {
            return
        }

        when (intent.action) {
            Intent.ACTION_SEND,
            Intent.ACTION_SEND_MULTIPLE -> {
                handleIntentExternalContent(lifecycleOwner, intent)
            }
        }
    }

    private fun handleIntentExternalContent(
        lifecycleOwner: LifecycleOwner,
        intent: Intent,
    ) {
        val handled = intent.getBooleanExtra("external_content_handled", false)
        if (handled) {
            return
        }
        intent.putExtra("external_content_handled", true)
        //wait compose first frame draw finish
        val composeContainerState = viewModel.bottomSheetState()
        lifecycleScope.launch {
            composeContainerState.composableStateAvailableFlow.collect { composableAvailable ->
                if (!composableAvailable) {
                    return@collect
                }
                composeContainerState.setShouldBackgroundSink(true)
                val fromAppDefinition = getCallActivityAppDefinition()
                composeContainerState.show {
                    ExternalContentSheetContent(
                        intent = intent,
                        fromAppDefinition = fromAppDefinition,
                        onConfirmClick = { handleType ->
                            when (handleType) {
                                EXTERNAL_CONTENT_HANDLE_BY_LOCAL_SHARE -> {
                                    composeContainerState.hide()
                                    handleExternalDataByAppSetsShare(intent)

                                }

                                EXTERNAL_CONTENT_HANDLE_BY_APPSETS -> {
                                }
                            }
                        }
                    )
                }
            }
        }
    }

    private fun handleExternalDataByAppSetsShare(intent: Intent) {
        navigateToAppSetsShareActivity(this, intent)
    }

    private suspend fun getCallActivityAppDefinition(): AppDefinition? {
        val callingPackage = getCallingPackage()
        if (callingPackage.isNullOrEmpty()) {
            return null
        }
        return PackageUtil.getAppDefinitionByPackageName(this, callingPackage)
    }


    override fun onEnterAnimationComplete() {
        PurpleLogger.current.d(TAG, "onEnterAnimationComplete")
    }
}