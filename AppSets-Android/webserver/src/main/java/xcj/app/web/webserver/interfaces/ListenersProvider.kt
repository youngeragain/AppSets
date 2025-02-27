package xcj.app.web.webserver.interfaces

interface ListenersProvider {
    fun getSendProgressListener(): ProgressListener?
    fun getReceiveProgressListener(): ProgressListener?
    fun getContentReceivedListener(): ContentReceivedListener?
}