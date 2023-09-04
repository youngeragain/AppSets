package xcj.app.compose_share.compose.usecase

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import kotlinx.coroutines.CoroutineScope
import xcj.app.compose_share.compose.dynamic.ComposeMethodsAware
import xcj.app.compose_share.compose.dynamic.IComposeMethods
import xcj.app.compose_share.compose.dynamic.PluginsRegistry
import xcj.app.compose_share.compose.util.FileUtils
import xcj.app.core.android.ApplicationHelper

class ComposeDynamicUseCase(private val coroutineScope: CoroutineScope) : ComposeMethodsAware {
    private val TAG = "ComposeDynamicUseCase"
    var composeMethodsState: MutableList<Pair<IComposeMethods, @Composable () -> Unit>> =
        mutableStateListOf()

    override fun setMethodsContainer(methods: MutableList<Pair<IComposeMethods, @Composable () -> Unit>>?) {
        if (methods.isNullOrEmpty()) {
            if (composeMethodsState.isNotEmpty())
                composeMethodsState.clear()
        } else {
            if (composeMethodsState.isNotEmpty())
                composeMethodsState.clear()
            composeMethodsState.addAll(methods)
        }
    }

    fun doLoad() {
        PluginsRegistry.loadMethodsToContainer(coroutineScope, this)
    }

    fun onAddClick(context: Context) {
        PlatformUseCase.openSystemFileProvider(
            context, 2221,
            "application/vnd.android.package-archive"
        )
    }

    fun onExternalAARFileSelectActivityResult(
        context: Context,
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        if (requestCode != 2221 || resultCode != Activity.RESULT_OK || data == null || data.data == null)
            return
        val desFilePath = FileUtils.copyFileToInternalStorage(
            context, data.data!!,
            ApplicationHelper.getContextFileDir().dynamicAARDir,
            null
        )
        if (desFilePath.isNullOrEmpty()) {
            Log.e(TAG, "onExternalAARFileSelectActivityResult early return, desFilePath is null")
            return
        }
        PluginsRegistry.registerAARFromExternal(desFilePath, ::doLoad)
    }

    fun onDeleteClick(iComposeMethods: IComposeMethods) {
        val tempMethods = composeMethodsState
        if (tempMethods.isEmpty())
            return
        val aarName = iComposeMethods.getAARName()
        if (aarName.isNullOrEmpty())
            return
        try {
            val iterator = tempMethods.iterator()
            while (iterator.hasNext()) {
                val next = iterator.next()
                if (next.first == iComposeMethods) {
                    iterator.remove()
                }
            }
            iComposeMethods.onComposeDispose("by delete")
            iComposeMethods.getStatesHolder().onUnLoad()
            PluginsRegistry.unRegisterByAARByUser(aarName, iComposeMethods)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun onParentComposeDispose() {
        composeMethodsState.forEach { it.first.onComposeDispose("parent dispose") }
        setMethodsContainer(null)
    }
}

class PlatformUseCase {
    companion object {
        fun openSystemFileProvider(context: Context, requestCode: Int, mimeType: String = "*/*") {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = mimeType
                // Optionally, specify a URI for the file that should appear in the
                // system file picker when it loads.
                addFlags(
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            }

            (context as AppCompatActivity).startActivityForResult(intent, requestCode)
        }
    }
}