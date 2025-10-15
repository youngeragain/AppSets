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

data class VideoMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Date,
    override val fromInfo: MessageFromInfo,
    override val toInfo: MessageToInfo,
    override val messageGroupTag: String?,
    override val metadata: VideoMessageMetadata,
    override val messageType: String = IMMessageDesignType.TYPE_VIDEO
) : IMMessage<VideoMessageMetadata>() {
    override fun readableContent(context: Context): String {
        return "(${ContextCompat.getString(context, xcj.app.appsets.R.string.video)})"
    }
}

fun VideoMessage.requireUri(): Pair<Uri?, Uri?>? {
    if (isReceivedMessage) {
        return metadata.url?.toUri() to metadata.companionUrl?.toUri()
    }

    val messageSendInfo = messageSending?.sendInfoState?.value
    if (messageSendInfo == null) {
        return null
    }
    if (messageSendInfo.isSent) {
        return metadata.url?.toUri() to metadata.companionUrl?.toUri()
    } else {
        val pair = metadata.localData as? Pair<UriProvider, UriProvider>
        return pair?.first?.provideUri() to pair?.second?.provideUri()
    }
}