package xcj.app.appsets.db.room.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xcj.app.appsets.db.room.dao.UserInfoDao
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.usecase.RelationsUseCase
import xcj.app.appsets.util.PictureUrlMapper
import xcj.app.starter.android.util.PurpleLogger

class UserInfoRepository(
    private val userInfoDao: UserInfoDao
) {

    suspend fun addRelatedUserInfoList(userInfoList: List<UserInfo>) = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "addRelatedUserInfoList, thread:${Thread.currentThread()}")
        return@withContext userInfoDao.addUserInfo(*userInfoList.toTypedArray())
    }

    suspend fun getRelatedUserList(): List<UserInfo> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getRelatedUserList, thread:${Thread.currentThread()}")
        val uids = RelationsUseCase.getInstance().getRelatedUserIds()
        if (uids.isEmpty()) {
            return@withContext emptyList()
        }
        val userInfoList = userInfoDao.getUserInfoByUids(*uids.toTypedArray())
        PictureUrlMapper.mapPictureUrl(userInfoList)
        return@withContext userInfoList
    }

    suspend fun getUnRelatedUserList(): List<UserInfo> = withContext(Dispatchers.IO) {
        PurpleLogger.current.d(TAG, "getUnRelatedUserList, thread:${Thread.currentThread()}")
        val uids = RelationsUseCase.getInstance().getUnRelatedUserIds()
        if (uids.isEmpty()) {
            return@withContext emptyList()
        }
        val userInfoList = userInfoDao.getUserInfoByUids(*uids.toTypedArray())
        PictureUrlMapper.mapPictureUrl(userInfoList)
        return@withContext userInfoList
    }

    companion object {

        private const val TAG = "UserInfoRepository"
        private var INSTANCE: UserInfoRepository? = null

        fun getInstance(): UserInfoRepository {
            if (INSTANCE == null) {
                val repository = UserInfoRepository(UserInfoDao.getInstance())
                INSTANCE = repository
            }
            return INSTANCE!!
        }
    }
}