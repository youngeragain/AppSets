package xcj.app.appsets.usecase

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import xcj.app.appsets.account.UserAccountStateAware
import xcj.app.appsets.ui.model.state.NowSpaceContent
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage
import xcj.app.starter.android.util.PurpleLogger

class NowSpaceContentUseCase() : ComposeLifecycleAware, UserAccountStateAware {

    companion object {
        private const val TAG = "NowSpaceContentUseCase"
    }

    private var _oldContent: NowSpaceContent = NowSpaceContent.Nothing

    val oldContent: NowSpaceContent
        get() = _oldContent

    private val _content: MutableState<NowSpaceContent> =
        mutableStateOf(NowSpaceContent.Nothing)

    val content: State<NowSpaceContent> = _content

    fun addNowSpaceContent(nowSpaceContent: NowSpaceContent) {
        PurpleLogger.current.d(TAG, "addNowSpaceContent, nowSpaceContent:$nowSpaceContent")
        _oldContent = content.value
        _content.value = nowSpaceContent
    }

    fun contentTypeIsSameAsLast(): Boolean {
        return content.value::class == oldContent::class
    }

    fun removeContent() {
        PurpleLogger.current.d(TAG, "removeContent")
        _oldContent = content.value
        _content.value = NowSpaceContent.Nothing
    }

    fun removeContentIf(test: (NowSpaceContent) -> Boolean) {
        PurpleLogger.current.d(TAG, "removeContentIf")
        if (test(content.value)) {
            removeContent()
        }
    }

    override fun onComposeDispose(by: String?) {

    }

    override fun onUserLogout(by: String?) {
        removeContent()
    }

    suspend fun showPlatformPermissionUsageTipsIfNeeded(
        context: Context,
        showFlow: Flow<Boolean> = flowOf(true),
        platformPermissionsUsagesProvider: (Context) -> List<PlatformPermissionsUsage> = { context ->
            PlatformPermissionsUsage.provideAll(context)
        }
    ) {
        showFlow.collect { show ->
            if (!show) {
                return@collect
            }
            val platformPermissionsUsages = platformPermissionsUsagesProvider(context)
            val platformPermissionUsageTips = NowSpaceContent.PlatformPermissionUsageTips(
                tips = xcj.app.appsets.R.string.app_platform_permissions_useage_tips,
                subTips = xcj.app.appsets.R.string.app_platform_permissions_useage_tips_des,
                platformPermissionsUsages = platformPermissionsUsages
            )
            addNowSpaceContent(platformPermissionUsageTips)
        }
    }
}