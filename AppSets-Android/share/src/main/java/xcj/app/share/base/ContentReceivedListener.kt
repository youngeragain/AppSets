package xcj.app.share.base

fun interface ContentReceivedListener {
    fun onContentReceived(contentType: String?, content: Any?)
}