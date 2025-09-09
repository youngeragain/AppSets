package xcj.app.appsets.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xcj.app.appsets.service.MainService
import xcj.app.starter.android.util.LocalMessenger
import xcj.app.starter.android.util.PurpleLogger

class LastSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "LastSyncWorker"
        const val MESSAGE_KEY_DATA_SYNC_FINISH = "data_sync_finish"
    }

    override suspend fun doWork(): Result {
        PurpleLogger.current.d(TAG, "doWork")
        LocalMessenger.post(MESSAGE_KEY_DATA_SYNC_FINISH, true)
        applicationContext.stopService(Intent(applicationContext, MainService::class.java))
        return Result.success()
    }
}