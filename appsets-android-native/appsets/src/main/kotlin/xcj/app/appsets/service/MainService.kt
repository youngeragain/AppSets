package xcj.app.appsets.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.google.gson.Gson
import xcj.app.appsets.BuildConfig
import xcj.app.appsets.account.LocalAccountManager
import xcj.app.appsets.im.RabbitMqBroker
import xcj.app.appsets.im.RabbitMqBrokerConfig
import xcj.app.appsets.im.RabbitMqBrokerProperty
import xcj.app.appsets.usecase.UserRelationsCase
import xcj.app.appsets.worker.LastSyncWorker
import xcj.app.appsets.worker.LocalSyncWorker
import xcj.app.appsets.worker.ServerSyncWorker

class MainService : Service() {
    override fun onCreate() {
        super.onCreate()
        Log.i("MainService", "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i("MainService", "MainMessageService:onStartCommand")
        intent?.getStringExtra("what_to_do")?.let {
            when (it) {
                "to_sync_user_data_from_server" -> toSyncUserDataFromServer("friends,groups")
                "to_sync_user_friends_from_server" -> toSyncUserDataFromServer("friends")
                "to_sync_user_groups_from_server" -> toSyncUserDataFromServer("groups")
                "to_sync_user_data_from_local" -> toSyncUserDataFromLocal("friends,groups")
                "to_sync_user_friends_from_local" -> toSyncUserDataFromLocal("friends")
                "to_sync_user_groups_from_local" -> toSyncUserDataFromLocal("groups")
                "to_start_rabbit" -> toStartRabbit()
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }

    @SuppressLint("RestrictedApi")
    private fun toSyncUserDataFromServer(conditions: String? = null) {

        val builder = OneTimeWorkRequest.Builder(ServerSyncWorker::class.java)
        if (!conditions.isNullOrEmpty()) {
            val data = androidx.work.Data(mapOf("conditions" to conditions))
            builder.setInputData(data)
        }
        val dataSyncRequest = builder.build()
        val lastSyncWorker = OneTimeWorkRequest.from(LastSyncWorker::class.java)
        WorkManager.getInstance(this).beginWith(dataSyncRequest).then(lastSyncWorker)
            .enqueue()
    }

    @SuppressLint("RestrictedApi")
    private fun toSyncUserDataFromLocal(conditions: String? = null) {
        val builder = OneTimeWorkRequest.Builder(LocalSyncWorker::class.java)
        if (!conditions.isNullOrEmpty()) {
            val data = androidx.work.Data(mapOf("conditions" to conditions))
            builder.setInputData(data)
        }
        val dataSyncRequest = builder.build()
        val lastSyncWorker = OneTimeWorkRequest.from(LastSyncWorker::class.java)
        WorkManager.getInstance(this).beginWith(dataSyncRequest).then(lastSyncWorker)
            .enqueue()
    }

    private fun toStartRabbit() {
        if (LocalAccountManager._userInfo.value.isDefault()) {
            Log.e(
                "MainService",
                "start rabbit failed! because of _userInfo is default!"
            )
            return
        }
        if (BuildConfig.RabbitProperties == "{}") {
            Log.e(
                "MainService",
                "start rabbit failed! because of BuildConfig.RabbitProperties is:${BuildConfig.RabbitProperties}"
            )
            return
        }
        try {
            val rabbitProperties =
                Gson().fromJson(BuildConfig.RabbitProperties, RabbitMqBrokerProperty::class.java)
            rabbitProperties.uid = LocalAccountManager._userInfo.value.uid
            rabbitProperties.`user-exchange-groups` =
                UserRelationsCase.getInstance().relatedGroupIdMap?.keys?.joinToString(",")
            val rabbitMqBrokerConfig = RabbitMqBrokerConfig(rabbitProperties)
            RabbitMqBroker.bootstrap(rabbitMqBrokerConfig)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}