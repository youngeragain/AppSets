package xcj.app.appsets.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import xcj.app.appsets.worker.LastSyncWorker
import xcj.app.appsets.worker.LocalSyncWorker
import xcj.app.appsets.worker.ServerSyncWorker
import xcj.app.starter.android.util.PurpleLogger

class DataSyncService : Service() {
    companion object {
        private const val TAG = "DataSyncService"
        const val CHANNEL_ID = TAG
        const val FOREGROUND_NOTIFICATION_ID = 1
        const val KEY_WHAT_TO_DO = "what_to_do"
        const val DO_TO_SYNC_USER_FRIENDS_FROM_SERVER = "to_sync_user_friends_from_server"
        const val DO_TO_SYNC_USER_GROUPS_FROM_SERVER = "to_sync_user_groups_from_server"
        const val DO_TO_SYNC_USER_DATA_FROM_SERVER = "to_sync_user_data_from_server"
        const val DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL = "to_sync_user_friends_from_local"
        const val DO_TO_SYNC_USER_GROUPS_FROM_LOCAL = "to_sync_user_groups_from_local"

        const val DO_TO_SYNC_USER_DATA_FROM_LOCAL = "to_sync_user_data_from_local"
    }

    private fun isForegroundNotificationIsShowing(): Boolean {
        val notificationManagerCompat = NotificationManagerCompat.from(this)
        val activeNotifications = notificationManagerCompat.activeNotifications
        for (notification in activeNotifications) {
            if (notification.id == IMService.Companion.FOREGROUND_NOTIFICATION_ID) {
                return true
            }
        }
        return false
    }

    private fun startForeground(by: String? = null) {
        PurpleLogger.current.d(TAG, "startForeground, by:$by")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(xcj.app.appsets.R.string.app_notification_channel_data_sync))
            .setContentText(getString(xcj.app.appsets.R.string.app_notification_channel_data_sync_description))
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        PurpleLogger.current.d(TAG, "onStartCommand")
        if (intent.hasExtra(KEY_WHAT_TO_DO)) {
            val whatToDo = intent.getStringExtra(KEY_WHAT_TO_DO)
            PurpleLogger.current.d(TAG, "onStartCommand, what_to_do:${whatToDo}")
            if (whatToDo != null) {
                startForeground()
                when (whatToDo) {
                    DO_TO_SYNC_USER_DATA_FROM_SERVER -> toSyncUserDataFromServer("friends,groups")
                    DO_TO_SYNC_USER_FRIENDS_FROM_SERVER -> toSyncUserDataFromServer("friends")
                    DO_TO_SYNC_USER_GROUPS_FROM_SERVER -> toSyncUserDataFromServer("groups")
                    DO_TO_SYNC_USER_DATA_FROM_LOCAL -> toSyncUserDataFromLocal("friends,groups")
                    DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL -> toSyncUserDataFromLocal("friends")
                    DO_TO_SYNC_USER_GROUPS_FROM_LOCAL -> toSyncUserDataFromLocal("groups")
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("RestrictedApi")
    private fun toSyncUserDataFromServer(conditions: String? = null) {
        PurpleLogger.current.d(TAG, "toSyncUserDataFromServer, conditions:${conditions}")
        if (conditions.isNullOrEmpty()) {
            return
        }
        val builder = OneTimeWorkRequest.Builder(ServerSyncWorker::class.java)
        val data = androidx.work.Data.Builder().putAll(mapOf("conditions" to conditions)).build()
        builder.setInputData(data)
        val dataSyncRequest = builder.build()
        val lastSyncWorker = OneTimeWorkRequest.from(LastSyncWorker::class.java)
        WorkManager.getInstance(this).beginWith(dataSyncRequest).then(lastSyncWorker)
            .enqueue()
    }

    @SuppressLint("RestrictedApi")
    private fun toSyncUserDataFromLocal(conditions: String? = null) {
        PurpleLogger.current.d(TAG, "toSyncUserDataFromLocal, conditions:${conditions}")

        if (conditions.isNullOrEmpty()) {
            return
        }
        val builder = OneTimeWorkRequest.Builder(LocalSyncWorker::class.java)
        val data = androidx.work.Data.Builder().putAll(mapOf("conditions" to conditions)).build()
        builder.setInputData(data)
        val dataSyncRequest = builder.build()
        val lastSyncWorker = OneTimeWorkRequest.from(LastSyncWorker::class.java)
        WorkManager.getInstance(this).beginWith(dataSyncRequest).then(lastSyncWorker)
            .enqueue()
    }

    override fun onBind(intent: Intent?): IBinder? {
        PurpleLogger.current.d(TAG, "onBind")
        return null
    }

    override fun onUnbind(intent: Intent?): Boolean {
        PurpleLogger.current.d(TAG, "onUnbind")
        return super.onUnbind(intent)
    }

}