package xcj.app.appsets.ui.model

import xcj.app.appsets.im.Session
import xcj.app.appsets.im.message.ImMessage

sealed interface NowSpaceObjectState {

    data class NewImMessage(val session: Session, val imMessage: ImMessage) : NowSpaceObjectState

    data object NULL : NowSpaceObjectState

}