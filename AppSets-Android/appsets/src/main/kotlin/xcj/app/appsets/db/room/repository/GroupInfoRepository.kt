package xcj.app.appsets.db.room.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.db.room.dao.GroupInfoDao
import xcj.app.appsets.db.room.dao.UserInfoDao
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.starter.android.util.PurpleLogger

class GroupInfoRepository(
    private val groupInfoDao: GroupInfoDao,
    private val userInfoDao: UserInfoDao
) {
    suspend fun getRelatedGroupList(): List<GroupInfo> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getRelatedGroupList, thread:${Thread.currentThread()}")
        val groupIds = RelationsUseCase.getInstance().getRelatedGroupIds()
        if (groupIds.isEmpty()) {
            return@withContext emptyList()
        }
        val groupInfoList = groupInfoDao.getGroups(*groupIds.toTypedArray())
        PictureUrlMapper.mapPictureUrl(groupInfoList)
        return@withContext groupInfoList
    }

    suspend fun getUnRelatedGroupList(): List<GroupInfo> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getUnRelatedGroupList, thread:${Thread.currentThread()}")
        val groupIds = RelationsUseCase.getInstance().getUnRelatedGroupIds()
        if (groupIds.isEmpty()) {
            return@withContext emptyList()
        }
        val groupInfoList = groupInfoDao.getGroups(*groupIds.toTypedArray())
        PictureUrlMapper.mapPictureUrl(groupInfoList)
        return@withContext groupInfoList
    }

    suspend fun addRelatedGroupInfoList(groupInfoList: List<GroupInfo>) =
        withContext(Dispatchers.IO) {
            PurpleLogger.current.d(TAG, "addRelatedGroupInfoList, thread:${Thread.currentThread()}")
            if (groupInfoList.isNotEmpty()) {
                groupInfoDao.addGroup(*groupInfoList.toTypedArray())
            }
            val userInfoList = groupInfoList.mapNotNull { it.userInfoList }.flatten()
            if (userInfoList.isNotEmpty()) {
                userInfoDao.addUserInfo(*userInfoList.toTypedArray())
            }
        }

    companion object {

        private const val TAG = "GroupInfoRepository"

        private var INSTANCE: GroupInfoRepository? = null

        fun getInstance(): GroupInfoRepository {
            if (INSTANCE == null) {
                val repository =
                    GroupInfoRepository(GroupInfoDao.getInstance(), UserInfoDao.getInstance())
                INSTANCE = repository
            }
            return INSTANCE!!
        }
    }
}
