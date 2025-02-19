package xcj.app.share.wlanp2p.base

fun interface ISocketExceptionListener {
    fun onException(type: String, exception: Exception)
}