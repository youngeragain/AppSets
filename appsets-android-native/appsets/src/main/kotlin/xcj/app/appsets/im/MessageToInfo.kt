package xcj.app.appsets.im

data class MessageToInfo(
    val toType: String,
    val id: String,
    val name: String?,
    val iconUrl: String?,
    val roles: String?
) {
    val isImSingleMessage: Boolean
        get() = toType == "one2one"

    val isImgGroupMessage: Boolean
        get() = toType == "one2many"
}