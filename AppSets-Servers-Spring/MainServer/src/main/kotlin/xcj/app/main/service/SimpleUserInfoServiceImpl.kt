package xcj.app.main.service

import org.springframework.stereotype.Service
import org.springframework.util.DigestUtils
import xcj.app.ApiDesignCode
import xcj.app.DesignResponse
import xcj.app.main.dao.mysql.UserDao
import xcj.app.main.dao.mysql.UserInfoDao
import xcj.app.main.model.req.UpdateUserInfoParams
import xcj.app.main.model.res.UserInfoRes
import xcj.app.main.util.TokenHelper
import xcj.app.util.PurpleLogger

@Service
class SimpleUserInfoServiceImpl(
    private val tokenHelper: TokenHelper,
    private val userDao: UserDao,
    private val userInfoDao: UserInfoDao
) : UserInfoService {

    companion object {
        private const val TAG = "SimpleUserInfoServiceImpl"
    }

    override fun getUserInfoByUid(uid: String): DesignResponse<UserInfoRes> {
        val userInfo = userDao.getUserInfoResByUid(uid)
        return DesignResponse(data = userInfo)
    }

    override fun getUserInfoByToken(token: String): DesignResponse<UserInfoRes> {
        val uid = tokenHelper.getUidByToken(token)
        return getUserInfoByUid(uid)
    }

    override fun updateUserInfo(token: String, updateUserInfoParam: UpdateUserInfoParams): DesignResponse<Boolean> {
        PurpleLogger.current.d(TAG, message = "updateUserInfo:${updateUserInfoParam}")
        val uid = tokenHelper.getUidByToken(token)
        if (uid != updateUserInfoParam.uid) {
            return DesignResponse(info = "Update userInfo with userId:${updateUserInfoParam.uid} failed!", data = false)
        }
        val updateUserInfoResult = userInfoDao.updateUserInfo(updateUserInfoParam.toUserInfo())
        return if (updateUserInfoResult == 1) {
            DesignResponse(info = "Update userInfo with userId:${updateUserInfoParam.uid} successful!", data = true)
        } else {
            DesignResponse(
                ApiDesignCode.ERROR_CODE_FATAL,
                "Update userInfo with userId:${updateUserInfoParam.uid} failed!",
                data = false
            )
        }
    }

    override fun searchUsersByKeywords(
        keywords: String,
        page: Int?,
        pageSize: Int?
    ): DesignResponse<List<UserInfoRes>> {
        val accountEnc = DigestUtils.md5DigestAsHex(keywords.toByteArray())
        val limit = pageSize ?: 20
        val offset = ((page ?: 1) - 1) * limit
        val userList = userDao.searchUserInfoResByKeywords(accountEnc, keywords, limit, offset)
        return DesignResponse(data = userList)
    }
}