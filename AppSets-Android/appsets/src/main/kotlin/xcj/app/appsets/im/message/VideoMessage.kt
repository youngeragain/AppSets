package xcj.app.appsets.im.message

import android.net.Uri
import androidx.core.net.toUri
import xcj.app.appsets.im.IMMessageDesignType
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.util.model.UriProvider
import java.util.Date
import java.util.UUID

data class VideoMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Date,
    override val fromInfo: MessageFromInfo,
    override val toInfo: MessageToInfo,
    override val messageGroupTag: String?,
    override val metadata: MessageMetadata<*>,
    override val messageType: String = IMMessageDesignType.TYPE_VIDEO
) : IMMessage()

fun VideoMessage.requireUri(): Pair<Uri?, Uri?>? {
    val videoMessageMetadata = metadata as VideoMessageMetadata
    if (isReceivedMessage) {
        return videoMessageMetadata.url?.toUri() to videoMessageMetadata.companionUrl?.toUri()
    }

    val messageSendInfo = messageSending?.sendInfoState?.value
    if (messageSendInfo == null) {
        return null
    }
    if (messageSendInfo.isSent) {
        return videoMessageMetadata.url?.toUri() to videoMessageMetadata.companionUrl?.toUri()
    } else {
        val pair = videoMessageMetadata.localData as? Pair<UriProvider, UriProvider>
        return pair?.first?.provideUri() to pair?.second?.provideUri()
    }
}