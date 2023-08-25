package xcj.app.userinfo.model.redis

import xcj.app.userinfo.model.req.LoginParams
import java.util.Calendar

class TokenInRedisWrapper(
    var createTime: Long,
    var expireAt: Long,
    var lastToken:String?,
    var canMultiOnline:Boolean?,
    val loginParams: LoginParams?
){
    val isExpire:Boolean
    get() {
        return Calendar.getInstance().time.time>expireAt
    }
}