package xcj.app.userinfo.service

import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.CoreLogger
import xcj.app.userinfo.TokenHelper
import xcj.app.userinfo.dao.mysql.UserDao
import xcj.app.userinfo.dao.mysql.UserInfoDao
import xcj.app.userinfo.model.req.UpdateUserInfoParams
import xcj.app.userinfo.model.res.UserInfoRes

@Service
class SimpleUserInfoServiceImpl(
    private val tokenHelper: TokenHelper,
    private val userDao: UserDao,
    private val userInfoDao: UserInfoDao
    ):UserInfoService {
    override fun getUserInfoByUid(uid: String): DesignResponse<UserInfoRes> {
        val userInfo = userDao.getUserInfoByUid(uid)
        return DesignResponse(data = userInfo)
    }

    override fun getUserInfoByToken(token: String): DesignResponse<UserInfoRes> {
        val uid = tokenHelper.getUidByToken(token)
        return getUserInfoByUid(uid)
    }

    override fun updateUserInfo(updateUserInfoParam: UpdateUserInfoParams): DesignResponse<Boolean> {
        CoreLogger.d(message = "updateUserInfo:${updateUserInfoParam}")
        val updateUserInfoResult = userInfoDao.updateUserInfo(updateUserInfoParam.toUserInfo())
        return if(updateUserInfoResult==1)
            DesignResponse(info = "Update userInfo with userId:${updateUserInfoParam.uid} successful!")
        else
            DesignResponse(ApiDesignCode.ERROR_CODE_FATAL, "Update userInfo with userId:${updateUserInfoParam.uid} failed!")
    }

    override fun searchUsersByKeywords(keywords: String, page: Int?, pageSize: Int?): DesignResponse<List<UserInfoRes>> {
        val accountEnc = DigestUtils.md5DigestAsHex(keywords.toByteArray())
        val limit = pageSize?:20
        val offset = ((page?:1)-1)*limit
        val userList = userDao.searchUserByKeywords(accountEnc, keywords , limit, offset)
        return DesignResponse(data = userList)
    }
}