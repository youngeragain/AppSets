package xcj.app.appsets.ui.model.state

import xcj.app.appsets.im.Session
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.ui.model.TipsProvider
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage
import java.util.UUID

sealed interface NowSpaceContent {

    val timeout: Int
        get() = Int.MAX_VALUE

    val id: String

    data class AppVersionChecked(
        val updateCheckResult: UpdateCheckResult,
        override val id: String = UUID.randomUUID().toString(),
        override val timeout: Int = Int.MAX_VALUE
    ) : NowSpaceContent

    data class IMMessage(
        val session: Session,
        val message: xcj.app.appsets.im.message.IMMessage,
        override val timeout: Int = Int.MAX_VALUE
    ) : NowSpaceContent {
        override val id: String
            get() = message.id
    }

    data class PlatformPermissionUsageTips(
        override val tips: Int,
        override val subTips: Int,
        val platformPermissionsUsages: List<PlatformPermissionsUsage>,
        override val id: String = UUID.randomUUID().toString(),
        override val timeout: Int = Int.MAX_VALUE
    ) : NowSpaceContent, TipsProvider

}