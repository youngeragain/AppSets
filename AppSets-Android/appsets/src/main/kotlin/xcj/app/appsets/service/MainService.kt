package xcj.app.appsets.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import xcj.app.appsets.worker.LastSyncWorker
import xcj.app.appsets.worker.LocalSyncWorker
import xcj.app.appsets.worker.ServerSyncWorker
import xcj.app.starter.android.util.PurpleLogger

class MainService : Service() {
    companion object {
        private const val TAG = "MainService"
        const val CHANNEL_ID = TAG
        const val KEY_WHAT_TO_DO = "what_to_do"
        const val DO_TO_SYNC_USER_FRIENDS_FROM_SERVER = "to_sync_user_friends_from_server"
        const val DO_TO_SYNC_USER_GROUPS_FROM_SERVER = "to_sync_user_groups_from_server"
        const val DO_TO_SYNC_USER_DATA_FROM_SERVER = "to_sync_user_data_from_server"
        const val DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL = "to_sync_user_friends_from_local"
        const val DO_TO_SYNC_USER_GROUPS_FROM_LOCAL = "to_sync_user_groups_from_local"
        const val DO_TO_SYNC_USER_DATA_FROM_LOCAL = "to_sync_user_data_from_local"
    }

    override fun onCreate() {
        PurpleLogger.current.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        PurpleLogger.current.d(TAG, "onDestroy")
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        PurpleLogger.current.d(TAG, "onStartCommand")
        intent?.getStringExtra(KEY_WHAT_TO_DO)?.let {
            PurpleLogger.current.d(TAG, "onStartCommand, what_to_do:${it}")
            startForeground()
            when (it) {
                DO_TO_SYNC_USER_DATA_FROM_SERVER -> toSyncUserDataFromServer("friends,groups")
                DO_TO_SYNC_USER_FRIENDS_FROM_SERVER -> toSyncUserDataFromServer("friends")
                DO_TO_SYNC_USER_GROUPS_FROM_SERVER -> toSyncUserDataFromServer("groups")
                DO_TO_SYNC_USER_DATA_FROM_LOCAL -> toSyncUserDataFromLocal("friends,groups")
                DO_TO_SYNC_USER_FRIENDS_FROM_LOCAL -> toSyncUserDataFromLocal("friends")
                DO_TO_SYNC_USER_GROUPS_FROM_LOCAL -> toSyncUserDataFromLocal("groups")
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun startForeground() {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(xcj.app.appsets.R.string.foreground_service))
            .setContentText(getString(xcj.app.appsets.R.string.service_running))
            .setSmallIcon(xcj.app.compose_share.R.drawable.ic_appsets_44)
            .setSound(null)
            .setSilent(true)
            .build()

        startForeground(1, notification) // 启动前台服务
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