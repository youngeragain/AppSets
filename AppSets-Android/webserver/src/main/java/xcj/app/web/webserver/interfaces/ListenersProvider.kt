package xcj.app.web.webserver.interfaces

import xcj.app.web.webserver.base.ProgressListener

interface ListenersProvider {
    fun getSendProgressListener(): ProgressListener?
    fun getReceiveProgressListener(): ProgressListener?
    fun getContentReceivedListener(): ContentReceivedListener?
}