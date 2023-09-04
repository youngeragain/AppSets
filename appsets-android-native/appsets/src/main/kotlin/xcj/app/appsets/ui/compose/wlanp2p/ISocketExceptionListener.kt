package xcj.app.appsets.ui.compose.wlanp2p

fun interface ISocketExceptionListener {
    fun onException(type: String, exception: Exception)
}