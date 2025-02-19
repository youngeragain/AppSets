package xcj.app.appsets.server.model

data class UpdateCheckResult(
    val versionCode: Int,
    val newestVersionCode: Int,
    val newestVersion: String?,
    val updateChangesHtml: String?,
    val forceUpdate: Boolean?,
    val downloadUrl: String?,
    val publishDateTime: String?,
    val canUpdate: Boolean
) {
    var versionFromTo: String? = null
}