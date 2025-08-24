package xcj.app.appsets.im.message

import xcj.app.appsets.im.ImMessageGenerator

abstract class MessageMetadata<D>(
    open val description: String,
    open val size: Int,//in bytes
    open val compressed: Boolean,
    open val encode: String,//url or base64
    open var data: D, // if encode is url, this will be https://xxxx, if encode is base64, decode this to bytes
    open val contentType: String,
) {
    @Transient
    var url: String? = null

    var localData: Any? = null

    override fun toString(): String {
        return ImMessageGenerator.gson.toJson(this)
    }
}