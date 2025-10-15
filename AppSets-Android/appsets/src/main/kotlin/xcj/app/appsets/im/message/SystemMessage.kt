package xcj.app.appsets.im.message

import android.content.Context
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import xcj.app.appsets.im.IMMessageDesignType
import xcj.app.appsets.im.IMMessageGenerator
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
import xcj.app.appsets.im.model.FriendRequestJson
import xcj.app.appsets.im.model.GroupRequestJson
import xcj.app.appsets.im.model.RequestFeedbackJson
import xcj.app.appsets.im.model.SystemContentInterface
import xcj.app.appsets.im.model.SystemContentJson
import java.util.Date
import java.util.UUID

data class SystemMessage(
    override val id: String = UUID.randomUUID().toString(),
    override val timestamp: Date,
    override val fromInfo: MessageFromInfo,
    override val toInfo: MessageToInfo,
    override val messageGroupTag: String?,
    override val metadata: StringMessageMetadata,
    override val messageType: String = IMMessageDesignType.TYPE_SYSTEM
) : IMMessage<StringMessageMetadata>() {

    private var systemContentInterfaceCached: SystemContentInterface? = null

    val systemContentInterface: SystemContentInterface?
        get() {
            if (systemContentInterfaceCached != null) {
                return systemContentInterfaceCached
            }
            runCatching {
                val systemContentJson = IMMessageGenerator.gson.fromJson<SystemContentJson>(
                    metadata.data,
                    SystemContentJson::class.java
                )
                val contentInterface =
                    systemContentJson.getContentObject(IMMessageGenerator.gson)
                systemContentInterfaceCached = contentInterface
                return contentInterface
            }
            return null
        }

    val handling: MutableState<Boolean> = mutableStateOf(false)

    override fun readableContent(context: Context): String {
        val systemContentInterface = systemContentInterface
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


}