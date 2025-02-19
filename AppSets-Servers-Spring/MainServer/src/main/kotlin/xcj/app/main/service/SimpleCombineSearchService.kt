package xcj.app.main.service

import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import xcj.app.DesignResponse
import xcj.app.main.dao.mongo.ApplicationDao
import xcj.app.main.dao.mysql.GroupDao
import xcj.app.main.dao.mysql.UserScreenDao
import xcj.app.main.model.res.CombineSearchRes
import xcj.app.main.model.res.GroupInfoRes
import xcj.app.main.model.res.UserInfoRes
import xcj.app.main.model.res.UserScreenRes
import xcj.app.main.model.table.mongo.Application

@Service
class SimpleCombineSearchService(
    val userDao: xcj.app.main.dao.mysql.UserDao,
    val screenDao: UserScreenDao,
    val groupDao: GroupDao,
    val applicationDao: ApplicationDao
) : SearchService {

    companion object {
        private const val TAG = "SimpleCombineSearchService"
        const val TYPE_ALL = "all"
        const val TYPE_APPLICATION = "application"
        const val TYPE_USER = "user"
        const val TYPE_GROUP = "group"
        const val TYPE_SCREEN = "screen"
    }

    override fun searchByKeywords(
        keywords: String,
        types: String?,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<CombineSearchRes> {
        val limit = pageSize ?: 20
        val offset = ((page ?: 1) - 1) * limit
        if (types.isNullOrEmpty() || types == TYPE_ALL) {
            val accountEnc = DigestUtils.md5DigestAsHex(keywords.toByteArray())
            val applicationList = applicationDao.searchApplicationsByKeywords(keywords, limit, offset)
            val userInfoResList = userDao.searchUserInfoResByKeywords(accountEnc, keywords, limit, offset)
            val groupInfoResList = groupDao.searchChatGroupResListByKeywords(keywords, limit, offset)
            val screenInfoResList = screenDao.searchScreenResByKeywords(keywords, limit, offset)
            return DesignResponse(
                data = CombineSearchRes(
                    applicationList,
                    screenInfoResList,
                    userInfoResList,
                    groupInfoResList
                )
            )
        }

        var applicationList: List<Application>? = null
        if (types.contains(TYPE_APPLICATION)) {
            applicationList = applicationDao.searchApplicationsByKeywords(keywords, limit, offset)
        }
        var userInfoResList: List<UserInfoRes>? = null
        if (types.contains(TYPE_USER)) {
            val accountEnc = DigestUtils.md5DigestAsHex(keywords.toByteArray())
            userInfoResList = userDao.searchUserInfoResByKeywords(accountEnc, keywords, limit, offset)
        }
        var groupInfoResList: List<GroupInfoRes>? = null
        if (types.contains(TYPE_GROUP)) {
            groupInfoResList = groupDao.searchChatGroupResListByKeywords(keywords, limit, offset)
        }
        var screenInfoResList: List<UserScreenRes>? = null
        if (types.contains(TYPE_SCREEN)) {
            screenInfoResList = screenDao.searchScreenResByKeywords(keywords, limit, offset)
        }
        return DesignResponse(
            data = CombineSearchRes(
                applicationList,
                screenInfoResList,
                userInfoResList,
                groupInfoResList
            )
        )
    }
}
