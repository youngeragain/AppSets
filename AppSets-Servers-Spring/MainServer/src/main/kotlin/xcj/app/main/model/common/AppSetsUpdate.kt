package xcj.app.main.model.common

import java.text.SimpleDateFormat
import java.util.*

data class UpdateCheckResult(
    val versionCode: Int,
    val newestVersionCode: Int,
    val newestVersion: String?,
    val updateChangesHtml: String?,
    val forceUpdate: Boolean?,
    val downloadUrl: String?,
    val publishDateTime: String?,
    val canUpdate: Boolean
)

data class AddAppSetsVersionForPlatformParams(
    val versionCode: Int,
    val version: String,
    val platform: String,
    val forceUpdate: Boolean,
    val updateChangesHtml: String,
    val downloadUrl: String?
)

data class AppSetsVersionForPlatform(
    val versionCode: Int,
    val version: String,
    val platform: String,
    val forceUpdate: Boolean,
    val updateChangesHtml: String,
    val downloadUrl: String?,
    val publishDateTime: String?,
    val publishTimestamp: Long?,
) {
    companion object {
        fun fromAddParams(addParams: AddAppSetsVersionForPlatformParams): AppSetsVersionForPlatform {
            val calendar = Calendar.getInstance()
            val simpleDateFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
            simpleDateFormat.timeZone = TimeZone.getTimeZone("Asia/Shanghai")
            val publishDateTime = simpleDateFormat.format(calendar.time)
            return AppSetsVersionForPlatform(
                addParams.versionCode, addParams.version,
                addParams.platform, addParams.forceUpdate, addParams.updateChangesHtml,
                addParams.downloadUrl,
                publishDateTime,
                calendar.time.time
            )
        }
    }
}