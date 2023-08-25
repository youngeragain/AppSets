package xcj.app.appsets.ui.nonecompose.base

import xcj.app.appsets.im.ImMessage
import xcj.app.appsets.util.NotificationPusher

interface NotificationPusherInterface {
    fun pushNotificationIfNeeded(
        notificationPusher: NotificationPusher,
        sessionId: String,
        imMessage: ImMessage
    )
}