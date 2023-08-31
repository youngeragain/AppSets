package xcj.app.appsets.db.room.repository

import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.ktx.isHttpUrl
import xcj.app.appsets.purple_module.ModuleConstant
import xcj.app.appsets.server.model.GroupInfo
import xcj.app.appsets.usecase.UserRelationsCase
import xcj.app.core.android.ApplicationHelper
import xcj.app.io.components.SimpleFileIO

class UserGroupsRoomRepository {
    private val groupInfoDao by lazy {
        ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            ?.groupInfoDao()
            ?: throw Exception("groupInfoDao 未初始化")
    }
    private val userInfoDao by lazy {
        ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)
            ?.userInfoDao()
            ?: throw Exception("userInfoDao 未初始化")
    }

    suspend fun getRelatedGroupList(): List<GroupInfo>? {
        val groupIds = UserRelationsCase.getInstance().relatedGroupIdMap?.keys
        if (groupIds.isNullOrEmpty())
            return null
        val groupInfoList = groupInfoDao.getGroups(*groupIds.toTypedArray())
        mapIconUrl(groupInfoList)
        return groupInfoList
    }

    suspend fun getUnRelatedGroupList(): List<GroupInfo>? {
        val groupIds = UserRelationsCase.getInstance().unRelateGroupIds
        if (groupIds.isNullOrEmpty())
            return null
        val groupInfoList = groupInfoDao.getGroups(*groupIds.toTypedArray())
        mapIconUrl(groupInfoList)
        return groupInfoList
    }

    suspend fun addRelatedGroupInfoList(groupInfoList: List<GroupInfo>) {
        //Log.e("UserGroupsRoomRepository", "addRelatedGroupInfoList, userInfoDao:${userInfoDao}")
        if (groupInfoList.isNotEmpty())
            groupInfoDao.addGroup(*groupInfoList.toTypedArray())
        val userInfoList = groupInfoList.mapNotNull { it.userInfoList }.flatten()
        if (userInfoList.isNotEmpty())
            userInfoDao.addUserInfo(*userInfoList.toTypedArray())
    }


    companion object {
        private var INSTANCE: UserGroupsRoomRepository? = null
        fun getInstance(): UserGroupsRoomRepository {
            return INSTANCE ?: synchronized(this) {
                val userGroupsRoomRepository = UserGroupsRoomRepository()
                INSTANCE = userGroupsRoomRepository
                userGroupsRoomRepository
            }
        }

        fun mapIconUrl(groupInfoList: List<GroupInfo>) {
            groupInfoList.forEach { groupInfo ->
                if (!groupInfo.iconUrl.isNullOrEmpty() && !groupInfo.iconUrl.isHttpUrl()) {
                    groupInfo.iconUrl =
                        SimpleFileIO.getInstance().generatePreSign(groupInfo.iconUrl!!)
                            ?: groupInfo.iconUrl
                }
            }
        }
    }
}
