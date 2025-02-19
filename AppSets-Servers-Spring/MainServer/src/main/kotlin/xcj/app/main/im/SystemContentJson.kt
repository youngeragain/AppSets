package xcj.app.main.im

import com.google.gson.Gson

/**
 * 业务数据为type指定
 * 当前有请求添加朋友，请求加入群组
 * @param type friend_request, group_request.....
 *
 */
data class SystemContentJson(val type: String, val content: String) {
    fun getContentObject(gson: Gson): SystemContentInterface? {
        when (type) {
            SystemContentInterface.ADD_FRIEND_REQUEST -> {
                runCatching {
                    return gson.fromJson(
                        content,
                        SystemContentInterface.FriendRequestJson::class.java
                    )
                }
            }

            SystemContentInterface.ADD_FRIEND_REQUEST_FEEDBACK -> {
                runCatching {
                    return gson.fromJson(
                        content,
                        SystemContentInterface.FriendRequestFeedbackJson::class.java
                    )
                }
            }

            SystemContentInterface.JOIN_GROUP_REQUEST -> {
                runCatching {
                    return gson.fromJson(
                        content,
                        SystemContentInterface.GroupRequestJson::class.java
                    )
                }
            }

            SystemContentInterface.JOIN_GROUP_REQUEST_FEEDBACK -> {
                runCatching {
                    return ImMessageGenerator.gson.fromJson(
                        content,
                        SystemContentInterface.GroupJoinRequestFeedbackJson::class.java
                    )
                }
            }

            else -> return null
        }
        return null
    }
}