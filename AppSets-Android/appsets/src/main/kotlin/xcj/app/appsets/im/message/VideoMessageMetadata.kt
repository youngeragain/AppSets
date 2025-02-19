package xcj.app.appsets.im.message

class VideoMessageMetadata(
    description: String,
    size: Int,
    compressed: Boolean,
    encode: String,
    contentType: String,
    data: String,
    var companionData: String
) : StringMessageMetadata(
    description, size, compressed, encode, data, contentType
) {
    @Transient
    var companionUrl: String? = null
}