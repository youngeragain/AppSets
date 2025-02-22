package xcj.app.web.webserver.interfaces

fun interface ContentReceivedListener {
    fun onContentReceived(content: Any)
}