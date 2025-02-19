package xcj.app.appsets.im.model

import com.google.gson.Gson
import xcj.app.appsets.im.ImMessageGenerator

/**
 * 业务数据为type指定
 * 当前有请求添加朋友，请求加入群组
 * @param type friendRequest, groupRequest.....
 *
 */
data class SystemContentJson(val type: String, val content: String) {
    fun getContentObject(gson: Gson): SystemContentInterface? {
        when (type) {
            SystemContentInterface.Companion.ADD_FRIEND_REQUEST -> {
                runCatching {
                    return gson.fromJson(
                        content,
                        FriendRequestJson::class.java
                    )
                }
            }

            SystemContentInterface.Companion.ADD_FRIEND_REQUEST_FEEDBACK -> {
                runCatching {
                    return gson.fromJson(
                        content,
                        FriendRequestFeedbackJson::class.java
                    )
                }
            }

            SystemContentInterface.Companion.JOIN_GROUP_REQUEST -> {
                runCatching {
                    return gson.fromJson(
                        content,
                        GroupRequestJson::class.java
                    )
                }
            }

            SystemContentInterface.Companion.JOIN_GROUP_REQUEST_FEEDBACK -> {
                runCatching {
                    return ImMessageGenerator.gson.fromJson(
                        content,
                        GroupJoinRequestFeedbackJson::class.java
                    )
                }
            }

            else -> return null
        }
        return null
    }
}