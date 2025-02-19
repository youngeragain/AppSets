package xcj.app.share.wlanp2p.base

fun interface DataHandleExceptionListener {
    fun onException(type: String, exception: Exception)
}