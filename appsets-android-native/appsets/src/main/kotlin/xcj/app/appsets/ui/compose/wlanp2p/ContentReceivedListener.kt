package xcj.app.appsets.ui.compose.wlanp2p

fun interface ContentReceivedListener {
    fun onContentReceived(contentType: String?, content: Any?)
}