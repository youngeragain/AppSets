package xcj.app.appsets.im.message

import android.net.Uri
import androidx.core.net.toUri
import xcj.app.appsets.im.IMMessageDesignType
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.util.model.UriProvider
import java.util.Date
import java.util.UUID

data class MusicMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Date,
    override val fromInfo: MessageFromInfo,
    override val toInfo: MessageToInfo,
    override val messageGroupTag: String?,
    override val metadata: StringMessageMetadata,
    override val messageType: String = IMMessageDesignType.TYPE_MUSIC
) : IMMessage()

fun MusicMessage.requireUri(): Uri? {
    if (isReceivedMessage) {
        return metadata.url?.toUri()
    }

    val messageSendInfo = messageSending?.sendInfoState?.value
    if (messageSendInfo != null) {
        if (messageSendInfo.isSent) {
            return metadata.url?.toUri()
        } else {
            return (metadata.localData as? UriProvider)?.provideUri()
        }
    }
    return null
}