package xcj.app.appsets.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xcj.app.appsets.db.room.repository.GroupInfoRepository
import xcj.app.appsets.db.room.repository.UserInfoRepository
import xcj.app.starter.server.requestBridge
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.starter.android.util.PurpleLogger

class ServerSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    companion object {
        private const val TAG = "ServerSyncWorker"
    }

    override suspend fun doWork(): Result {
        PurpleLogger.current.d(TAG, "doWork, thread:${Thread.currentThread()}")
        val conditions = inputData.getString("conditions")
        PurpleLogger.current.d(TAG, "doWork, conditions:${conditions}")

        if (conditions.isNullOrEmpty()) {
            return Result.failure()
        }
        var hasSingleCondition: Boolean = false
        if (conditions.contains("friends")) {
            hasSingleCondition = true
            val friends = requestBridge(
                action = {
                    UserRepository.getInstance().getFriends()
                }
            )
            PurpleLogger.current.d(TAG, "doWork, friends:$friends")
            if (!friends.isNullOrEmpty()) {
                PurpleLogger.current.d(TAG, "doWork, add friends to local room db")
                UserInfoRepository.getInstance().addRelatedUserInfoList(friends)
                RelationsUseCase.getInstance()
                    .initRelatedUsersFromServer(friends.map { it.uid }.toSet())
            }
        }

        if (conditions.contains("groups")) {
            hasSingleCondition = true
            val groups = requestBridge(
                {
                    UserRepository.getInstance().getChatGroups()
                }
            )
            PurpleLogger.current.d(TAG, "doWork, groups:$groups")
            if (!groups.isNullOrEmpty()) {
                val groupIdMap = mutableMapOf<String, Set<String>?>()
                groups.forEach {
                    groupIdMap[it.groupId] =
                        it.userInfoList?.map { userInfo -> userInfo.uid }?.toSet()
                }
                PurpleLogger.current.d(TAG, "doWork, add groups to local room db")
                GroupInfoRepository.getInstance().addRelatedGroupInfoList(groups)
                RelationsUseCase.getInstance().initRelatedGroupsFromServer(groupIdMap)
            }
        }
        if (hasSingleCondition) {
            return Result.success()
        }
        return Result.failure()
    }
}