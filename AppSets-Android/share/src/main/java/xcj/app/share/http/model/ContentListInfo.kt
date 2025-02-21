@file:OptIn(ExperimentalEncodingApi::class)

package xcj.app.share.http.model

import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

data class ContentListInfo(
    var contentListUri: String,
    var count: Int,
    var contentList: List<String>
) {
    fun decode(): ContentListInfo {
        val decodeContentListUri = Base64.decode(contentListUri).decodeToString()
        contentListUri = decodeContentListUri
        val decodeContentList = mutableListOf<String>()
        contentList.forEach { encodeUri ->
            val decodeUri = Base64.decode(encodeUri).decodeToString()
            decodeContentList.add(decodeUri)
        }
        contentList = decodeContentList
        return this
    }

    fun encode(): ContentListInfo {
        val encodeContentListUri = Base64.encode(contentListUri.toByteArray())
        contentListUri = encodeContentListUri
        val encodeContentList = mutableListOf<String>()
        contentList.forEach { noneEncodeUri ->
            val encodeUri = Base64.encode(noneEncodeUri.toByteArray())
            encodeContentList.add(encodeUri)
        }
        contentList = encodeContentList
        return this
    }
}
