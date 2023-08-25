package xcj.app.appsets.db.room.repository

import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.server.model.UserInfo
import xcj.app.appsets.usecase.UserRelationsCase
import xcj.app.core.android.ApplicationHelper
import xcj.app.io.components.SimpleFileIO
import xcj.app.purple_module.ModuleConstant

class UserInfoRoomRepository() {

    private val userInfoDao by lazy {
        ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            ?.userInfoDao()
            ?: throw Exception("userInfoDao 未初始化")
    }

    suspend fun addRelatedUserInfoList(userInfoList: List<UserInfo>) {
        userInfoDao.addUserInfo(*userInfoList.toTypedArray())
    }


    suspend fun getRelatedUserList(): List<UserInfo>? {
        val uids = UserRelationsCase.getInstance().relatedUids
        if (uids.isNullOrEmpty())
            return null
        val userInfoByUids = userInfoDao.getUserInfoByUids(*uids.toTypedArray())
        userInfoByUids?.let(::mapAvatarUrl)
        return userInfoByUids
    }

    suspend fun getUnRelatedUserList(): List<UserInfo>? {
        val uids = UserRelationsCase.getInstance().unRelateUids
        if (uids.isNullOrEmpty())
            return null
        val userInfoByUids = userInfoDao.getUserInfoByUids(*uids.toTypedArray())
        userInfoByUids?.let(::mapAvatarUrl)
        return userInfoByUids
    }


    companion object {
        private var INSTANCE: UserInfoRoomRepository? = null
        fun getInstance(): UserInfoRoomRepository {
            if (INSTANCE == null) {
                INSTANCE = UserInfoRoomRepository()
            }
            return INSTANCE!!
        }

        fun mapAvatarUrl(userInfoList: List<UserInfo>) {
            userInfoList.forEach { userInfo ->
                if (!userInfo.avatarUrl.isNullOrEmpty() && !userInfo.avatarUrl.isHttpUrl()) {
                    userInfo.avatarUrl =
                        SimpleFileIO.getInstance().generatePreSign(userInfo.avatarUrl!!)
                            ?: userInfo.avatarUrl
                }
            }
        }
    }
}