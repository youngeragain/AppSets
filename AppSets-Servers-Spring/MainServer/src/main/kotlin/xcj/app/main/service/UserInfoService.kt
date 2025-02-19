package xcj.app.main.service

import xcj.app.DesignResponse
import xcj.app.main.model.req.UpdateUserInfoParams
import xcj.app.main.model.res.UserInfoRes

interface UserInfoService {

    fun getUserInfoByUid(uid: String): DesignResponse<UserInfoRes>

    fun getUserInfoByToken(token: String): DesignResponse<UserInfoRes>

    fun updateUserInfo(token: String, updateUserInfoParam: UpdateUserInfoParams): DesignResponse<Boolean>

    fun searchUsersByKeywords(keywords: String, page: Int?, pageSize: Int?): DesignResponse<List<UserInfoRes>>

}