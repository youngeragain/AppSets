package xcj.app.appsets.ui.model.state

import xcj.app.appsets.im.Session
import xcj.app.appsets.ui.model.TipsProvider
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage

sealed interface NowSpaceContent {

    data object Nothing : NowSpaceContent

    data class IMMessage(
        val session: Session,
        val imMessage: xcj.app.appsets.im.message.IMMessage
    ) : NowSpaceContent

    data class PlatformPermissionUsageTips(
        override val tips: Int,
        override val subTips: Int,
        val platformPermissionsUsages: List<PlatformPermissionsUsage>
    ) : NowSpaceContent, TipsProvider

}