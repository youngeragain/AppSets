package xcj.app.appsets.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xcj.app.appsets.ui.compose.DataSyncFinishEvent
import xcj.app.appsets.ui.compose.EventDispatcher

class LastSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        EventDispatcher.dispatchEvent(DataSyncFinishEvent().apply {
            payload = "sync_data_finish"
        })
        return Result.success()
    }
}