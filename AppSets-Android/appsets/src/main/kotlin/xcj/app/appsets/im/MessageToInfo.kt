package xcj.app.appsets.im

import xcj.app.appsets.im.message.IMMessage
import xcj.app.appsets.server.model.Application
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.server.model.UserInfo
import xcj.app.starter.android.util.PurpleLogger

data class MessageToInfo(
    val toType: String,
    val iconUrl: String?,
    val roles: String?,
    val id: String,
    val name: String?
) : Bio {
    override val bioId: String
        get() = id
    override val bioName: String?
        get() = name ?: id
    override var bioUrl: Any? = null

    companion object {

        private const val TAG = "MessageToInfo"

        fun fromImObj(imObj: IMObj): MessageToInfo {
            val iconUrl = when (imObj.bio) {
                is UserInfo -> imObj.bio.avatarUrl
                is GroupInfo -> imObj.bio.iconUrl
                is Application -> imObj.bio.iconUrl
                else -> imObj.bio.bioUrl
            }
            val messageToInfo = when (imObj) {
                is IMObj.IMSingle -> {
                    MessageToInfo(
                        toType = IMMessage.TYPE_O2O,
                        iconUrl = iconUrl?.toString(),
                        roles = imObj.userRoles,
                        id = imObj.bio.bioId,
                        name = imObj.bio.bioName,
                    )
                }

                is IMObj.IMGroup -> {
                    MessageToInfo(
                        toType = IMMessage.TYPE_O2M,
                        iconUrl = iconUrl?.toString(),
                        roles = null,
                        id = imObj.bio.bioId,
                        name = imObj.bio.bioName,
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