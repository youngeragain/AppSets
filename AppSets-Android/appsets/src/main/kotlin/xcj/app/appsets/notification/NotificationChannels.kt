package xcj.app.appsets.notification

import android.content.Context
import xcj.app.appsets.service.MainService

object NotificationChannels {

    const val CHANNEL_ID_CONVERSATION_1 = "Conversion_Channel_1"

    fun provide(context: Context): List<NotificationChannel> {
        val channels = mutableListOf<NotificationChannel>()
        channels.add(
            NotificationChannel(
                CHANNEL_ID_CONVERSATION_1,
                xcj.app.appsets.R.string.app_notification_channel_conversation_1_name,
                xcj.app.appsets.R.string.app_notification_channel_conversation_1_description
            )
        )
        channels.add(
            NotificationChannel(
                MainService.CHANNEL_ID,
                xcj.app.appsets.R.string.app_notification_channel_data_sync,
                xcj.app.appsets.R.string.app_notification_channel_data_sync_description
            )
        )
        return channels
    }

}