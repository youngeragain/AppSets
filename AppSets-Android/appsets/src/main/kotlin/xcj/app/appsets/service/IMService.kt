package xcj.app.appsets.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import xcj.app.starter.android.util.PurpleLogger

class IMService : Service() {
    companion object {
        private const val TAG = "IMService"
        const val CHANNEL_ID = TAG
        const val FOREGROUND_NOTIFICATION_ID = 2

        const val KEY_IS_APP_IN_BACKGROUND = "is_app_in_background"
    }

    private fun isForegroundNotificationIsShowing(): Boolean {
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        val activeNotifications = notificationManagerCompat.activeNotifications
        for (notification in activeNotifications) {
            if (notification.id == FOREGROUND_NOTIFICATION_ID) {
                return true
            }
        }
        return false
    }

    private fun startForeground(by: String? = null) {
        PurpleLogger.current.d(TAG, "startForeground, by:$by")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(xcj.app.appsets.R.string.app_notification_channel_im_service))
            .setContentText(getString(xcj.app.appsets.R.string.app_notification_channel_im_service_description))
            .setSmallIcon(xcj.app.compose_share.R.drawable.ic_appsets_44)
            .setSound(null)
            .setSilent(true)
            .build()

        startForeground(FOREGROUND_NOTIFICATION_ID, notification)
    }

    private fun stopForeground(by: String? = null) {
        val isForegroundNotificationIsShowing = isForegroundNotificationIsShowing()
        PurpleLogger.current.d(
            TAG,
            "stopForeground, isForegroundNotificationIsShowing:$isForegroundNotificationIsShowing, by:$by"
        )
        if (isForegroundNotificationIsShowing) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        }
    }

    override fun onCreate() {
        PurpleLogger.current.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        PurpleLogger.current.d(TAG, "onDestroy")
        stopForeground("onDestroy")
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground("onTaskRemoved")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        val onStartCommand = super.onStartCommand(intent, flags, startId)
        if (intent.hasExtra(KEY_IS_APP_IN_BACKGROUND)) {
            val appInBackground = intent.getBooleanExtra(KEY_IS_APP_IN_BACKGROUND, false)
            if (appInBackground) {
                startForeground("app_in_background")
            } else {
                stopForeground("app_not_in_background")
            }
        }
        return onStartCommand
    }
}