package xcj.app.appsets.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xcj.app.appsets.usecase.UserRelationsCase

class LocalSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        UserRelationsCase.getInstance().initFromDb()
        return Result.success()
    }
}