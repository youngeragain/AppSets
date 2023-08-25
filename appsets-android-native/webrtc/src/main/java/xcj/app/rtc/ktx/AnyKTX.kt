package xcj.app.rtc.ktx

import xcj.app.rtc.DependencyProvider

fun Any?.jsonify():String?{
    if (this == null||(this is String && this.isNullOrEmpty())){
        return null
    }
    return DependencyProvider.gson.toJson(this)
}