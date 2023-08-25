package xcj.app.userinfo.service

import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import xcj.app.DesignResponse
import xcj.app.userinfo.dao.mongo.ApplicationDao
import xcj.app.userinfo.dao.mysql.GroupDao
import xcj.app.userinfo.dao.mysql.UserScreenDao
import xcj.app.userinfo.model.res.CombineSearchRes
import xcj.app.userinfo.model.res.GroupInfoRes
import xcj.app.userinfo.model.res.UserInfoRes
import xcj.app.userinfo.model.res.UserScreenInfoRes
import xcj.app.userinfo.model.table.mongo.Application

@Service
class SimpleCombineSearchService(
    val userDao: xcj.app.userinfo.dao.mysql.UserDao,
    val screenDao: UserScreenDao,
    val groupDao: GroupDao,
    val applicationDao: ApplicationDao
):SearchService{
    override fun searchByKeywords(keywords: String, types:String?, page: Int?, pageSize: Int?): DesignResponse<CombineSearchRes> {
        val limit = pageSize?:20
        val offset = ((page?:1)-1)*limit
        if(types.isNullOrEmpty()||types=="all"){
            val accountEnc = DigestUtils.md5DigestAsHex(keywords.toByteArray())
            val applicationList = applicationDao.searchApplicationsByKeywords(keywords, limit, offset)
            val userInfoResList = userDao.searchUserByKeywords(accountEnc, keywords, limit, offset)
            val groupInfoResList = groupDao.searchChatGroupResListByKeywords(keywords, limit, offset)
            val screenInfoResList = screenDao.searchScreenByKeywords(keywords, limit, offset)
            return DesignResponse(data = CombineSearchRes(applicationList, screenInfoResList, userInfoResList, groupInfoResList))
        }

        var applicationList:List<Application>? = null
        if(types.contains("application")){
            applicationList = applicationDao.searchApplicationsByKeywords(keywords, limit, offset)
        }
        var userInfoResList:List<UserInfoRes>? = null
        if(types.contains("user")){
            val accountEnc = DigestUtils.md5DigestAsHex(keywords.toByteArray())
            userInfoResList = userDao.searchUserByKeywords(accountEnc, keywords, limit, offset)
        }
        var groupInfoResList:List<GroupInfoRes>? = null
        if(types.contains("group")){
            groupInfoResList = groupDao.searchChatGroupResListByKeywords(keywords, limit, offset)
        }
        var screenInfoResList:List<UserScreenInfoRes>? = null
        if(types.contains("screen")){
            screenInfoResList = screenDao.searchScreenByKeywords(keywords, limit, offset)
        }
        return DesignResponse(data = CombineSearchRes(applicationList, screenInfoResList, userInfoResList, groupInfoResList))
    }
}
