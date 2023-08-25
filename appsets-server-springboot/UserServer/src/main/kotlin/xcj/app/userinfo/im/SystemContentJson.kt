package xcj.app.userinfo.im



/**
 * 业务数据为type指定
 * 当前有请求添加朋友，请求加入群组
 * @param type friend_request, group_request.....
 *
 */
data class SystemContentJson(val type:String, val content:String)