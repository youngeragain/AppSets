package xcj.app.compose_share.usecase

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.LifecycleObserver
import xcj.app.compose_share.dynamic.ComposeMethodsAware
import xcj.app.compose_share.dynamic.ComposeMethodsWrapper
import xcj.app.compose_share.dynamic.IComposeLifecycleAware
import xcj.app.compose_share.dynamic.PluginsRegistry
import xcj.app.starter.android.usecase.PlatformUseCase
import xcj.app.starter.android.util.FileUtil
import xcj.app.starter.android.util.PurpleLogger
import xcj.app.starter.test.LocalAndroidContextFileDir
import xcj.app.starter.util.ContentType

class ComposeDynamicUseCase() : ComposeMethodsAware, IComposeLifecycleAware, LifecycleObserver {

    val composeMethodsState: MutableList<ComposeMethodsWrapper> =
        mutableStateListOf()

    override fun setMethodsContainer(methods: MutableList<ComposeMethodsWrapper>) {
        if (methods.isEmpty()) {
            composeMethodsState.clear()
        } else {
            composeMethodsState.clear()
            composeMethodsState.addAll(methods)
        }
    }

    suspend fun doLoad() {
        PluginsRegistry.loadMethodsToContainer(this@ComposeDynamicUseCase)
    }

    fun onAddClick(context: Context) {
        PlatformUseCase.openSystemFileProvider(
            context,
            PlatformUseCase.REQUEST_CODE_FOR_FILE_PROVIDER,
            ContentType.APPLICATION_ANDROID_PACKAGE
        )
    }

    suspend fun onActivityResult(
        context: Context,
        requestCode: Int,
        resultCode: Int,
        data: Intent
    ) {
        val fileUri = data.data
        if (requestCode != PlatformUseCase.REQUEST_CODE_FOR_FILE_PROVIDER ||
            resultCode != Activity.RESULT_OK ||
            fileUri == null
        ) {
            return
        }
        val desFilePath = FileUtil.copyFileToInternalStorage(
            context,
            fileUri,
            LocalAndroidContextFileDir.current.dynamicAARDir,
            null
        )
        if (desFilePath.isNullOrEmpty()) {
            PurpleLogger.current.d(
                TAG,
                "onExternalAARFileSelectActivityResult early return, desFilePath is null"
            )
            return
        }
        val result = PluginsRegistry.registerAARFromExternal(desFilePath)
        if (result) {
            doLoad()
        }
    }

    fun onDeleteClick(wrapper: ComposeMethodsWrapper) {
        val tempMethods = composeMethodsState
        if (tempMethods.isEmpty()) {
            return
        }
        val aarName = wrapper.iComposeMethods.getAARName()
        if (aarName.isNullOrEmpty()) {
            return
        }
        try {
            val iterator = tempMethods.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.iComposeMethods == wrapper.iComposeMethods) {
                    iterator.remove()
                }
            }
            wrapper.iComposeMethods.onComposeDispose("by delete")
            wrapper.iComposeMethods.getStatesHolder().onUnLoad()
            PluginsRegistry.unRegisterByAARByUser(aarName, wrapper.iComposeMethods)
        } catch (e: Exception) {
            PurpleLogger.current.d(TAG, "onDeleteClick, exception:${e.message}")
        }
    }

    override fun onComposeDispose(by: String?) {
        composeMethodsState.forEach { it.iComposeMethods.onComposeDispose("dispose") }
        setMethodsContainer(mutableListOf())
    }

    companion object {
        private const val TAG = "ComposeDynamicUseCase"
    }
}