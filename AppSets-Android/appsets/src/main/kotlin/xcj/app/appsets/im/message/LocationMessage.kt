package xcj.app.appsets.im.message

import android.content.Context
import androidx.core.content.ContextCompat
import xcj.app.appsets.im.IMMessageDesignType
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import java.util.Date
import java.util.UUID

data class LocationMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Date,
    override val fromInfo: MessageFromInfo,
    override val toInfo: MessageToInfo,
    override val messageGroupTag: String?,
    override val metadata: LocationMessageMetadata,
    override val messageType: String = IMMessageDesignType.TYPE_LOCATION
) : IMMessage<LocationMessageMetadata>() {
    override fun readableContent(context: Context): String {
        return "(${
            ContextCompat.getString(
                context,
                xcj.app.appsets.R.string.location
            )
        })"
    }
}