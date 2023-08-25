package xcj.app.userinfo.service

import xcj.app.DesignResponse
import xcj.app.userinfo.model.req.UpdateUserInfoParams
import xcj.app.userinfo.model.res.UserInfoRes

interface UserInfoService {
    fun getUserInfoByUid(uid: String):DesignResponse<UserInfoRes>
    fun getUserInfoByToken(token: String):DesignResponse<UserInfoRes>
    fun updateUserInfo(updateUserInfoParam: UpdateUserInfoParams):DesignResponse<Boolean>
    fun searchUsersByKeywords(keywords: String, page: Int?, pageSize: Int?): DesignResponse<List<UserInfoRes>>
}