package xcj.app.appsets.usecase

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.account.UserAccountStateAware
import xcj.app.appsets.settings.AppSetsModuleSettings
import xcj.app.appsets.ui.model.state.NowSpaceContent
import xcj.app.compose_share.dynamic.ComposeLifecycleAware
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
        directToShow: Boolean = false
    ) {
        if (directToShow) {
            val platformPermissionUsageTips = NowSpaceContent.PlatformPermissionUsageTips(
                tips = xcj.app.appsets.R.string.app_platform_permissions_useage_tips,
                subTips = xcj.app.appsets.R.string.app_platform_permissions_useage_tips_des,
            )
            addNowSpaceContent(platformPermissionUsageTips)
            return
        }
        AppSetsModuleSettings.get().isAppFirstLaunch().collect { isFistLaunch ->
            if (!isFistLaunch) {
                return@collect
            }
            val platformPermissionUsageTips = NowSpaceContent.PlatformPermissionUsageTips(
                tips = xcj.app.appsets.R.string.app_platform_permissions_useage_tips,
                subTips = xcj.app.appsets.R.string.app_platform_permissions_useage_tips_des,
            )
            addNowSpaceContent(platformPermissionUsageTips)
        }
    }
}