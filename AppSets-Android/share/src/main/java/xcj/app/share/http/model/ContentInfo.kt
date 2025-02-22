@file:OptIn(ExperimentalEncodingApi::class)

package xcj.app.share.http.model

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

/**
 * @param id is DataContent.id
 * @param type, 0:String, 1:File
 */
data class ContentInfo(
    val id: String,
    var name: String,
    val size: Long = 0,
    val type: Int = 0,
) {
    companion object{
        const val TYPE_STRING = 0
        const val TYPE_FILE = 1
        const val TYPE_URI = 2
        const val TYPE_BYTES = 3
    }
    fun decode(): ContentInfo {
        val decodeName = Base64.decode(name).decodeToString()
        name = decodeName
        return this
    }

    fun encode(): ContentInfo {
        val encodeName = Base64.encode(name.toByteArray())
        name = encodeName
        return this
    }
}