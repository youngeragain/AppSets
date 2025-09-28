package xcj.app.appsets.usecase

import xcj.app.appsets.db.room.dao.UserRelationDao
import xcj.app.appsets.db.room.entity.UserRelation
import xcj.app.appsets.im.Bio
import xcj.app.appsets.server.model.UserInfo
import xcj.app.starter.android.util.PurpleLogger

class RelationsUseCase private constructor(
    private val userRelationDao: UserRelationDao
) {
    private val relatedUids: MutableSet<String> = mutableSetOf()
    private val unRelateUids: MutableSet<String> = mutableSetOf()

    //key:groupId, value:userIds
    private val relatedGroupIdMap: MutableMap<String, Set<String>?> = mutableMapOf()
    private val unRelateGroupIds: MutableSet<String> = mutableSetOf()

    fun getRelatedUserIds(): List<String> {
        return relatedUids.toList()
    }

    fun getUnRelatedUserIds(): List<String> {
        return unRelateUids.toList()
    }

    fun getRelatedGroupIds(): List<String> {
        return relatedGroupIdMap.keys.toList()
    }

    fun getUnRelatedGroupIds(): List<String> {
        return unRelateUids.toList()
    }

    fun hasUserRelated(uid: String): Boolean {
        val contains = relatedUids.contains(uid)
        if (!contains) {
            addUnRelatedUid(uid)
        }
        return contains
    }

    fun hasGroupRelated(groupId: String): Boolean {
        val contains = relatedGroupIdMap.keys.contains(groupId)
        if (!contains) {
            addUnRelatedGroupId(groupId)
        }
        return contains
    }

    suspend fun initRelatedUsersFromServer(idList: Set<String>) {
        PurpleLogger.current.d(TAG, "initRelatedUsersFromServer, idList:${idList}")
        runCatching {
            val userRelations = idList.map {
                UserRelation(it, UserRelation.TYPE_USER, 1)
            }
            userRelationDao.addUserRelation(*userRelations.toTypedArray())
        }
        idList.forEach {
            addRelatedUid(it)
        }
    }

    private fun addRelatedUid(uid: String): Boolean {
        unRelateUids.remove(uid)
        return relatedUids.add(uid)
    }

    fun addUnRelatedUid(uid: String): Boolean {
        relatedUids.remove(uid)
        return unRelateUids.add(uid)
    }

    fun addUnRelatedGroupId(groupId: String): Boolean {
        return unRelateGroupIds.add(groupId)
    }

    suspend fun initUnRelatedUsersFromServer(idList: Set<String>) {
        runCatching {
            val userRelations = idList.map {
                UserRelation(it, UserRelation.TYPE_USER, 0)
            }
            userRelationDao.addUserRelation(*userRelations.toTypedArray())
        }
        unRelateUids.addAll(idList)
    }

    suspend fun initRelatedGroupsFromServer(groupIdMap: MutableMap<String, Set<String>?>) {
        runCatching {
            val userRelations = groupIdMap.map {
                UserRelation(it.key, UserRelation.TYPE_GROUP, 1)
            }
            userRelationDao.addUserRelation(*userRelations.toTypedArray())
        }
        relatedGroupIdMap.putAll(groupIdMap)
    }

    suspend fun initUnRelatedGroupsFromServer(groupIdList: Set<String>) {
        runCatching {
            val userRelations = groupIdList.map {
                UserRelation(it, UserRelation.TYPE_GROUP, 0)
            }
            userRelationDao.addUserRelation(*userRelations.toTypedArray())
        }
        unRelateGroupIds.addAll(groupIdList)
    }

    suspend fun initRelationFromLocalDB() {
        runCatching {
            val relationList = userRelationDao.getRelationList()
            PurpleLogger.current.d(TAG, "relationList:${relationList}")
            if (relationList.isEmpty()) {
                return
            }
            val relatedUids = relatedUids
            val unRelateUids = unRelateUids
            val relatedGroupIds = mutableSetOf<String>()
            val unRelateGroupIds = unRelateGroupIds
            relationList.forEach {
                if (it.type == UserRelation.TYPE_USER) {
                    if (it.isRelate == 1) {
                        relatedUids.add(it.id)
                    } else if (it.isRelate == 0) {
                        unRelateUids.add(it.id)
                    }
                } else if (it.type == UserRelation.TYPE_GROUP) {
                    if (it.isRelate == 1) {
                        relatedGroupIds.add(it.id)
                    } else if (it.isRelate == 0) {
                        unRelateGroupIds.add(it.id)
                    }
                }
            }
            if (relatedGroupIds.isNotEmpty()) {
                this.relatedGroupIdMap.putAll(
                    relatedGroupIds.associateWith { setOf<String>() }
                        .toMutableMap()
                )
            }
        }.onFailure {
            PurpleLogger.current.d(TAG, "initFromDb failed! ${it.message}")
        }
    }

    fun getGroupIds(): Set<String> {
        val groupIds = mutableSetOf<String>()
        groupIds.addAll(relatedGroupIdMap.keys)
        groupIds.addAll(unRelateGroupIds)
        return groupIds
    }

    fun updateRelatedGroupIfNeeded(bio: Bio): Boolean {
        PurpleLogger.current.d(TAG, "updateRelatedGroupIfNeeded")
        if (bio is UserInfo) {
            return false
        }
        if (hasGroupRelated(bio.bioId)) {
            return false
        }
        addUnRelatedGroupId(bio.bioId)
        return true
    }


    companion object {

        private const val TAG = "UserRelationsCase"

        private var INSTANCE: RelationsUseCase? = null

        fun getInstance(): RelationsUseCase {
            return INSTANCE ?: synchronized(RelationsUseCase::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = RelationsUseCase(UserRelationDao.getInstance())
                }
                INSTANCE!!
            }
        }
    }
}