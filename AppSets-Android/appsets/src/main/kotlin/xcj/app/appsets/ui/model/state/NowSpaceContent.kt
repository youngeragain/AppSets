package xcj.app.appsets.ui.model.state

import xcj.app.appsets.im.Session
import xcj.app.appsets.server.model.UpdateCheckResult
import xcj.app.appsets.ui.model.TipsProvider
import xcj.app.starter.android.ui.model.PlatformPermissionsUsage
import xcj.app.starter.android.util.UriProvider
import java.util.UUID

sealed interface NowSpaceContent {

    val timeout: Int

    val id: String

    data class Demo(
        val tips: String,
        val subTips: String,
        override val id: String = UUID.randomUUID().toString(),
        override val timeout: Int = Int.MAX_VALUE
    ) : NowSpaceContent

    data class AppVersionChecked(
        val updateCheckResult: UpdateCheckResult,
        override val id: String = UUID.randomUUID().toString(),
        override val timeout: Int = Int.MAX_VALUE
    ) : NowSpaceContent

    data class IMMessage(
        val session: Session,
        val message: xcj.app.appsets.im.message.IMMessage<*>,
        override val timeout: Int = Int.MAX_VALUE
    ) : NowSpaceContent {
        override val id: String
            get() = message.id
    }

    data class PlatformPermissionUsageTips(
        val platformPermissionsUsages: List<PlatformPermissionsUsage>,
        override val tips: Int,
        override val subTips: Int,
        override val id: String = UUID.randomUUID().toString(),
        override val timeout: Int = Int.MAX_VALUE
    ) : NowSpaceContent, TipsProvider


    data class AudioPlayer(
        val uriProvider: UriProvider,
        override val id: String = UUID.randomUUID().toString(),
        override val tips: Int? = null,
        override val subTips: Int? = null,
        override val timeout: Int = Int.MAX_VALUE
    ) : NowSpaceContent, TipsProvider

}