package xcj.app.main.model.redis

import xcj.app.main.model.req.AddUserScreenParams
import xcj.app.main.model.res.UserInfoRes

data class MediaFallDataObject(
    val userInfo: UserInfoRes,
    val addUserScreenParams: AddUserScreenParams
)