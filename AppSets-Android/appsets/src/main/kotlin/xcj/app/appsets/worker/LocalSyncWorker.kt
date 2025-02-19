package xcj.app.appsets.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.starter.android.util.PurpleLogger

class LocalSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    companion object {
        private const val TAG = "LocalSyncWorker"
    }

    override suspend fun doWork(): Result {
        PurpleLogger.current.d(TAG, "doWork")
        RelationsUseCase.getInstance().initRelationFromLocalDB()
        return Result.success()
    }
}