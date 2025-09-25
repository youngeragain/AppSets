package xcj.app.appsets.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import xcj.app.appsets.service.DataSyncService
import xcj.app.appsets.service.IMService
import xcj.app.appsets.service.MediaPlayback101Service


object NotificationChannels {

    const val CHANNEL_ID_CONVERSATION_1 = "Conversion_Channel_1"

    private fun getDesignedChannels(): List<DesignNotificationChannel> {
        val channels = listOf(
            DesignNotificationChannel(
                DataSyncService.CHANNEL_ID,
                xcj.app.appsets.R.string.app_notification_channel_data_sync,
                xcj.app.appsets.R.string.app_notification_channel_data_sync_description,
                NotificationManager.IMPORTANCE_MIN
            ),
            DesignNotificationChannel(
                IMService.CHANNEL_ID,
                xcj.app.appsets.R.string.app_notification_channel_im_service,
                xcj.app.appsets.R.string.app_notification_channel_im_service_description,
                NotificationManager.IMPORTANCE_MIN
            ),
            DesignNotificationChannel(
                MediaPlayback101Service.CHANNEL_ID,
                xcj.app.appsets.R.string.notification_channel_media_playback,
                xcj.app.appsets.R.string.notification_channel_media_playback_description
            ),
            DesignNotificationChannel(
                CHANNEL_ID_CONVERSATION_1,
                xcj.app.appsets.R.string.app_notification_channel_conversation_1_name,
                xcj.app.appsets.R.string.app_notification_channel_conversation_1_description
            )
        )
        return channels
    }

    fun prepareToSystem(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val notificationManager: NotificationManagerCompat =
            NotificationManagerCompat.from(context)

        val designedNotificationChannels = getDesignedChannels()
        for (designedChannel in designedNotificationChannels) {
            if (notificationManager.getNotificationChannel(designedChannel.id) == null) {
                val platformSystemChannel =
                    NotificationChannel(
                        designedChannel.id,
                        context.getString(designedChannel.name),
                        designedChannel.importance
                    )
                platformSystemChannel.description = context.getString(designedChannel.description)
                notificationManager.createNotificationChannel(platformSystemChannel)
            }
        }
    }
}