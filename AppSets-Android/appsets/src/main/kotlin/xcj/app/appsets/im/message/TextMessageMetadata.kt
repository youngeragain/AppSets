package xcj.app.appsets.im.message

class TextMessageMetadata(
    description: String,
    size: Int,
    compressed: Boolean,
    encode: String,
    data: String,
    contentType: String,
    val textStyle: ImMessageTextStyle? = null
) : StringMessageMetadata(
    description, size, compressed, encode, data, contentType
) {
    data class ImMessageTextStyle(val textSize: Int)
}