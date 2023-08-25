package xcj.app.appsets.usecase

import android.util.Log
import xcj.app.appsets.db.room.AppDatabase
import xcj.app.appsets.db.room.dao.UserRelationDao
import xcj.app.appsets.db.room.entity.UserRelation
import xcj.app.core.android.ApplicationHelper
import xcj.app.purple_module.ModuleConstant

class UserRelationsCase private constructor() {
    var relatedUids: MutableSet<String>? = null
    var unRelateUids: MutableSet<String>? = null

    //key:groupId, value:userIds
    var relatedGroupIdMap: MutableMap<String, Set<String>?>? = null
    var unRelateGroupIds: MutableSet<String>? = null

    private val userRelationDao: UserRelationDao? by lazy {
        ApplicationHelper.getDataBase<AppDatabase>(ModuleConstant.MODULE_NAME)?.userRelationDao()
    }

    fun hasUserRelated(uid: String): Boolean {
        if (relatedUids.isNullOrEmpty())
            return false
        return relatedUids!!.contains(uid)
    }

    fun hasGroupRelated(groupId: String): Boolean {
        if (relatedGroupIdMap.isNullOrEmpty())
            return false
        return relatedGroupIdMap!!.keys.contains(groupId)
    }

    suspend fun initRelatedUsersFromNew(idList: Set<String>) {
        Log.e("UserRelationsCase", "initRelatedUsersFromNew, idList:${idList}")
        kotlin.runCatching {
            val userRelations = idList.map {
                UserRelation(it, "user", 1)
            }
            userRelationDao?.addUserRelation(*userRelations.toTypedArray())
        }
        relatedUids = idList.toMutableSet()

        if (!relatedUids.isNullOrEmpty() && !unRelateUids.isNullOrEmpty()) {
            val iterator = unRelateUids!!.iterator()
            while (iterator.hasNext()) {
                val unRelatedUid = iterator.next()
                if (relatedUids!!.contains(unRelatedUid)) {
                    iterator.remove()
                }
            }
        }
    }

    fun addUnRelatedUid(uid: String) {
        if (unRelateUids == null)
            unRelateUids = mutableSetOf()
        unRelateUids!!.add(uid)
    }

    suspend fun initUnRelatedUsersFromNew(idList: Set<String>) {
        kotlin.runCatching {
            val userRelations = idList.map {
                UserRelation(it, "user", 0)
            }
            userRelationDao?.addUserRelation(*userRelations.toTypedArray())
        }
        unRelateUids = idList.toMutableSet()
    }

    suspend fun initRelatedGroupsFromNew(groupIdMap: MutableMap<String, Set<String>?>) {
        kotlin.runCatching {
            val userRelations = groupIdMap.map {
                UserRelation(it.key, "group", 1)
            }
            userRelationDao?.addUserRelation(*userRelations.toTypedArray())
        }
        relatedGroupIdMap = groupIdMap
    }

    suspend fun initUnRelatedGroupsFromNew(groupIdList: Set<String>) {
        kotlin.runCatching {
            val userRelations = groupIdList.map {
                UserRelation(it, "group", 0)
            }
            userRelationDao?.addUserRelation(*userRelations.toTypedArray())
        }
        unRelateGroupIds = groupIdList.toMutableSet()
    }

    suspend fun initFromDb() {
        kotlin.runCatching {
            val relationList = userRelationDao?.getRelationList()
            Log.e("UserRelationsCase", "relationList:${relationList}")
            if (relationList.isNullOrEmpty())
                return
            val relatedUids = mutableSetOf<String>()
            val unRelateUids = mutableSetOf<String>()
            val relatedGroupIds = mutableSetOf<String>()
            val unRelateGroupIds = mutableSetOf<String>()
            relationList.forEach {
                if (it.type == "user") {
                    if (it.isRelate == 1) {
                        relatedUids.add(it.id)
                    } else if (it.isRelate == 0) {
                        unRelateUids.add(it.id)
                    }
                } else if (it.type == "group") {
                    if (it.isRelate == 1) {
                        relatedGroupIds.add(it.id)
                    } else if (it.isRelate == 0) {
                        unRelateGroupIds.add(it.id)
                    }
                }
            }
            if (relatedUids.isNotEmpty())
                this.relatedUids = relatedUids
            if (unRelateUids.isNotEmpty())
                this.unRelateUids = unRelateUids
            if (relatedGroupIds.isNotEmpty())
                this.relatedGroupIdMap =
                    relatedGroupIds.associateWith { setOf<String>() }.toMutableMap()
            if (unRelateGroupIds.isNotEmpty())
                this.unRelateGroupIds = unRelateGroupIds
        }.onFailure {
            Log.e("UserRelationsCase", "initFromDb failed! ${it.message}")
        }
    }


    companion object {
        private var INSTANCE: UserRelationsCase? = null
        fun getInstance(): UserRelationsCase {
            return INSTANCE ?: synchronized(UserRelationsCase::class.java) {
                if (INSTANCE == null) {
                    INSTANCE = UserRelationsCase()
                }
                INSTANCE!!
            }
        }
    }
}