package xcj.app.appsets.server.model

/**
 * @param mediaFileUrl 主要文件url
 * @param mediaFileCompanionUrl 图片或视频的封面url
 */
data class ScreenMediaFileUrl(
    var mediaFileUrl: String,
    var mediaFileCompanionUrl: String?,
    var mediaType: String,
    var mediaDescription: String,
    var x18Content: Int?,
)