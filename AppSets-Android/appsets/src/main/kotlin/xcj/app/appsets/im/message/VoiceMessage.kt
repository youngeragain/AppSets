package xcj.app.appsets.im.message

import android.content.Context
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import xcj.app.appsets.im.IMMessageDesignType
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.util.model.UriProvider
import java.util.Date
import java.util.UUID

data class VoiceMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Date,
    override val fromInfo: MessageFromInfo,
    override val toInfo: MessageToInfo,
    override val messageGroupTag: String?,
    override val metadata: StringMessageMetadata,
    override val messageType: String = IMMessageDesignType.TYPE_VOICE
) : IMMessage<StringMessageMetadata>() {
    override fun readableContent(context: Context): String {
        return "(${ContextCompat.getString(context, xcj.app.appsets.R.string.voice)})"
    }
}

fun VoiceMessage.requireUri(): Uri? {
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