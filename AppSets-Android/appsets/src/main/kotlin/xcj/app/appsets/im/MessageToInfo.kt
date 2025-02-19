package xcj.app.appsets.im

import xcj.app.appsets.im.message.ImMessage
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.starter.android.util.PurpleLogger

data class MessageToInfo(
    val toType: String,
    override val id: String,
    override val name: String?,
    val iconUrl: String?,
    val roles: String?
) : Bio {
    override var bioUrl: Any? = null

    companion object {

        private const val TAG = "MessageToInfo"

        fun fromImObj(imObj: ImObj): MessageToInfo {
            val iconUrl = when (imObj.bio) {
                is UserInfo -> imObj.bio.avatarUrl
                is GroupInfo -> imObj.bio.iconUrl
                is Application -> imObj.bio.iconUrl
                else -> imObj.bio.bioUrl
            }
            val messageToInfo = when (imObj) {
                is ImObj.ImSingle -> {
                    MessageToInfo(
                        toType = ImMessage.TYPE_O2O,
                        id = imObj.bio.id,
                        name = imObj.bio.name,
                        iconUrl = iconUrl?.toString(),
                        roles = imObj.userRoles
                    )
                }

                is ImObj.ImGroup -> {
                    MessageToInfo(
                        toType = ImMessage.TYPE_O2M,
                        id = imObj.bio.id,
                        name = imObj.bio.name,
                        iconUrl = iconUrl?.toString(),
                        roles = null
                    )
                }
            }
            PurpleLogger.current.d(
                TAG,
                "fromImObj, imObj:$imObj, " +
                        "bio:${imObj.bio}, " +
                        "messageToInfo:$messageToInfo"
            )
            return messageToInfo
        }

    }
}