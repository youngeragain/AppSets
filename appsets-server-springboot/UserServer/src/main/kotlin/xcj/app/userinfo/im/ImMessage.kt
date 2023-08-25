package xcj.app.userinfo.im

import java.util.*

class ImMessage(
    val id: String,
    val content: String,
    val rawContent: String?,
    val msgFromInfo: MessageFromInfo,
    val date: Date,
    val msgToInfo: MessageToInfo,
    val groupMessageTag: String?,
){
    val contentType: String = "application/*"
}