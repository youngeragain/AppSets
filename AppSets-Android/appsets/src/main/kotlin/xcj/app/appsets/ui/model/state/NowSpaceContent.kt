package xcj.app.appsets.ui.model.state

import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage

sealed interface NowSpaceContent {

    data class NewImMessage(
        val session: Session,
        val imMessage: ImMessage
    ) : NowSpaceContent

    data object Nothing : NowSpaceContent

}