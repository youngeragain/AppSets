package xcj.app.appsets.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import xcj.app.appsets.db.room.repository.UserGroupsRoomRepository
import xcj.app.appsets.db.room.repository.UserInfoRoomRepository
import xcj.app.appsets.ktx.requestNotNullBridge
import xcj.app.appsets.server.api.URLApi
import xcj.app.appsets.server.api.UserApi
import xcj.app.appsets.server.repository.UserRepository
import xcj.app.appsets.usecase.UserRelationsCase

class ServerSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val conditions = inputData.getString("conditions")
        Log.e("ServerSyncWorker", "conditions:${conditions}")

        if (conditions.isNullOrEmpty())
            return Result.failure()
        var hasSingleCondition: Boolean = false
        if (conditions.contains("friends")) {
            hasSingleCondition = true
            val friends = requestNotNullBridge {
                UserRepository(URLApi.provide(UserApi::class.java)).getFriends()
            }
            Log.e("ServerSyncWorker", "friends:$friends")
            if (!friends.isNullOrEmpty()) {
                Log.e("ServerSyncWorker", "add friends to local room db")
                UserInfoRoomRepository.getInstance().addRelatedUserInfoList(friends)
                UserRelationsCase.getInstance()
                    .initRelatedUsersFromNew(friends.map { it.uid }.toSet())
            }
        }

        if (conditions.contains("groups")) {
            hasSingleCondition = true
            val groups = requestNotNullBridge {
                UserRepository(URLApi.provide(UserApi::class.java)).getChatGroups()
            }
            Log.e("ServerSyncWorker", "groups:$groups")
            if (!groups.isNullOrEmpty()) {
                val groupIdMap = mutableMapOf<String, Set<String>?>()
                groups.forEach {
                    groupIdMap[it.groupId] =
                        it.userInfoList?.map { userInfo -> userInfo.uid }?.toSet()
                }
                Log.e("ServerSyncWorker", "add groups to local room db")
                UserRelationsCase.getInstance().initRelatedGroupsFromNew(groupIdMap)
                UserGroupsRoomRepository.getInstance().addRelatedGroupInfoList(groups)
            }
        }
        if (hasSingleCondition) {
            return Result.success()
        }
        return Result.failure()
    }
}