package xcj.app.appsets.im.message

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import xcj.app.appsets.im.ImMessageDesignType
import xcj.app.appsets.im.ImMessageGenerator
import xcj.app.appsets.im.MessageFromInfo
import xcj.app.appsets.im.MessageToInfo
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
    override val messageType: String = ImMessageDesignType.TYPE_SYSTEM
) : ImMessage() {

    private var systemContentInterfaceCached: SystemContentInterface? = null

    val systemContentInterface: SystemContentInterface?
        get() {
            if (systemContentInterfaceCached != null) {
                return systemContentInterfaceCached
            }
            runCatching {
                val systemContentJson = ImMessageGenerator.gson.fromJson<SystemContentJson>(
                    metadata.data,
                    SystemContentJson::class.java
                )
                val contentInterface =
                    systemContentJson.getContentObject(ImMessageGenerator.gson)
                systemContentInterfaceCached = contentInterface
                return contentInterface
            }
            return null
        }

    val handling: MutableState<Boolean> = mutableStateOf(false)
}