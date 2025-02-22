package xcj.app.share.http.model

data class ContentInfoListWrapper(
    val uri: String,
    val count: Int,
    val infoList: List<ContentInfo>
) {
    fun decode(): ContentInfoListWrapper {
        infoList.forEach { encodeInfo ->
            encodeInfo.decode()
        }
        return this
    }

    fun encode(): ContentInfoListWrapper {
        infoList.forEach { encodeInfo ->
            encodeInfo.encode()
        }
        return this
    }
}
