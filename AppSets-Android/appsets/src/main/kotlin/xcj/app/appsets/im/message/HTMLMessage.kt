package xcj.app.appsets.im.message

import xcj.app.appsets.im.IMMessageDesignType
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import java.util.Date
import java.util.UUID

data class HTMLMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Date,
    override val fromInfo: MessageFromInfo,
    override val toInfo: MessageToInfo,
    override val messageGroupTag: String?,
    override val metadata: StringMessageMetadata,
    override val messageType: String = IMMessageDesignType.TYPE_HTML
) : IMMessage()