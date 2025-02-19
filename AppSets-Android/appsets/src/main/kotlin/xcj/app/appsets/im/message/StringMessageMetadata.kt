package xcj.app.appsets.im.message

open class StringMessageMetadata(
    description: String,
    size: Int,
    compressed: Boolean,
    encode: String,
    data: String,
    contentType: String,
) : MessageMetadata<String>(
    description, size, compressed, encode, data, contentType
)