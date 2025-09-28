package xcj.app.appsets.im.message

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import xcj.app.appsets.im.IMMessageDesignType
import xcj.app.appsets.im.IMMessageGenerator
import xcj.app.appsets.im.IMObj
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupRequestJson
import xcj.app.appsets.im.model.RequestFeedbackJson
import xcj.app.starter.util.ContentType
import java.util.Date

data class MessageSendInfo(
    val progress: Float = 0f,
    val isSent: Boolean = false,
    val failureReason: String? = null
)

class MessageSending {
    val sendInfoState: MutableState<MessageSendInfo?> = mutableStateOf(null)

    fun updateSendInfo(messageSendInfo: MessageSendInfo) {
        sendInfoState.value = messageSendInfo
    }
}


abstract class IMMessage {

    companion object {

        const val KEY_SESSION_ID = "session_id"
        const val KEY_IM_MESSAGE_ID = "im_message_id"
        const val KEY_IM_MESSAGE_NOTIFICATION_ID = "im_message_notification_id"

        const val TYPE_O2O = "one2one"
        const val TYPE_O2M = "one2many"

        const val HEADER_MESSAGE_MESSAGE_DELIVERY_TYPE = "a1"
        const val HEADER_MESSAGE_ID = "a2"
        const val HEADER_MESSAGE_UID = "a3"
        const val HEADER_MESSAGE_NAME = "a4"
        const val HEADER_MESSAGE_NAME_BASE64 = "a5"
        const val HEADER_MESSAGE_AVATAR_URL = "a6"
        const val HEADER_MESSAGE_ROLES = "a7"
        const val HEADER_MESSAGE_MESSAGE_GROUP_TAG = "a8"
        const val HEADER_MESSAGE_TO_ID = "a9"
        const val HEADER_MESSAGE_TO_NAME = "a10"
        const val HEADER_MESSAGE_TO_NAME_BASE64 = "a11"
        const val HEADER_MESSAGE_TO_TYPE = "a12"
        const val HEADER_MESSAGE_TO_ICON_URL = "a13"
        const val HEADER_MESSAGE_TO_ROLES = "a14"

        @JvmStatic
        fun readableContent(context: Context, imMessage: IMMessage?): String? {
            if (imMessage == null) {
                return null
            }
            when (imMessage) {

                is TextMessage -> {
                    if (imMessage.metadata.data == null) {
                        return null
                    }
                    return imMessage.metadata.data.toString()
                }

                is HTMLMessage -> {
                    return "(${
                        ContextCompat.getString(
                            context,
                            xcj.app.appsets.R.string.web_content
                        )
                    })"
                }

                is AdMessage -> {
                    return "(${
                        ContextCompat.getString(
                            context,
                            xcj.app.appsets.R.string.advertisement
                        )
                    })"
                }

                is LocationMessage -> {
                    return "(${
                        ContextCompat.getString(
                            context,
                            xcj.app.appsets.R.string.location
                        )
                    })"
                }

                is MusicMessage -> {
                    return "(${ContextCompat.getString(context, xcj.app.appsets.R.string.music)})"
                }

                is VideoMessage -> {
                    return "(${ContextCompat.getString(context, xcj.app.appsets.R.string.video)})"
                }

                is VoiceMessage -> {
                    return "(${ContextCompat.getString(context, xcj.app.appsets.R.string.voice)})"
                }

                is FileMessage -> {
                    return "(${ContextCompat.getString(context, xcj.app.appsets.R.string.file)})"
                }

                is ImageMessage -> {
                    return "(${ContextCompat.getString(context, xcj.app.appsets.R.string.image)})"
                }

                is SystemMessage -> {
                    val systemContentInterface = imMessage.systemContentInterface
                    when (systemContentInterface) {
                        is FriendRequestJson -> {
                            return systemContentInterface.hello
                        }

                        is GroupRequestJson -> {
                            return systemContentInterface.hello
                        }

                        is RequestFeedbackJson -> {
                            return if (systemContentInterface.isAccept) {
                                ContextCompat.getString(
                                    context,
                                    xcj.app.appsets.R.string.your_request_has_passed
                                )
                            } else {
                                ContextCompat.getString(
                                    context,
                                    xcj.app.appsets.R.string.your_request_has_not_passed
                                )
                            }
                        }

                        else -> return "?"

                    }
                }

                else -> {
                    return ""
                }
            }
        }

        fun textImMessageMetadata(text: String): StringMessageMetadata {
            return StringMessageMetadata(
                "",
                0,
                false,
                "none",
                text,
                ContentType.APPLICATION_TEXT
            )
        }
    }

    abstract val id: String
    abstract val timestamp: Date
    abstract val fromInfo: MessageFromInfo
    abstract val toInfo: MessageToInfo
    abstract val messageGroupTag: String?
    abstract val metadata: MessageMetadata<*>

    /**
     * @see IMMessageDesignType
     */
    abstract val messageType: String

    var messageSending: MessageSending? = null

    fun updateSending(messageSendInfo: MessageSendInfo) {
        messageSending?.updateSendInfo(messageSendInfo)
    }

    val isSendMessage: Boolean
        get() = messageSending != null

    val isReceivedMessage: Boolean
        get() = messageSending == null

    val readableDate: String
        get() {
            return IMMessageGenerator.sdf.format(timestamp)
        }

    override fun toString(): String {
        return IMMessageGenerator.gson.toJson(this)
    }
}

/**
 * 解析消息To信息
 */
fun IMMessage.parseToImObj(): IMObj? {
    if (toInfo.toType == IMMessage.TYPE_O2M) {
        return IMObj.IMGroup(toInfo)
    }
    if (toInfo.toType == IMMessage.TYPE_O2O) {
        return IMObj.IMSingle(toInfo, toInfo.roles)
    }
    return null
}


/**
 * 解析消息from信息
 */
fun IMMessage.parseFromImObj(): IMObj? {
    if (toInfo.toType == IMMessage.TYPE_O2M) {
        return IMObj.IMGroup(toInfo)
    }
    if (toInfo.toType == IMMessage.TYPE_O2O) {
        return IMObj.IMSingle(fromInfo, fromInfo.roles)
    }
    return null
}