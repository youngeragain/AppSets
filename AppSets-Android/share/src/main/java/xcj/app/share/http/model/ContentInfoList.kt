package xcj.app.share.http.model

data class ContentInfoList(
    val uri: String,
    val count: Int,
    val infoList: List<ContentInfo>
) {
    fun decode(): ContentInfoList {
        infoList.forEach { encodeInfo ->
            encodeInfo.decode()
        }
        return this
    }

    fun encode(): ContentInfoList {
        infoList.forEach { encodeInfo ->
            encodeInfo.encode()
        }
        return this
    }
}
