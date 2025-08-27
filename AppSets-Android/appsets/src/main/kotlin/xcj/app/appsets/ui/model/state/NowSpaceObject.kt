package xcj.app.appsets.ui.model.state

import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage

sealed interface NowSpaceObject {

    data class NewImMessage(
        val session: Session,
        val imMessage: ImMessage
    ) : NowSpaceObject

    data object NULL : NowSpaceObject

}