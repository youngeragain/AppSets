package xcj.app.appsets.im.message

import xcj.app.starter.util.ContentType

open class StringMessageMetadata(
    description: String,
    size: Int,
    compressed: Boolean,
    encode: String,
    data: String,
    contentType: String,
) : MessageMetadata<String>(
    description, size, compressed, encode, data, contentType
) {
    companion object {
        fun fromString(content: String): StringMessageMetadata {
            return StringMessageMetadata(
                "",
                0,
                false,
                "none",
                content,
                ContentType.APPLICATION_TEXT
            )
        }
    }
}